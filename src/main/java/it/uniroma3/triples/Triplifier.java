package it.uniroma3.triples;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.util.StringUtils;
import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.util.Pair;

/**
 * This module extracts triples from articles.
 * 
 * Before to extract the triple we pre-process the sentence:
 * - remove parenthesis
 * - we do not consider triples where the object is followed by 's
 * - we do not consider triples with phrases that end with "that"
 * 
 * @author matteo
 *
 */
public class Triplifier {

    private Queue<MultiValue> mvl;
    private Queue<Pair<String, Triple>> triplesNER;
    private Queue<Pair<String, Triple>> triplesMVL;
    private Queue<Pair<Pair<String, Triple>, String>> triplesLabeled;
    private Queue<Pair<String, Triple>> triplesUnlabeled;

    private WitnessCounter wc;

    private File mvlFile;
    private File mvlTriplesFile;
    private File nerTriplesFile;
    private File labledTriplesFile;
    private File unlabledTriplesFile;

    /**
     * 
     */
    public Triplifier() {
	this.mvl = new ConcurrentLinkedQueue<MultiValue>();

	this.triplesLabeled = new ConcurrentLinkedQueue<Pair<Pair<String, Triple>, String>>();
	this.triplesUnlabeled = new ConcurrentLinkedQueue<Pair<String, Triple>>();
	this.triplesNER = new ConcurrentLinkedQueue<Pair<String, Triple>>();
	this.triplesMVL = new ConcurrentLinkedQueue<Pair<String, Triple>>();

	this.mvlFile = openFile(Configuration.getMVLFile());
	this.mvlTriplesFile = openFile(Configuration.getMVLTriples());
	this.labledTriplesFile = openFile(Configuration.getLabeledTriples());
	this.unlabledTriplesFile = openFile(Configuration.getUnlabeledTriples());
	this.nerTriplesFile = openFile(Configuration.getNERTriples());

	this.wc = new WitnessCounter();
    }

    /**
     * It opens a new file from the specified path 
     * (or create it if it does not exists).
     * 
     * @param path
     */
    private File openFile(String path){
	File file = new File(path);
	if(file.exists()){
	    file.delete();
	}
	return file;

    }


    /**
     * Given the article, extract the triples from the sentences.
     * 
     * @param article
     */
    public void extractTriples(WikiArticle article) {
	for (Map.Entry<String, List<String>> sentenceCollection : article.getSentences().entrySet()) {
	    for (String sentence : sentenceCollection.getValue()) {
		sentence = preprocess(sentence);
		sentence = replaceMultiValuedList(sentence, sentenceCollection.getKey(), article.getWikid());
		for (Triple t : createTriples(sentence)) {
		    if(isCorrectTriple(t)){
			if(t.isMVTriple()){
			    this.triplesMVL.add(Pair.make(article.getWikid(), t));
			    break;
			}
			if(t.isNERTriple()){
			    this.triplesNER.add(Pair.make(article.getWikid(), t));
			    break;
			}
			if(t.isJoinableTriple()){
			    Set<String> labels = t.getLabels();
			    if (!labels.isEmpty()){
				this.triplesLabeled.add(Pair.make(Pair.make(article.getWikid(), t), StringUtils.join(labels, ",")));
				synchronized(wc){
				    for(String label : labels)
					//wc.newPhraseAndRelation(t.getSubjectType() + t.getPhrase() + t.getObjectType(), label);
					wc.newPhraseAndRelation(t.getPhrase(), label);
				}
			    }else{
				this.triplesUnlabeled.add(Pair.make(article.getWikid(), t));
				synchronized(wc){
				    //wc.newPhraseAlone(t.getSubjectType() + t.getPhrase() + t.getObjectType());
				    wc.newPhraseAlone(t.getPhrase());
				}
			    }
			    break;
			}
		    }
		}
	    }
	}
    }



    /**
     * 
     * @param sentence
     * @return
     */
    private String replaceMultiValuedList(String sentence, String section, String wikid){
	// find entities
	String taggedEntity = "<[A-Z-][^>]*?>>";

	Pattern ENTITIES = Pattern.compile("(" + taggedEntity + "(,)\\s){3,8}" + "((,)?\\sand\\s([A-Za-z0-9 ]+\\s)?" + taggedEntity + ")?");
	Matcher m = ENTITIES.matcher(sentence);
	while(m.find()){
	    MultiValue mv = new MultiValue(m.group(0), section, wikid);
	    this.mvl.add(mv);
	    sentence = m.replaceAll("<MVL<" + mv.getCode() + ">>");
	}
	return sentence;
    }

    /**
     * 
     * @param triple
     * @return
     */
    private boolean isCorrectTriple(Triple triple){
	boolean isCorrect = true;

	// we remove triples with possessive objects ('s)
	if(triple.getPost().startsWith("'s"))
	    isCorrect = false;

	// we remove triples with objects intoduced with "that"
	if(triple.getPhrase().endsWith(" that"))
	    isCorrect = false;

	// we remove triples with phrase longer than 10
	if (triple.getPhrase().split(" ").length > 10){
	    isCorrect = false;
	}

	return isCorrect;
    }

    /**
     * Eliminate parethesis.
     * 
     * @param sentence
     * @return
     */
    private String preprocess(String sentence){
	sentence = Lector.getTextParser().removeParenthesis(sentence);
	sentence = sentence.replaceAll(",,", ",");
	sentence = sentence.replaceAll("''", "");
	sentence = sentence.replaceAll("\"", "");
	sentence = sentence.replaceAll("—", " ");
	sentence = sentence.replaceAll("=", "");
	sentence = sentence.trim();
	return sentence;
    }

    /**
     * Extract a list of triples (using consecutive entities) from the given sentence.
     * 
     * @param sentence
     * @return
     */
    private List<Triple> createTriples(String sentence) {
	List<Triple> triples = new ArrayList<Triple>();

	// find entities
	Pattern ENTITIES = Pattern.compile("<[A-Z-][^>]*?>>");
	Matcher m = ENTITIES.matcher(sentence);

	// condition
	boolean foundSubject = false;

	// entities
	String pre = ""; 
	String subject = null;
	String object = null;
	String post = "";
	String phrase = "";

	// delimiters
	int subjectStartPos = 0;
	int subjectEndPos = 0;
	int objectStartPos = 0;
	int objectEndPos = 0;


	while(m.find()){
	    if (!foundSubject) {
		foundSubject = true;
		subject = m.group(0);
		subjectStartPos = m.start(0);
		subjectEndPos = m.end(0);
	    }else{
		object = m.group(0);
		objectStartPos = m.start(0);
		objectEndPos = m.end(0);

		pre = getWindow(replaceEntities(sentence.substring(Math.max(subjectStartPos-200, 0), subjectStartPos).trim()), 3, "pre");
		post = getWindow(replaceEntities(sentence.substring(objectEndPos, Math.min(sentence.length(), objectEndPos + 200)).trim()), 3, "post");
		phrase = sentence.substring(subjectEndPos, objectStartPos).trim();

		Triple t = new Triple(preprocess(pre), subject, preprocess(phrase), object, preprocess(post));
		triples.add(t);

		// change subject now for the next triple
		subject = object;
		subjectStartPos = objectStartPos;
		subjectEndPos = objectEndPos;
	    }
	}
	return triples;
    }

    /**
     * 
     * @param spanNCharacters
     * @param N
     * @param direction
     * @return
     */
    private String getWindow(String spanNCharacters, int N, String direction){
	// number of elements to include in the window
	int elems = N;

	// split by space
	String[] tokens = spanNCharacters.split(" ");

	StringBuffer buff = new StringBuffer();
	String window = "";
	if (tokens.length > 0){
	    if (direction.equals("pre")){
		// pre-window: start from the end
		for (int i = tokens.length-1; i>0 && elems>0; i--){
		    buff.append(tokens[i] + " ");
		    elems--;
		}
		window = reverseWords(buff.toString().trim());
	    }
	    else if (direction.equals("post")){
		// post-window: start from the beginning
		for (int i = 0; i<tokens.length && elems>0; i++){
		    buff.append(tokens[i] + " ");
		    elems--;
		}
		window = buff.toString().trim();
	    }
	}
	return window;
    }

    /**
     * 
     * @param s
     * @return
     */
    private String reverseWords(String s) {
	if (s == null || s.length() == 0)
	    return "";
	// split to words by space
	String[] arr = s.split(" ");
	StringBuilder sb = new StringBuilder();
	for (int i = arr.length - 1; i >= 0; --i) {
	    if (!arr[i].equals("")) {
		sb.append(arr[i]).append(" ");
	    }
	}
	return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
    }

    /**
     * 
     * @param spanNCharacters
     * @return
     */
    private String replaceEntities(String spanNCharacters){
	// find entities
	Pattern ENTITIES = Pattern.compile("<[A-Z-][^>]*?>>");
	Matcher m = ENTITIES.matcher(spanNCharacters);
	StringBuffer buf = new StringBuffer();
	while(m.find()){
	    m.appendReplacement(buf, "<EN>");
	}
	m.appendTail(buf);
	return buf.toString();
    }


    /**
     * Used for testing.
     * 
     * @return
     */
    public String printEverything(){
	StringBuffer sb = new StringBuffer();

	sb.append("\n");sb.append("\n");
	sb.append(" ------  MULTIVALUE TRIPLES ------ ");
	sb.append("\n");
	for (Pair<String, Triple> t : this.triplesMVL){
	    sb.append(t.key + "\t" + t.value.toString());
	    sb.append("\n");
	}
	this.triplesMVL.clear();
	
	sb.append("\n");sb.append("\n");
	sb.append(" ------  LABELED TRIPLES ------ ");
	sb.append("\n");
	for (Pair<Pair<String, Triple>, String> t : this.triplesLabeled){
	    sb.append(t.key.key + "\t" + t.key.value.toString() + "\t" + t.value);
	    sb.append("\n");
	}
	this.triplesLabeled.clear();
	
	sb.append("\n");sb.append("\n");
	sb.append(" ------  UNLABELED TRIPLES ------ ");
	sb.append("\n");
	for (Pair<String, Triple> t : this.triplesUnlabeled){
	    sb.append(t.key + "\t" + t.value.toString());
	    sb.append("\n");
	}
	this.triplesUnlabeled.clear();

	sb.append("\n");sb.append("\n");
	sb.append(" ------  NER TRIPLES ------ ");
	sb.append("\n");
	for (Pair<String, Triple> t : this.triplesNER){
	    sb.append(t.key + "\t" + t.value.toString());
	    sb.append("\n");
	}
	this.triplesNER.clear();
	
	return sb.toString();
    }

    /**
     * 
     */
    public void flushEverything(){
	try{
	    BufferedWriter bMVL = new BufferedWriter(new FileWriter(this.mvlFile, true));
	    for (MultiValue mv : this.mvl){
		bMVL.write(mv.toString());
		bMVL.write("\n");
	    }
	    bMVL.close();
	    this.mvl.clear();

	    BufferedWriter bTRIPLESMVL = new BufferedWriter(new FileWriter(this.mvlTriplesFile, true));
	    for (Pair<String, Triple> t : this.triplesMVL){
		bTRIPLESMVL.write(t.key + "\t" + t.value.toString());
		bTRIPLESMVL.write("\n");
	    }
	    bTRIPLESMVL.close();
	    this.triplesMVL.clear();

	    BufferedWriter bLABELED = new BufferedWriter(new FileWriter(this.labledTriplesFile, true));
	    for (Pair<Pair<String, Triple>, String> t : this.triplesLabeled){
		bLABELED.write(t.key.key + "\t" + t.key.value.toString() + "\t" + t.value);
		bLABELED.write("\n");
	    }
	    bLABELED.close();
	    this.triplesLabeled.clear();

	    BufferedWriter bUNLABELED = new BufferedWriter(new FileWriter(this.unlabledTriplesFile, true));
	    for (Pair<String, Triple> t : this.triplesUnlabeled){
		bUNLABELED.write(t.key + "\t" + t.value.toString());
		bUNLABELED.write("\n");
	    }
	    bUNLABELED.close();
	    this.triplesUnlabeled.clear();

	    BufferedWriter bNER = new BufferedWriter(new FileWriter(this.nerTriplesFile, true));
	    for (Pair<String, Triple> t : this.triplesNER){
		bNER.write(t.key + "\t" + t.value.toString());
		bNER.write("\n");
	    }
	    bNER.close();
	    this.triplesNER.clear();  

	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    /**
     * 
     */
    public void printStatistics(){
	try {
	    this.wc.printStatistics();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
