package it.uniroma3.parser;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class WikiParserTest {

    public static String testWikilik1 = "test 1 - a [[wikilink|Jh]] molecule that is described as";
    public static String testWikilik2 = "test 2 - a [[wikilink|@A]] molecule that is described as";
    public static String testWikilik3 = "test 3 - a [[wikilink|Rend[ered]]] molecule that is described as";
    public static String testWikilik4 = "test 4 - a [[[wikilink|Rendered]]] molecule that is described as";
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
    }

    public static void main(String[] args){
	WikiParserTest test = new WikiParserTest();
	test.test();
    }

}
