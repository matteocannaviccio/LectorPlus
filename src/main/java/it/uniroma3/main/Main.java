package it.uniroma3.main;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;
import it.uniroma3.main.pipeline.Complete;
import it.uniroma3.main.pipeline.articleparser.ArticleParser;
import it.uniroma3.main.pipeline.entitydetection.EntityDetection;
import it.uniroma3.main.pipeline.factsextractor.FactsExtractor;
import it.uniroma3.main.pipeline.triplesextractor.TriplesExtractor;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
/**
 * Welcome.
 * This is the entry point of the LectorPlus tool.
 * 
 * Be sure you configured the config.properties file, especially with the needed pipeline steps and the languages 
 * and then run the system. It can be executed using the complete pipeline or the single step pipeline based on 
 * which steps are expressed in the configuration.
 * 
 * @author matteo
 *
 */
public class Main {

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	//Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("language", lang);
	    System.out.println("\n===================================");
	    System.out.println("Starting a new LectorPlus execution");
	    System.out.println("===================================");
	    Configuration.printFullyDetails();
	    WikiLanguage wikiLang = new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());

	    Lector.init(Configuration.getPipelineSteps());
	    if (Configuration.getPipelineSteps().split(",").length == 4)
		completeInMemoryProcess(wikiLang);
	    else
		singleStepProcess(wikiLang);
	    Lector.close();
	}
    }

    /**
     * This is the complete pipeline.
     * We first initialize Lector with the components that are needed (i.e. from the pipeline)
     * and at the end we close all the connections with the databases and the spotlight (if there are).
     * 
     * @param inputPath
     */
    private static void completeInMemoryProcess(WikiLanguage lang){
	Complete cp = new Complete(Configuration.getOriginalArticlesFile());
	cp.runPipeline(Configuration.getNumArticlesToProcess(), Configuration.getChunkSize());
	cp.extractNovelFacts();
    }

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
	    System.out.println("\nModel creation");
	    System.out.println("-----------------");
	    // here we derive model table
	    Lector.getDbmodel(false).deriveModelTable();
	    Model model = Model.getNewModel(Lector.getDbmodel(false), "model_triples", Configuration.getMinF(), Configuration.getPercUnl(), ModelType.NaiveBayes, Configuration.getMajThr());
	    model.printStats();
	    FactsExtractor extractor = new FactsExtractor(model);
	    extractor.runExtractOnFile(Integer.MAX_VALUE);
	}
    }

}
