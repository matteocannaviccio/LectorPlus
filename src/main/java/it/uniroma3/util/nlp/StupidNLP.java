package it.uniroma3.util.nlp;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
public class StupidNLP {

    /**
     * 
     * @param document
     * @return
     */
    public static List<String> splitSentence(String document) {
	List<String> sentenceList = new ArrayList<String>();
	BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
	bi.setText(document);
	int start = bi.first();
	int end = bi.next();
	int tempStart = start;
	while (end != BreakIterator.DONE) {
	    String sentence = document.substring(start, end);
	    if (!hasAbbreviation(sentence)) {
		sentence = document.substring(tempStart, end);
		tempStart = end;
		sentenceList.add(sentence);
	    }
	    start = end; 
	    end = bi.next();
	}
	return sentenceList;
    }

    /**
     * 
     * @param sentence
     * @return
     */
    private static boolean hasAbbreviation(String sentence) {
	if (sentence == null || sentence.isEmpty())
	    return false;
	if (Pattern.matches("\\sDr.\\s||\\sProf.\\s||\\sMr.\\s||\\sMrs.\\s||\\sMs.\\s||"
		+ "\\sJr.\\s||\\sPh.D.\\s||\\sSr.\\s||\\sfeat.\\s||\\sInc.\\s", sentence))
	    return true;
	return false;
    }

}
