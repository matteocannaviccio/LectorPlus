package it.uniroma3.main;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.pipeline.ArticleParser;
import it.uniroma3.extractor.pipeline.EntityDetection;
import it.uniroma3.extractor.pipeline.TriplesExtractor;
import it.uniroma3.model.extraction.FactsExtractor;
import it.uniroma3.model.extraction.FactsExtractor.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
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
     * This is a pipeline in which we execute one step at a time. 
     * Need for testing.
     * 
     * @param inputPath
     */
    protected static void singleStepProcess(String inputPath){
	if (Configuration.getPipelineSteps().contains("AP")){
	    ArticleParser ap = new ArticleParser(inputPath, inputPath.endsWith("bz2"), Configuration.getParsedArticlesFile());
	    ap.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("ED")){
	    EntityDetection ed = new EntityDetection(Configuration.getParsedArticlesFile(), 
		    Configuration.getAugmentedArticlesFile());
	    ed.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("TE")){
	    TriplesExtractor te = new TriplesExtractor(Configuration.getAugmentedArticlesFile());
	    te.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("FE")){
	    FactsExtractor extractor = new FactsExtractor();
	    extractor.setModelForEvaluation(ModelType.TextExtChallenge, "labeled_triples", 5, -1, PhraseType.TYPED_PHRASES);
	    extractor.runExtraction();
	}

	Lector.closeAllConnections();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	String[] languages = new String[]{"en", "es", "it", "fr", "de"};
	for (String lang : languages){
	    Configuration.init(args);
	    Configuration.setParameter("language", lang);
	    Configuration.printDetails();
	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	    completeInMemoryProcess(wikiLang);
	}
    }
}
