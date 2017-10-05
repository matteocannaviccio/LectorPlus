package it.uniroma3.main.pipeline.entitydetection.dbsp;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;

public class TestDBpediaSpotlight {


  public static void main(String[] args) {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
    Configuration.updateParameter("language", "en");
    Configuration.updateParameter("useSpotlight", "yes");
    Configuration.printFullyDetails();
    System.out.println("\n------------------------------------");
    System.out.println("Testing DBPedia Spotlight");
    System.out.println("------------------------------------");
    WikiLanguage wikiLang =
        new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
    testSpotlight(wikiLang);

  }

  private static void testSpotlight(WikiLanguage wikiLang) {
    Lector.init("AP,ED");
    String text =
        "<<PE-TITLE><Azerbaijan><Azerbaijan>> (; Azərbaycan), officially the <<PE-ALIAS><Azerbaijan><Republic of "
            + "<<PE-TITLE><Azerbaijan><Azerbaijan>>>> (Azərbaycan Respublikası), is a country in the <<SE-ORG><Transcaucasia><South Caucasus>> region, "
            + "situated at the crossroads of <<SE-DBPS><Western_Asia><Southwest Asia>>"
            + " and <<SE-ORG><Southeast_Europe><Southeastern Europe>> and <<SE-ORG><Barack_Obama><Barack Obama>> and Barack Obama and <<SE-ORG><Barack_Obama><Jonh and Barack Obama>>";
    System.out.println(Lector.getDBSpot().annotateText(text, "Azerbaijan"));
    Lector.close();
  }
}
