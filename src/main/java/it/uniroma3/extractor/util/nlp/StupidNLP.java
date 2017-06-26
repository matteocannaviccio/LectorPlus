package it.uniroma3.extractor.util.nlp;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
public class StupidNLP {
    
    /**
     * 
     */
    private static final Set<String> ABBREVIATIONS = new HashSet<String>(Arrays.asList(new String[]{
	        "Dr.", "Prof.", "Mr.", "Mrs.", "Ms.", "Jr.","Ph.D.", "Sr.", "feat.", "Inc."}));

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
	for (String w : ABBREVIATIONS) {
	    if (sentence.contains(w)) {
		return true;
	    }
	}
	return false;
    }

}
