package it.uniroma3.extractor.pipeline;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.model.extraction.FactsExtractor;
import it.uniroma3.model.extraction.FactsExtractor.ModelType;
import it.uniroma3.model.model.Model.PhraseType;

public class Complete {
    
    /**
     * This is a pipeline in which we execute one step at a time. 
     * Need for testing.
     * 
     * @param inputPath
     */
    protected static void singleStepProcess(WikiLanguage lang){
	if (Configuration.getPipelineSteps().contains("AP")){
	    ArticleParser ap = new ArticleParser(Configuration.getOriginalArticlesFile(), Configuration.getParsedArticlesFile());
	    ap.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("ED")){
	    EntityDetection ed = new EntityDetection(Configuration.getParsedArticlesFile(),  Configuration.getAugmentedArticlesFile());
	    ed.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("TE")){
	    TriplesExtractor te = new TriplesExtractor(Configuration.getAugmentedArticlesFile());
	    te.pipelinedProcess();
	}

	if (Configuration.getPipelineSteps().contains("FE")){
	    FactsExtractor extractor = new FactsExtractor();
	    extractor.setModelForEvaluation(ModelType.TextExtChallenge, "labeled_triples", 5, -1, PhraseType.TYPED_PHRASES);
	    extractor.run();
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
	    singleStepProcess(wikiLang);
	}
    }

}
