package it.uniroma3.main;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class Main {

    /**
     * This is the complete pipeline. 
     * 
     * @param inputPath
     */
    private static void completeInMemoryProcess(WikiLanguage lang){
	Lector.init(lang, Configuration.getPipelineSteps());
	CompletePipeline cp = new CompletePipeline(Configuration.getOriginalArticlesFile());
	cp.runPipeline(Configuration.getNumArticlesToProcess(), Configuration.getChunkSize());
	cp.extractNovelFacts();
	Lector.closeAllConnections();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	String[] languages = new String[]{"es", "it", "de", "fr", "en"};
	for (String lang : languages){
	    Configuration.init(args);
	    Configuration.setParameter("language", lang);
	    Configuration.printDetails();
	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	    completeInMemoryProcess(wikiLang);
	}
    }
}
