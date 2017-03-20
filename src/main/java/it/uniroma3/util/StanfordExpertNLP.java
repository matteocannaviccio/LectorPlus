package it.uniroma3.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
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
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.process.WordToSentenceProcessor;
import it.uniroma3.entitydetection.PatternComparator;
/**
 * 
 * @author matteo
 *
 */
public class StanfordExpertNLP {
    private AbstractSequenceClassifier<CoreLabel> classifier;
    private TokenizerFactory<CoreLabel> tokenizerFactory;
    private Map<String, String> mapping;

    /**
     * 
     */
    public StanfordExpertNLP(){
	
	/********* this code only makes all writes to the System.err stream silent to avoid the print "Loading classifier ... " *****/
	PrintStream err = System.err;
	//PrintStream out = System.out;
	System.setErr(new PrintStream(new OutputStream() {public void write(int b) {}}));
	//System.setOut(new PrintStream(new OutputStream() {public void write(int b) {}}));
	this.classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
	//this.classifier = CRFClassifier.getClassifierNoExceptions("edu/stanford/nlp/models/ner/english.muc.7class.distsim.crf.ser.gz");
	this.tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");
	System.setErr(err);
	//System.setOut(out);
	/*** and then set everything back to its original state afterwards ***/

	this.mapping = new HashMap<String, String>();
	mapping.put("-LSB-", "[");
	mapping.put("-RSB-", "]");
	mapping.put("-LCB-", "{");
	mapping.put("-RCB-", "}");
	mapping.put("-LRB-", "(");
	mapping.put("-RRB-", ")");
    }

    /**
     * 
     * @param block
     * @return
     */
    private List<String> blockToSentencesImproved(String block){
	List<String> sentenceList = new ArrayList<String>();
	List<CoreLabel> tokens = tokenizerFactory.getTokenizer(new StringReader(block)).tokenize();

	/*
	 *  Group the tokens by sentences
	 */
	List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>().process(tokens);
	int end;
	int start = 0;
	for (List<CoreLabel> sentence: sentences) {
	    end = sentence.get(sentence.size()-1).endPosition();
	    sentenceList.add(block.substring(start, end).trim());
	    start = end;
	}
	return sentenceList;
    }

    /**
     * 
     * @param block
     * @return
     */
    private String annotateBlock(String block){
	// classify the sentence = assign a label to each token
	List<List<CoreLabel>> out = classifier.classify(block);
	for (List<CoreLabel> label : out) {
	    ConcurrentSkipListSet<Pair<String, String>> tags = 
		    new ConcurrentSkipListSet<Pair<String, String>>(new PatternComparator());
	    StringBuffer currentToken = new StringBuffer();
	    String currentLabel = "O";
	    for (CoreLabel token : label) {
		String word = token.originalText();
		if(mapping.containsKey(word))
		    word = mapping.get(word);
		String type = token.get(CoreAnnotations.AnswerAnnotation.class);
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
	    // apply regex
	    for(Pair<String, String> regex : tags){
		block = block.replaceAll(regex.key, regex.value);
	    }
	}
	return block;
    }

    /**
     * 
     * @return
     */
    public List<String> processBlock(String block){
	String annotatedBlock = annotateBlock(block);
	List<String> sentences = blockToSentencesImproved(annotatedBlock);
	return sentences;
    }


    public static void main(String[] args){
	String text = "Albert of Prussia (17 May 149020 March 1568) was the 37th SE-ORG<Grand_Masters_of_the_Teutonic_Knights> of the ''SE-ORG<Teutonic_Knights>'', who after converting to SE-ORG<Lutheranism>, became the first monarch of the SE-ORG<Duchy_of_Prussia>, the secularized state that emerged from the former SE-ORG<State_of_the_Teutonic_Order>. Albert was the first European ruler to establish Lutheranism, and thus SE-ORG<Protestantism>, as the official state religion of his lands. He proved instrumental in the political spread of Protestantism in its early stage, ruling the Prussian lands for nearly six decades (1510–1568).\nA member of the SE-ORG<Principality_of_Ansbach> branch of the SE-ORG<House_of_Hohenzollern>, Albert became Grand Master, where his skill in political administrator and leadership ultimately succeeded in reversing the decline of the Teutonic Order. But Albert, who was sympathetic to the demands of SE-ORG<Martin_Luther>, rebelled against the SE-ORG<Catholic_Church> and the SE-ORG<Holy_Roman_Empire> by converting the Teutonic state into a Protestant and hereditary realm, the Duchy of Prussia, for which he paid homage to his uncle, the SE-ORG<Sigismund_I_the_Old>. That arrangement was confirmed by the SE-ORG<Treaty_of_Kraków> in 1525. Albert pledged a personal oath to the King and in return was invested with the duchy for himself and his heirs.\nAlbert's rule in Prussia was fairly prosperous. Although he had some trouble with the peasantry, the confiscation of the lands and treasures of the Catholic Church enabled him to propitiate the nobles and provide for the expenses of the newly-established Prussian court. He was active in imperial politics, joining the SE-ORG<League_of_Torgau> in 1526, and acted in unison with the Protestants in plotting to overthrow Emperor SE-ORG<Charles_V,_Holy_Roman_Emperor> after the issue of the SE-ORG<Augsburg_Interim> in May 1548. Albert established schools in every town and founded SE-ORG<University_of_Königsberg> in 1544. He promoted culture and arts, patronising the works of SE-ORG<Erasmus_Reinhold> and SE-ORG<Caspar_Hennenberger>. During the final years of his rule, Albert was forced to raise taxes instead of further confiscating now-depleted church lands, causing peasant rebellion. The intrigues of the court favourites SE-ORG<Johann_Funck> and SE-ORG<Paul_Skalić> also led to various religious and political disputes. Albert spent his final years virtually deprived of power and died at SE-ORG<Gvardeysk> on 20 March 1568. His son, SE-ORG<Albert_Frederick,_Duke_of_Prussia>, succeeded him as Duke of Prussia.\nAlbert's dissolution of the Teutonic State caused the founding of the Duchy of Prussia, paving the way for the rise of the House of Hohenzollern. He is therefore often seen as the father of the Prussian nation, and even as indirectly responsible for the unification of Germany.";
	StanfordExpertNLP expert = new StanfordExpertNLP();
	expert.processBlock(text).stream().forEach(System.out::println);
    }



}
