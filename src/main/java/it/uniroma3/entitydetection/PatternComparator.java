package it.uniroma3.entitydetection;

import java.util.Comparator;

import it.uniroma3.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class PatternComparator implements Comparator<Pair<String, String>> {

    @Override
    public int compare(Pair<String, String> p1, Pair<String, String> p2) {
	String o1 = p1.key.replaceAll("\\(.*?\\)", "");
	String o2 = p2.key.replaceAll("\\(.*?\\)", ""); 
	if (o1.length() > o2.length()) {
	    return -1;
	} else if (o1.length() < o2.length()) {
	    return 1;
	}
	return o1.compareTo(o2);
    }


}
