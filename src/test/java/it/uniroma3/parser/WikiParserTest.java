package it.uniroma3.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import it.uniroma3.model.WikiLanguage;

public class WikiParserTest {

    public static String testWikilik1 = "test 1 - a [[wikilink|rendered]] molecule that is described as";
    public static String testWikilik2 = "test 2 - a [[wikilink|]] molecule that is described as";
    public static String testWikilik3 = "test 3 - a [[wikilink|rend[ered]]] molecule that is described as";
    public static String testWikilik4 = "test 4 - a [[[wikilink|rendered]]] molecule that is described as";
    public static String testWikilik5 = "test 5 - a [wikilink|[rend[ered]]]] molecule that is described as";
    public static String testWikilik6 = "test 6 - a [[Sunnooo))|SunnO))]] molecule that is described as";
    public static List<String> testCase = new ArrayList<String>();

    public WikiParserTest(){
	testCase.add(testWikilik1);
	testCase.add(testWikilik2);
	testCase.add(testWikilik3);
	testCase.add(testWikilik4);
	testCase.add(testWikilik5);
	testCase.add(testWikilik6);
    }


    @Test
    public void test() {
	WikiParser parser = new WikiParser(new WikiLanguage("en"));

	// composite wikilinks, e.g. [[Barack_Obama|Obama]]
	Pattern LINKS1 = Pattern.compile("(?<=\\[)?(?<=\\[\\[)([^\\[\\]]+)\\|([^\\]\\[]+)?(?=\\]\\])(?=\\])?");
	for(String test: testCase){
	    System.out.print(test.split(" - ")[0] + "\t\t");
	    for (Map.Entry<String, Set<String>> entry : parser.harvestCompositeWikilinks(test, LINKS1).entrySet()){
		for (String rendered : entry.getValue())
		    System.out.print(entry.getKey() + "\t\t" + rendered);
	    }
	    System.out.println();
	}
    }

    public static void main(String[] args){
	WikiParserTest test = new WikiParserTest();
	test.test();
    }

}
