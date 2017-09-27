package it.uniroma3.main.pipeline.entitydetection;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.main.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class PatternComparator implements Comparator<Pair<String, String>> {

    @Override
    public int compare(Pair<String, String> p1, Pair<String, String> p2) {
	String o1 = normalizePattern(p1.key);
	String o2 = normalizePattern(p2.key);
	if (o1.length() > o2.length()) {
	    return -1;
	} else if (o1.length() < o2.length()) {
	    return 1;
	}
	return o1.compareTo(o2);
    }

    /**
     * 
     * @param regex
     * @return
     */
    public String normalizePattern(String regex){
	Pattern REGEXCONTENT = Pattern.compile("^.*\\Q(.+)\\E.*$");
	Matcher m = REGEXCONTENT.matcher(regex);
	if(m.find()){
	    if (m.group(1) != null)
		regex = m.replaceAll(m.group(1));
	}
	return regex;

    }


}
