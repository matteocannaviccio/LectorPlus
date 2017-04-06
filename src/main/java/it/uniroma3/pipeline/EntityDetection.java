package it.uniroma3.pipeline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.entitydetection.ReplAttacher;
import it.uniroma3.entitydetection.ReplFinder;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.reader.JSONReader;
/**
 * 
 * @author matteo
 *
 */
public class EntityDetection {

    private JSONReader inputReader;
    private ReplFinder entitiesFinder;
    private ReplAttacher entitiesTagger;
    private PrintStream outputWriter;

    /**
     * 
     * @param configFile
     */
    public EntityDetection(String inputFile, String outputFile){

	inputReader = new JSONReader(inputFile);
	entitiesFinder = new ReplFinder();
	entitiesTagger = new ReplAttacher();

	try {

	    outputWriter = new PrintStream(new FileOutputStream(outputFile), false, "UTF-8");

	} catch (UnsupportedEncodingException | FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param lines
     * @param parser
     * @param entitiesFinder
     * @param outputWriter
     */
    public void process(boolean test){
	List<WikiArticle> articles;
	while (!(articles = inputReader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    System.out.print("Entity Detection on next: " + articles.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();

	    articles.stream()
	    .map(s -> entitiesFinder.increaseEvidence(s))
	    .map(s -> entitiesTagger.augmentEvidence(s))
	    .forEach(s -> outputWriter.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    articles.clear();
	}
	inputReader.closeBuffer();
	outputWriter.close();
    }

    /**
     * Entry point!
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	String configFile;
	if (args.length == 0){
	    configFile = "/Users/matteo/Desktop/data/config.properties";
	}else{
	    configFile = args[0];
	}

	Configuration.init(configFile);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	EntityDetection entDet = new EntityDetection(Configuration.getParsedArticlesFile(), Configuration.getAugmentedArticlesFile());
	entDet.process(false);
    }

}
