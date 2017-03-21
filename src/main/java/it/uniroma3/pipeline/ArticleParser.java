package it.uniroma3.pipeline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.entitydetection.ReplacementsFinder;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;
import it.uniroma3.reader.XMLReader;

/**
 * This is the main entry point of the parser.
 * 
 * It reads a configuration file where is specified the path of a Wikipedia XML
 * dump and start to read it, parsing the articles one by one, and creating a
 * pipeline of processment that acts over a "chunck" of articles (size in config
 * file).
 * 
 * The pipeline is composed by:
 * 
 * - an XMLReder: responsible to extract the wikimarkup of the articles from the
 * XML dump;
 * 
 * - a WikiParser: responsible to extract a structured version ev of the article
 * (i.e. WikiArticle);
 * 
 * - an EntityAugmenter: implementation of the Edntity Detection module that
 * allows to increase the instances of wikilinks.
 * 
 * 
 * 
 * @author matteo
 *
 */
public class ArticleParser {

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
	
	Configuration.init(config);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	XMLReader reader = new XMLReader(Configuration.getOriginalArticlesFile(), true);
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	ReplacementsFinder repFinder = new ReplacementsFinder();
	String output_file = Configuration.getParsedArticlesFile();
	PrintStream out_json = new PrintStream(new FileOutputStream(output_file), false, "UTF-8");

	List<String> lines;

	/* Iterate over the dump and process each article */
	while (!(lines = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {

	    System.out.println("Working on next: " + lines.size() + " articles.");
	    long start_time = System.currentTimeMillis();

	    lines.parallelStream().map(s -> parser.createArticleFromXml(s))
		    .filter(s -> s.getType() == ArticleType.ARTICLE).map(s -> repFinder.increaseEvidence(s))
		    .forEach(s -> out_json.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.println("Time: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");

	    System.out.println("Reading for next batch.");
	    lines.clear();
	}

	reader.closeBuffer();
	out_json.close();

    }

}
