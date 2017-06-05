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
public class PipelineTest {
    
    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Test
    public void factsExtractorTest(){
	File file = new File("/Users/matteo/Work/Repository/java/lectorplus/src/test/resources/timbarnerslee.xml");
	System.out.println("Working with: " + file.getAbsolutePath());
	Full.pipelinedProcess(file.getAbsolutePath());
    }
}
