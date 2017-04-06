package it.uniroma3.util;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.util.nlp.StanfordNLP;

public class StanfordExpertNLPTest {

    private static StanfordNLP expert;
    
    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init("/Users/matteo/Desktop/data/config.properties");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	expert = new StanfordNLP();
    }
    
    @Test
    public void processBlockTest(){
	String block = "The history of the <PE-ALIAS<Apache_Software_Foundation>> come from London and is linked to the <SE-AUG<Apache_HTTP_Server>>, "
		+ "development beginning in February 1993. The date of the first Scotland Linux leadership on <SE-ORG<Mac>>.";
	System.out.println(expert.processBlock(block));
    }

}
