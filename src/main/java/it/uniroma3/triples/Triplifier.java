package it.uniroma3.triples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.bean.WikiArticle;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.DBlite;
import it.uniroma3.util.Pair;

/**
 * This module extracts triples from the articles and write them in DB.
 * 
 * Before to extract the triple we pre-process the sentence and perform
 * some text filtering such as:
 * - applying place-holders
 * - removing parenthesis
 * - removing triples where the object is followed by 's
 * - removing triples with phrases that end with "that"
 * 
 * @author matteo
 *
 */
public class Triplifier {

    private DBlite db;
    private Map<String, Integer> stats;
    private Queue<Pair<WikiTriple, String>> labeled_triples;
    private Queue<WikiTriple> unlabeled_triples;
    private PlaceholderFilter placeholderFilter;

    /**
     * 
     */
    public Triplifier() {
	db = new DBlite("lector.db");
	// uncomment for new execution
	//db.createDB();
	stats = new ConcurrentHashMap<String, Integer>();
	labeled_triples = new ConcurrentLinkedQueue<Pair<WikiTriple, String>>();
	unlabeled_triples = new ConcurrentLinkedQueue<WikiTriple>();
	placeholderFilter = new PlaceholderFilter();
    }

    /**
     * Given the article, iterate all the sentences and extract the triples.
     * We eliminate the content inside parenthesis and then we dispatch
     * each triple in the right db using its type as a label.
     * 
     * @param article
     */
    public void extractTriples(WikiArticle article) {
	try{
	    for (Map.Entry<String, List<String>> sentenceCollection : article.getSentences().entrySet()) {
		for (String sentence : sentenceCollection.getValue()) {
		    sentence = Lector.getTextParser().removeParenthesis(sentence);
		    sentence = replaceMultiValuedList(sentence, sentenceCollection.getKey(), article.getWikid());
		    for (WikiTriple t : createTriples(article, sentence)) {
			processTriple(t);
		    }
		}
	    }
	}catch(Exception e){
	    e.printStackTrace();
	}
    }
    
    /**
     * Dispatch the triple in the DB, with the right label on it.
     * 
     * @param t
     */
    public void processTriple(WikiTriple t){
	switch(t.getType()){

	// it is a joinable triple only if both the subject
	// and object are wiki entities with a type
	case JOINABLE: 
	    Set<String> labels = t.getLabels();
	    if (!labels.isEmpty()){
		for (String relation : t.getLabels()){
		    labeled_triples.add(Pair.make(t, relation));
		    String key = t.getPhrase() + "\t" + t.getSubjectType()+ "\t" +  t.getObjectType()+ "\t" + relation;
		    if (!stats.containsKey(key))
			stats.put(key, 0);
		    stats.put(key, stats.get(key) + 1);

		}
	    }else{
		unlabeled_triples.add(t);
	    }
	    break;

	case MVL:
	case NER_BOTH:
	case NER_OBJ:
	case NER_SBJ:
	case JOINABLE_NOTYPE_BOTH:
	case JOINABLE_NOTYPE_SBJ:
	case JOINABLE_NOTYPE_OBJ:
	case DROP:
	    unlabeled_triples.add(t);
	    break;

	}
    }

    /**
     * Extract a list of triples (using consecutive entities) from the given sentence.
     * 
     * @param sentence
     * @return
     */
    public List<WikiTriple> createTriples(WikiArticle article, String sentence) {
	List<WikiTriple> triples = new ArrayList<WikiTriple>();

	// find entities
	Pattern ENTITIES = Pattern.compile("<[A-Z-][^>]*?>>");
	Matcher m = ENTITIES.matcher(sentence);

	// initial condition
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

		pre = placeholderFilter.preprocess(pre);
		post = placeholderFilter.preprocess(post);
		phrase = placeholderFilter.preprocess(phrase);

		if (!phrase.equals("")){
		    WikiTriple t = new WikiTriple(article.getWikid(), pre, subject, phrase, object, post);
		    triples.add(t);
		}

		// change subject now for the next triple
		subject = object;
		subjectStartPos = objectStartPos;
		subjectEndPos = objectEndPos;
	    }
	}

	return filterUncorrectTriple(triples);
    }
    
    /**
     * 
     * @param triple
     * @return
     */
    private List<WikiTriple> filterUncorrectTriple(List<WikiTriple> triples){
	List<WikiTriple> filteredTriples = new ArrayList<WikiTriple>(triples.size());
	boolean isCorrect;
	for (WikiTriple t : triples){
	    isCorrect = true;
	    // we remove triples with possessive objects ('s)
	    if(t.getPost().startsWith("'s"))
		isCorrect = false;
	    // we remove triples with objects intoduced with "that"
	    if(t.getPhrase().endsWith(" that"))
		isCorrect = false;
	    // we remove triples with phrase longer than 10
	    if (t.getPhrase().split(" ").length > 10)
		isCorrect = false;
	    if (isCorrect)
		filteredTriples.add(t);
	}
	return filteredTriples;
    }
    
    /**
     * Replace all the MVL lists that are present in the sentence.
     * 
     * @param sentence
     * @return
     */
    private String replaceMultiValuedList(String sentence, String section, String wikid){
	Matcher m = WikiMVL.getRegexMVL().matcher(sentence);
	while(m.find()){
	    WikiMVL mv = new WikiMVL(m.group(0), section, wikid);
	    db.insertMVList(mv);
	    sentence = m.replaceAll(Matcher.quoteReplacement("<MVL<" + mv.getCode() + ">>"));
	}
	return sentence;
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
     * 
     */
    public void endConnection(){
	db.closeConnection();
    }

    /**
     * 
     */
    public void updateBlock(){
	for (Pair<WikiTriple, String> pair : this.labeled_triples){
	    db.insertLabeledTriple(pair.key, pair.value);
	}

	for(WikiTriple t : this.unlabeled_triples){
	    db.insertUnlabeledTriple(t);
	}

	for (Map.Entry<String, Integer> entry : this.stats.entrySet()){
	    String[] fields = entry.getKey().split("\t");
	    db.updateModelStats(fields[0], fields[1], fields[2], fields[3], entry.getValue());
	}

	this.unlabeled_triples.clear();
	this.labeled_triples.clear();
	this.stats.clear();
    }


}
