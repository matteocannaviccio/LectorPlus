package it.uniroma3.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import it.uniroma3.entitydetection.PatternComparator;
/**
 * 
 * @author matteo
 *
 */
public class StanfordExpertNLP {

    private AbstractSequenceClassifier<CoreLabel> classifier;
    private Map<String, String> mapping;


    /**
     * 
     */
    public StanfordExpertNLP(){
	/********* this code only makes all writes to the System.err stream silent to avoid the print "Loading classifier ... " *****/
	//PrintStream out = System.out;
	//System.setOut(new PrintStream(new OutputStream() {public void write(int b) {}}));

	this.classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");

	this.mapping = new HashMap<String, String>();
	mapping.put("-LSB-", "[");
	mapping.put("-RSB-", "]");
	mapping.put("-LCB-", "{");
	mapping.put("-RCB-", "}");
	mapping.put("-LRB-", "(");
	mapping.put("-RRB-", ")");

	/*** and then set everything back to its original state afterwards ***/
	//System.setOut(out);
    }

    /**
     * 
     * @return
     */
    public String splitAndAnnotateSentences(String block){
	List<List<CoreLabel>> out = classifier.classify(block);	// classify the sentence = assign a label to each token
	
	for (List<CoreLabel> label : out) {
	    
	    ConcurrentSkipListSet<Pair<String, String>> tags = new ConcurrentSkipListSet<Pair<String, String>>(new PatternComparator());
	    StringBuffer currentToken = new StringBuffer();
	    String currentLabel = "O";
	    
	    for (CoreLabel token : label) {
		String word = token.originalText();
		if(mapping.containsKey(word))
		    word = mapping.get(word);
		String type = token.get(CoreAnnotations.AnswerAnnotation.class);

		//System.out.println(word + "\t" + type);
		// Build annotations

		if (!type.equals("O")){
		    if (Character.isLetterOrDigit(word.charAt(0)))
			currentToken = currentToken.append(" " + word);
		    else
			currentToken = currentToken.append(word);
		}else if(!currentLabel.equals("O")){

		    // add regex and replacement in the map

		    String regex = "(?![A-Z-]+<)\\b" + Pattern.quote(currentToken.toString().trim()) + "\\b(?![^<]*?>)";
		    String replacement = Matcher.quoteReplacement(currentLabel + "<" + currentToken.toString().trim() + ">");
		    tags.add(Pair.make(regex, replacement));

		    currentToken = new StringBuffer();
		}else{
		    currentToken = new StringBuffer();
		}
		currentLabel = type;
	    }
	    /*
	     * Apply all the annotations (regex with the replacements).
	     */
	    for(Pair<String, String> regex : tags){
		block = block.replaceAll(regex.key, regex.value);
	    }
	}
	return block;
    }
    
    public static void main(String[] args){
	String text = "PE-ALIAS<Alexander_Kerensky> was a Russian lawyer and Alexander politician who served as the SE-NAME<Minister_of_Justice> in the newly formed SE-ORG<Russian_Provisional_Government>, as SE-ORG<Minister_of_War>, and second SE-ORG<Prime_Minister_of_Russia> of the between July and November 1917. A leader of the moderate-socialist SE-ORG<Trudoviks> faction of the SE-ORG<Socialist_Revolutionary_Party>, Kerensky was a key political figure in the SE-ORG<Russian_Revolution> of 1917. On 7 November, his government was overthrown by the SE-ORG<Vladimir_Lenin>-led SE-ORG<Bolshevik>s in the SE-ORG<October_Revolution>. PE-PRO<Alexander_Kerensky> spent the remainder of his life in exile, in Paris and New York City, but was buried in London.";
	StanfordExpertNLP expert = new StanfordExpertNLP();
	System.out.println(expert.splitAndAnnotateSentences(text));
    }



}
