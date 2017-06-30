package it.uniroma3.extractor.util.nlp;

import java.util.Arrays;
import java.util.HashSet;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;

public class TestDBpediaSpotlight {


    public static void main(String[] args){
	Configuration.init(args);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "fr");
	Configuration.updateParameter("useSpotlight", "TRUE");
	Configuration.printFullyDetails();
	System.out.println("\n------------------------------------");
	System.out.println("Testing DBPedia Spotlight");
	System.out.println("------------------------------------");
	WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	testSpotlight(wikiLang);

    }

    private static void testSpotlight(WikiLanguage wikiLang) {
	Lector.init(wikiLang, new HashSet<String>(Arrays.asList("AP,ED".split(","))));
	String text = "En 1937, <PE-SUBTITLE<Aldous_Huxley>> s'installe à <SE-ORG<Hollywood>> en <SE-AUG<États-Unis>> avec sa femme et Son ami.";
	System.out.println(Lector.getDBSpot().annotateText(text, "Aldous_Huxley"));
    }
}
