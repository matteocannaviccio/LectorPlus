package it.uniroma3.pipeline;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.reader.JSONReader;
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
	reader = new JSONReader(inputFile);
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
	while (!(articles = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    System.out.print("Facts Extractor on next: " + articles.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();

	    articles.parallelStream()
	    .forEach(s -> Lector.getTriplifier().extractTriples(s));

	    if(test){
		System.out.print("Printing results.\t");
		System.out.println(Lector.getTriplifier().printEverything()); // do it with a single article
	    }else{
		System.out.print("Flushing results.\t");
		Lector.getTriplifier().flushEverything();
	    }

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    articles.clear();
	}
	Lector.getTriplifier().printStatistics();
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
	FactsExtractor factsExtractor = new FactsExtractor(Configuration.getAugmentedArticlesFile());
	factsExtractor.process(false);
    }
}
