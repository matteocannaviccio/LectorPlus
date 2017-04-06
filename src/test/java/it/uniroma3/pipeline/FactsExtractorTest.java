package it.uniroma3.pipeline;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class FactsExtractorTest {

   private static FactsExtractor fe;
    
    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init("/Users/matteo/Desktop/data/config.properties");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	fe = new FactsExtractor("src/test/resources/gisele_augmented.json");
    }
    
    @Test
    public void factsExtractorTest(){
	fe.process(true);
    }
}
