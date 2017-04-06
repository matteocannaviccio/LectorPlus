package it.uniroma3.pipeline;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiLanguage;

public class EntityDetectionTest {


    private static EntityDetection ed;
     
     @BeforeClass
     public static void runOnceBeforeClass() {
 	Configuration.init("/Users/matteo/Desktop/data/config.properties");
 	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
 	ed = new EntityDetection("src/test/resources/gisele_parsed.json", "src/test/resources/gisele_augmented.json");
     }
     
     @Test
     public void factsExtractorTest(){
 	ed.process(true);
     }
}
