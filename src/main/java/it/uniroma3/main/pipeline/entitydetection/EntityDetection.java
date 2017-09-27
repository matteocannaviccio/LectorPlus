package it.uniroma3.main.pipeline.entitydetection;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiArticle;
import it.uniroma3.main.util.inout.JSONReader;
/**
 * 
 * @author matteo
 *
 */
public class EntityDetection {

    private JSONReader inputReader;
    private PrintStream outputWriter;

    /**
     * 
     * @param inputFile
     * @param outputFile
     */
    public EntityDetection(String inputFile, String outputFile){
	System.out.println("\n**** ENTITY DETECTION ****");

	if (inputFile != null && outputFile != null){
	    inputReader = new JSONReader(inputFile);
	    try {
		outputWriter = new PrintStream(new FileOutputStream(outputFile), false, "UTF-8");
	    } catch (UnsupportedEncodingException | FileNotFoundException e) {
		e.printStackTrace();
	    }
	}
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
	int cont = 0;
	while (!(articles = inputReader.nextChunk(Configuration.getChunkSize())).isEmpty() 
		&& cont < Configuration.getNumArticlesToProcess()) {
	    
	    System.out.print("Entity Detection on next: " + articles.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    cont += articles.size();

	    articles.parallelStream()
	    .map(s -> Lector.getEntitiesFinder().increaseEvidence(s))
	    .map(s -> Lector.getEntitiesTagger().augmentEvidence(s))
	    .forEach(s -> outputWriter.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    articles.clear();
	}
	
	inputReader.closeBuffer();
	outputWriter.close();
	
	Lector.closeDBPediaSpotlight();
    }
}
