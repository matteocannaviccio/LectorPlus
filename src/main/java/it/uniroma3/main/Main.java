package it.uniroma3.main;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;
import it.uniroma3.main.pipeline.Pipeline;
import it.uniroma3.model.evaluation.Evaluator;
import it.uniroma3.model.evaluation.Extractor;
import it.uniroma3.model.paper.Paper;

/**
 * Welcome. This is the entry point of the LectorPlus tool.
 * 
 * Be sure you configured the config.properties file, especially with the needed pipeline steps and
 * the languages and then run the system. It can be executed using the complete pipeline or the
 * single step pipeline based on which steps are expressed in the configuration.
 * 
 * @author matteo
 *
 */
public class Main {
  
  public enum Task {
    fullpipeline, evaluation, extraction, paper
  };

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    Configuration.init(args);
    //Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
    Task t = Task.valueOf(Configuration.getTask());
    switch(t){
      case fullpipeline:
        extractionPipeline();
        break;
        
      case extraction:
        Extractor.extract();
        break;
        
      case evaluation:
        Evaluator.evaluate();
        break;
        
      case paper:
        Paper.calculateStats();
        break;
    }
  }
  
  /**
   * 
   */
  public static void extractionPipeline(){
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);
      System.out.println("\n===================================");
      System.out.println("Starting a new LectorPlus execution");
      System.out.println("===================================");
      Configuration.printFullyDetails();
      WikiLanguage wikiLang =
          new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties());
      Lector.init(Configuration.getPipelineSteps());
      processComplete(wikiLang);
      Lector.close();
    }
  }

  /**
   * This is the complete pipeline. We first initialize Lector with the components that are needed
   * (i.e. from the pipeline)
   * 
   * @param lang
   */
  private static void processComplete(WikiLanguage lang) {
    Pipeline cp = new Pipeline(Configuration.getOriginalArticlesFile(),
        Configuration.getParsedArticlesFile(), Configuration.getAugmentedArticlesFile());
    
    cp.runPipeline(Configuration.getNumArticlesToProcess(), Configuration.getChunkSize(),
        Configuration.getPipelineSteps());
    
    cp.extractNovelFacts();
  }

}
