package it.uniroma3.entitydetection;

import java.util.Comparator;
/**
 * 
 * @author matteo
 *
 */
public class PatternComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
	o1 = o1.replaceAll("\\(.*?\\)", "");
	o2 = o2.replaceAll("\\(.*?\\)", ""); 
	if (o1 == null || o2 == null)
	    return 0;
	return o2.length() - o1.length();
    }


}
