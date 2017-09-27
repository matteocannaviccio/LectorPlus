package it.uniroma3.main.util.nlp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import it.uniroma3.config.Lector;
import it.uniroma3.main.pipeline.entitydetection.PatternComparator;
import it.uniroma3.main.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class StanfordNLP {
    private AbstractSequenceClassifier<CoreLabel> classifier;
    private Map<String, String> mapping;

    /**
     * 
     */
    public StanfordNLP(){
	/********* this code only makes all writes to the System.err stream silent to avoid the print "Loading classifier ... " *****/
	PrintStream err = System.err;
	System.setErr(new PrintStream(new OutputStream() {@Override
	public void write(int b) {}}));
	switch(Lector.getWikiLang().getLang()){
	case en:
	    this.classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
	    break;
	    
	case es:
	    this.classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
	    break;
	    
	default:
	    System.out.println("No Stanford classifier for this language.");
	    System.exit(1);
	    break;
	}
	System.setErr(err);
	/*** and then set everything back to its original state afterwards ***/

	this.mapping = new HashMap<String, String>();
	mapping.put("-LSB-", "[");
	mapping.put("-RSB-", "]");
	mapping.put("-LCB-", "{");
	mapping.put("-RCB-", "}");
	mapping.put("-LRB-", "(");
	mapping.put("-RRB-", ")");
    }


    public List<String> processBlock(String block){
	List<String> sentenceList = new ArrayList<String>();
	/*
	 * classify the sentence:
	 * (1) assign a label to each token
	 * (2) split in multiple sentences
	 */
	List<List<CoreLabel>> labeledSentences = classifier.classify(block);
	int start = 0;
	int end = 0;

	// for each sentence ..
	for (List<CoreLabel> sentence : labeledSentences) {

	    // initialize a tags set
	    List<Pair<String, String>> tags = new ArrayList<Pair<String, String>>();

	    // get the first sentence's text
	    end = sentence.get(sentence.size()-1).endPosition();
	    String sentenceText = block.substring(start, end).trim();
	    start = end;

	    // for each token ...
	    StringBuffer currentToken = new StringBuffer();
	    String currentLabel = "O";
	    for (CoreLabel token : sentence) {
		String word = token.originalText();
		if(mapping.containsKey(word))
		    word = mapping.get(word);
		String label = token.get(CoreAnnotations.AnswerAnnotation.class);

		//System.out.println(word + "\t" + label);

		if (!label.equals("O")){
		    if (Character.isLetterOrDigit(word.charAt(0)))
			currentToken = currentToken.append(" " + word);
		    else
			currentToken = currentToken.append(word);

		}else if(!currentLabel.equals("O")){
		    // add regex and replacement in the map
		    String regex = "(?!<[A-Z-]+<)\\b" + Pattern.quote(currentToken.toString().trim()) + "\\b(?![^<]*?>>)";
		    String replacement = Matcher.quoteReplacement("<" + currentLabel + "<" + currentToken.toString().trim() + ">>");
		    tags.add(Pair.make(regex, replacement));
		    //System.out.println(Pair.make(regex, replacement));
		    currentToken = new StringBuffer();
		}else{
		    currentToken = new StringBuffer();
		}
		currentLabel = label;
	    }

	    /*
	     * Sort them.
	     */
	    Collections.sort(tags, new PatternComparator()); 

	    // apply regex!

	    for(Pair<String, String> regex : tags){
		try {
		    sentenceText = applyRegex(sentenceText, regex.value, regex.key);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	    sentenceList.add(sentenceText.replace("\n", "").replace("\r", "").replace("\t", ""));

	}
	return sentenceList;
    }

    /**
     * 
     * @param article
     * @param sentence
     * @param replacement
     * @param pattern
     * @return
     * @throws Exception
     */
    private String applyRegex(String sentence, String replacement, String pattern) throws Exception{
	StringBuffer tmp = new StringBuffer();
	try{ 
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(sentence);

	    while (m.find()){
		m.appendReplacement(tmp, Matcher.quoteReplacement(replacement));
	    }
	    m.appendTail(tmp);

	}catch(Exception e){
	    e.printStackTrace();
	    throw new Exception();
	}
	return tmp.toString();
    }
}
