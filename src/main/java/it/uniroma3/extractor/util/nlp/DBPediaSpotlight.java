package it.uniroma3.extractor.util.nlp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import it.uniroma3.config.Configuration;
import it.uniroma3.extractor.entitydetection.PatternComparator;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.io.TSVReader;

/**
 * @author matteo
 */
public class DBPediaSpotlight {

    private HttpClient client;
    private double confidence;
    private int support;

    private Set<String> blacklist_wikilinks;	// this is a list of all the wikilinks that we do not want to highlight as entities
    private Set<String> blacklist_names;	// this is a list of all the rendered names that we do not want to highlight as entities


    /**
     * @param confidence
     * @param support
     */
    public DBPediaSpotlight(double confidence, int support) {
	this.confidence = confidence;
	this.support = support;
	client = new HttpClient(new MultiThreadedHttpConnectionManager());

	this.blacklist_wikilinks = new HashSet<String>();
	this.blacklist_wikilinks.addAll(TSVReader.getLines2Set(Configuration.getCurrenciesList()));
	this.blacklist_wikilinks.addAll(TSVReader.getLines2Set(Configuration.getProfessionsList()));
	this.blacklist_names = new HashSet<String>();
	this.blacklist_names.addAll(TSVReader.getLines2Set(Configuration.getNationalitiesList()));
    }

    /**
     * Creates the request to the DBPediaSpotlight service.
     *
     * @param text
     * @return
     */
    private JsonObject getAnnotatedText(String text) {
	JsonObject response = null;    // Read the response body.
	GetMethod getMethod = null;
	// we do not want to process long sentences.
	if (text.length() < 600){
	    try {
		getMethod = new GetMethod(
			Configuration.getSpotlightLocalURL() +
			"/annotate/?" +
			"confidence=" + this.confidence +
			"&support=" + this.support +
			"&text=" + URLEncoder.encode(stripNonValidXMLCharacters(text), "utf-8"));

		getMethod.addRequestHeader(new Header("Accept", "application/json"));
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
			new DefaultHttpMethodRetryHandler(3, false));

		// Execute the method.
		int statusCode = client.executeMethod(getMethod);
		if (statusCode == HttpStatus.SC_OK) {
		    InputStream responseBody = getMethod.getResponseBodyAsStream();
		    response = new JsonParser().parse(new InputStreamReader(responseBody)).getAsJsonObject();
		}

	    } catch (IOException e) {
		e.printStackTrace();
	    } finally{
		getMethod.releaseConnection();
	    }
	}
	return response;
    }

    /**
     * This method ensures that the output String has only
     * valid XML unicode characters.
     * 
     * @param inputText
     * @return
     */
    private static String stripNonValidXMLCharacters(String inputText) {
	StringBuffer out = new StringBuffer(); 
	char c; 
	if (inputText == null || ("".equals(inputText))) return ""; 
	for (int i = 0; i < inputText.length(); i++) {
	    c = inputText.charAt(i);
	    if ((c == 0x9) ||
		    (c == 0xA) ||
		    (c == 0xD) ||
		    ((c >= 0x20) && (c <= 0xD7FF)) ||
		    ((c >= 0xE000) && (c <= 0xFFFD)) ||
		    ((c >= 0x10000) && (c <= 0x10FFFF)))
		out.append(c);
	}
	return out.toString();
    }   

    /**
     * @param input
     * @return
     */
    private List<Annotation> process(String text) {
	JsonObject response = getAnnotatedText(text);
	List<Annotation> annotatedEntities = new ArrayList<Annotation>();

	if (response != null){
	    JsonArray jarray = response.getAsJsonArray("Resources");
	    Gson gson = new Gson();
	    if (jarray != null)
		for (JsonElement jres : jarray) {
		    try{
			Annotation ann = gson.fromJson(jres, Annotation.class);
			ann.setWikid();
			if ((Character.isUpperCase(ann.getSurfaceForm().charAt(0)) ||
				Character.isDigit(ann.getSurfaceForm().charAt(0))) && 
				!blacklist_wikilinks.contains(ann.getWikid()) &&
				!blacklist_names.contains(ann.getSurfaceForm())){
			    annotatedEntities.add(ann);
			}
		    }catch(JsonSyntaxException e){
		    }
		}
	}
	return annotatedEntities;
    }

    /**
     * Returns a list of pairs composed by surface form and related entity.
     * Entities are marked with PE or SE based on the input PE given.
     *
     * @param text
     * @param PE
     * @return
     */
    private List<Pair<String, String>> getAnnotations(String text, String PE) {
	List<Annotation> annotations = process(text);
	List<Pair<String, String>> textualAnnotations = new ArrayList<Pair<String, String>>();
	for (Annotation an : annotations) {
	    if (an.getWikid().equals(PE))
		textualAnnotations.add(Pair.make(an.getSurfaceForm(), an.toString("PE")));
	    else
		textualAnnotations.add(Pair.make(an.getSurfaceForm(), an.toString("SE")));
	}
	return textualAnnotations;
    }

    /**
     * To match a name it has to be in a sentence sourrounded by two boarders (\\b) that are
     * not square bracket, _ or pipe | (which are terms that are inside a wikilink).
     * <p>
     * https://regex101.com/r/qdZyYl/4
     *
     * @param name
     * @return
     */
    private String createRegexName(String name) {
	return "(\\s[^\\sA-Z]++\\s|(?:^|\\. |, |: |\\n)(?:\\w++\\s)?)\\b(" + Pattern.quote(name) + ")\\b(?!\\s[A-Z][a-z]++|-|<| <)";
    }

    /**
     * @param sentence
     * @param replacement
     * @param pattern
     * @return
     * @throws Exception
     */
    private String applyRegex(String sentence, String replacement, String pattern) throws Exception {
	StringBuffer tmp = new StringBuffer();
	try {
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(sentence);
	    while (m.find()) {
		// we attached the part of text before the entities (m.group(1)) and then the entity replaced.
		m.appendReplacement(tmp, Matcher.quoteReplacement(m.group(1)) + Matcher.quoteReplacement(replacement));
	    }
	    m.appendTail(tmp);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Exception();
	}
	return tmp.toString();
    }

    /**
     * Checks if it is woth to process the sentence with a dbpedia spotlight.
     * I.e. it is worth if there is at least one capital letter in the span of text
     * outside already marked entities.
     * 
     * 
     * @param sentence
     * @return
     */
    private static boolean checkIsWorth(String sentence){
	boolean isWorth = true;
	//remove first capital letter...
	sentence = sentence.substring(1);

	for (String span : sentence.split("<.*?>>")){
	    isWorth = span.matches("^(.*?[A-Z]).*$");
	    if (isWorth)
		return true;
	}
	return false;
    }

    /**
     * @param block
     * @param PE
     * @return
     */
    public List<String> annotateText(String block, String PE) {
	List<String> sentences = new LinkedList<String>();

	for (String sentence : StupidNLP.splitSentence(block)) {
	    if (checkIsWorth(sentence)){
		List<Pair<String, String>> annotations = getAnnotations(sentence, PE);
		List<Pair<String, String>> regex2entity = new ArrayList<Pair<String, String>>();
		for (Pair<String, String> entity : annotations) {
		    regex2entity.add(Pair.make(createRegexName(entity.key), entity.value));
		}
		Collections.sort(regex2entity, new PatternComparator());
		for (Pair<String, String> regex : regex2entity) {
		    try {
			sentence = applyRegex(sentence, regex.value, regex.key);
		    } catch (Exception e) {
			System.out.println("Exception in:	" + regex.value);
			System.out.println("using the regex:	" + regex.key);
			System.out.println("--------------------------------------------------");
			break;
		    }
		}
	    }
	    // in any case, add the sentence to the "annotated" list of sentences, i.e. document
	    sentences.add(sentence);
	}
	return sentences;
    }

}