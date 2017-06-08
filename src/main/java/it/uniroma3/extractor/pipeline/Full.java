package it.uniroma3.extractor.pipeline;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.model.extraction.FactsExtractor;
import it.uniroma3.model.extraction.FactsExtractor.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
/**
 * 
 * @author matteo
 *
 */
public class Full {

    /**
     * 
     * @param inputPath
     */
    public static void completeInMemoryProcess(String inputPath){
	CompletePipeline cp = new CompletePipeline(inputPath, inputPath.endsWith("bz2"));
	cp.pipelinedProcess();
    }

    /**
     * 
     * @param inputPath
     */
    public static void pipelinedProcess(String inputPath){
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
	    extractor.setModelForEvaluation(ModelType.LectorScore, "labeled_triples", 5, -1, PhraseType.TYPED_PHRASES);
	    extractor.runExtraction();
	}

	Lector.closeAllConnections();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	for (String lang : new String[]{"en", "es", "de", "it", "fr"}){
	    Configuration.init(args);
	    Configuration.setParameter("language", lang);
	    Configuration.printDetails();

	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
	    Lector.init(wikiLang, Configuration.getPipelineSteps());
	    completeInMemoryProcess(Configuration.getOriginalArticlesFile());
	}
    }

}
