package it.uniroma3.pipeline;

import java.io.File;

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
public class FullTest {
    

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Test
    public void factsExtractorTest(){
	ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource("NEXT.xml").getFile());
	Full.pipelinedProcess(file.getAbsolutePath());
	
	
    }
}
