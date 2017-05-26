package it.uniroma3.extractor.pipeline;

import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
/**
 * 
 * @author matteo
 *
 */
public class Full {

    /**
     * 
     * @param ap
     * @param ed
     * @param fe
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
	if (Configuration.getPipelineSteps().contains("FE")){
	    FactsExtractor fe = new FactsExtractor(Configuration.getAugmentedArticlesFile());
	    fe.pipelinedProcess();
	}
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	pipelinedProcess(Configuration.getOriginalArticlesFile());
    }

}
