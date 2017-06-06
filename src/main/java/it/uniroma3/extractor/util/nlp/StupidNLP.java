package it.uniroma3.extractor.util.nlp;

import java.text.BreakIterator;
import java.util.LinkedList;
import java.util.List;

public class StupidNLP {
    
    /**
     * 
     * @param text
     * @return
     */
    public static List<String> splitSentence(String text){
	List<String> sentences = new LinkedList<String>();
	BreakIterator bi = BreakIterator.getSentenceInstance();
	bi.setText(text);
	int index = 0;
	while (bi.next() != BreakIterator.DONE) {
	    String sentence = text.substring(index, bi.current());
	    sentences.add(sentence);
	    index = bi.current();
	}
	return sentences;
    }

}
