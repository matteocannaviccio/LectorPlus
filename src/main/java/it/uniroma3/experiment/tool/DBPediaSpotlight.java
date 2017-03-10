package it.uniroma3.experiment.tool;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.stanford.nlp.util.StringUtils;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.parser.TextParser;
import it.uniroma3.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class DBPediaSpotlight{

    private static String API_URL = "http://spotlight.sztaki.hu:2222/";
    private static double confidence = 0.5;
    private static int support = 0;

    /**
     * 
     * @param text
     * @return
     */
    private static JsonObject getAnnotatedText(String text){
	// Read the response body.
	InputStream responseBody = null;
	try {

	    GetMethod getMethod = new GetMethod(API_URL + "rest/annotate/?"
		    + "confidence=" + confidence + "&support=" + support
		    + "&text=" + URLEncoder.encode(text, "utf-8"));
	    getMethod.addRequestHeader(new Header("Accept", "application/json"));
	    getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));

	    // Execute the method.
	    int statusCode = new HttpClient().executeMethod(getMethod);
	    if (statusCode != HttpStatus.SC_OK) {
		System.out.println("Method failed: " + getMethod.getStatusLine());
	    }
	    responseBody = getMethod.getResponseBodyAsStream();

	} catch (IOException e) {
	    e.printStackTrace();
	}
	// Create the JSON object.
	JsonObject jobject = new JsonParser().parse(new InputStreamReader(responseBody)).getAsJsonObject();

	return jobject;
    }

    /**
     * 
     * @param input
     * @return
     */
    private static List<Annotation> process(String text) {
	JsonObject response = getAnnotatedText(text);
	JsonArray jarray = response.getAsJsonArray("Resources");
	List<Annotation> annotatedEntities = new ArrayList<Annotation>();
	Gson gson = new Gson();
	if (jarray != null)
	    for (JsonElement jres : jarray){
		Annotation ann = gson.fromJson(jres, Annotation.class);
		ann.setWikid();
		if (Character.isUpperCase(ann.getSurfaceForm().charAt(0)) || Character.isDigit(ann.getSurfaceForm().charAt(0)))
		    annotatedEntities.add(ann);
	    }
	return annotatedEntities;
    }

    /**
     * 
     * @param text
     * @return
     */
    private static List<String> getAnnotations(String text, String PE){
	List<Annotation> annotations = process(text);
	List<String> textualAnnotations = new ArrayList<String>();
	for (Annotation an : annotations){
	    if (an.getWikid().equals(PE))
		textualAnnotations.add(an.toString("PE"));
	    else
		textualAnnotations.add(an.toString("SE"));
	}
	return textualAnnotations;
    }

    /**
     * 
     * @param block
     * @param PE
     * @return
     */
    private static Pair<Integer, List<String>> getBlockAnnotations(String block, String PE){
	List<String> textualAnnotations = getAnnotations(block, PE);
	List<String> secEnt = new ArrayList<String>();
	int countPE = 0;

	Pattern SE = Pattern.compile("SE[^<]*<([^>]*)>");
	for (String annotation : textualAnnotations){
	    Matcher m = SE.matcher(annotation);
	    if(m.find()){
		secEnt.add("wiki/" + m.group(1));
	    }else{
		countPE++;
	    }
	}
	return Pair.make(countPE, secEnt);
    }


    /**
     * 
     * @param article
     * @return
     */
    public static String getCompleteStats(WikiArticle article){
	int globalPeAbstract = 0;
	int globalPeBody = 0;
	List<String> globalSeAbstract = new ArrayList<String>();
	List<String> globalSeBody = new ArrayList<String>();

	for (Entry<String, String> block : article.getCleanBlocks().entrySet()){
	    for (String sentence : TextParser.splitSentences(block.getValue())){
		Pair<Integer, List<String>> stats = getBlockAnnotations(sentence, article.getWikid());
		if (block.getKey().equals("#Abstract")){
		    globalPeAbstract += stats.key;
		    globalSeAbstract.addAll(stats.value);
		}else{
		    globalPeBody += stats.key;
		    globalSeBody.addAll(stats.value);
		}
	    }
	}
	if (globalSeAbstract.isEmpty())
	    globalSeAbstract.add("-");
	if (globalSeBody.isEmpty())
	    globalSeBody.add("-");

	return 
		article.getWikid() + "\t" 
		+ globalPeAbstract + "\t" 
		+ globalPeBody + "\t" 
		+ StringUtils.join(globalSeAbstract, " ") + "\t"
		+ StringUtils.join(globalSeBody, " ");
    }


    public static void main(String[] args) {
	getAnnotations("Turing was born in Maida Vale, London, while his father, Julius Mathison Turing (1873–1947), was on leave from his position with the Indian Civil Service (ICS) at Chhatrapur, Bihar and Orissa Province, in British India.[13][14] Turing's father was the son of a clergyman, the Rev. John Robert Turing, from a Scottish family of merchants that had been based in the Netherlands and included a baronet. Turing's mother, Julius' wife, was Ethel Sara (née Stoney; 1881–1976), daughter of Edward Waller Stoney, chief engineer of the Madras Railways. The Stoneys were a Protestant Anglo-Irish gentry family from both County Tipperary and County Longford, while Ethel herself had spent much of her childhood in County Clare.", "Alan_Turing");
    }

}