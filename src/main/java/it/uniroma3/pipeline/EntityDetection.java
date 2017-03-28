package it.uniroma3.pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.entitydetection.ReplAttacher;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.reader.JSONReader;

public class EntityDetection {
    
    /**
     * Entry point!
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	
	String config;
	if (args.length == 0){
	    config = "/Users/matteo/Desktop/data/config.properties";
	}else{
	    config = args[0];
	}
	
	/******************************************************************/
	Configuration.init(config);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	String input_file = Configuration.getParsedArticlesFile();
	JSONReader reader = new JSONReader(input_file);
	String output_file = Configuration.getAugmentedArticlesFile();
	PrintStream out_json = new PrintStream(new FileOutputStream(output_file), false, "UTF-8");
	ReplAttacher entDet = new ReplAttacher();
	/******************************************************************/
	
	/******************************************************************/
	List<WikiArticle> articles;
	while (!(articles = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    System.out.println("Working on next: " + articles.size() + " articles.");
	    long start_time = System.currentTimeMillis();

	    articles.parallelStream()
	    .map(s -> entDet.augmentEvidence(s))
	    .forEach(s -> out_json.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.println("Time: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");

	    System.out.println("Reading for next batch.");
	    articles.clear();
	}
	out_json.close();
	/******************************************************************/
    }

}
