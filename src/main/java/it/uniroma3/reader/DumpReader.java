package it.uniroma3.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.EntityAugmenter;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;
import it.uniroma3.util.Reader;
/**
 * This is the main entry point of the parser.
 * 
 * It reads a configuration file where is specified the path 
 * of a Wikipedia XML dump and start to read it, parsing the 
 * articles one by one, and creating a pipeline of processment 
 * that acts over a "chunck" of articles (size in config file).
 * 
 * The pipeline is composed by:
 * 
 *  - an XMLReder: responsible to extract the wikimarkup of the
 *  	articles from the XML dump;
 *  
 *  - a WikiParser: responsible to extract a structured version 
 *  	of the article (i.e. WikiArticle);
 *  
 *  - an EntityAugmenter: implementation of the Edntity Detection
 *  	module that allows to increase the instances of wikilinks.
 * 
 * 
 * 
 * @author matteo
 *
 */
public class DumpReader {

    /**
     * Entry point!
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");
	
	 // Dump reader from XML dump ("true" because is in .bzip2 format, otherwise "false"). 
	XMLReader reader = new XMLReader(Configuration.getInputWikipediaDump(), true);

	// Wikipedia article parser: from a span of xml text to a WikiArticle object.
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageUsed()));

	// Output printer.
	String output_file = Configuration.getOutputFolder() + "refactor_FILTERED.json";
	PrintStream out_json = new PrintStream(new FileOutputStream(output_file), false, "UTF-8");
	
	// Filter (tmp)
	Set<String> entitieToFilterIn = Reader.getLines("/Users/matteo/Work/Repository/java/lectorplus/groundtruths/entities/gt_people.tsv");

	/* ********************************
	 * *********** EXECUTION **********
	 * ********************************/

	List<String> lines;

	/* Iterate over the dump and process each article */
	while (!(lines = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	   
	    System.out.println("Working on next: " + lines.size() + " articles.");
	    long start_time = System.currentTimeMillis();
	    
	    lines.parallelStream()
	    .map(s -> parser.createArticleFromXml(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .filter(s -> entitieToFilterIn.contains(s.getWikid()))		//includes a filter to eliminate not-person entities
	    //.filter(s -> !s.getTables().isEmpty())
	    .map(s -> EntityAugmenter.augmentEntities(s))
	    .forEach(s -> out_json.println(s.toJson()));
	    
	    long end_time = System.currentTimeMillis();
	    System.out.println("Time: " + (end_time - start_time) + " ms.");
	    
	    System.out.println("Reading for next batch.");
	    lines.clear();
	}

	reader.closeBuffer();
	out_json.close();

    }

}
