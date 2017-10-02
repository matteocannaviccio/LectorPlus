package it.uniroma3.main.pipeline.entitydetection.dbsp;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;

public class TestDBpediaSpotlight {


    public static void main(String[] args){
	Configuration.init(args);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
	Configuration.updateParameter("language", "fr");
	Configuration.updateParameter("useSpotlight", "yes");
	Configuration.printFullyDetails();
	System.out.println("\n------------------------------------");
	System.out.println("Testing DBPedia Spotlight");
	System.out.println("------------------------------------");
	WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	testSpotlight(wikiLang);

    }

    private static void testSpotlight(WikiLanguage wikiLang) {
	Lector.init("AP,ED");
	String text = "En 1937, Aldous Huxley s'installe à <SE-ORG<Hollywood>> en <SE-AUG<États-Unis>> avec sa femme et Son ami.";
	System.out.println(Lector.getDBSpot().annotateText(text, "Aldous_Huxley"));
	Lector.close();
    }
}
