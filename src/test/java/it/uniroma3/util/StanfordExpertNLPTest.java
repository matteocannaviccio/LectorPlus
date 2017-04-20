package it.uniroma3.util;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiLanguage;

public class StanfordExpertNLPTest {
    
    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Test
    public void processBlockTest(){
	String block = "The history of the <PE-ALIAS<Apache_Software_Foundation>> come from London and is linked to the <SE-AUG<Apache_HTTP_Server>>, "
		+ "development beginning in February 1993. The date of the first Scotland Linux leadership on <SE-ORG<Mac>>.";
	System.out.println(Lector.getNLPExpert().processBlock(block));
    }

}
