package it.uniroma3.extractor.pipeline;

import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.util.reader.JSONReader;
/**
 * 
 * @author matteo
 *
 */
public class FactsExtractor {

    private JSONReader reader;

    /**
     * 
     * @param configFile
     */
    public FactsExtractor(String inputFile){
	if (inputFile != null)
	    reader = new JSONReader(inputFile);
    }


    /**
     * 
     * @param lines
     * @param parser
     * @param entitiesFinder
     * @param outputWriter
     */
    public void pipelinedProcess(){
	List<WikiArticle> articles;
	while (!(articles = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    System.out.print("Facts Extraction on next: " + articles.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    articles
	    .parallelStream()
	    .forEach(s -> Lector.getTriplifier().extractTriples(s));
	    
	    Lector.getTriplifier().updateBlock();

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    articles.clear();
	}
	Lector.getTriplifier().endConnection();
	//System.out.println(Lector.getTriplifier().printEverything());

    }

}
