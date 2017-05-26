package it.uniroma3.pipeline;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.pipeline.Full;
/**
 * 
 * @author matteo
 *
 */
public class FullTest {
    

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Configuration.keyValue.put("createModelDB", "FALSE");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Test
    public void factsExtractorTest(){
	ClassLoader classLoader = getClass().getClassLoader();
	File file = new File(classLoader.getResource("Keno.xml").getFile());
	Full.pipelinedProcess(file.getAbsolutePath());
    }
}
