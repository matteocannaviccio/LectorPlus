package it.uniroma3.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;
import it.uniroma3.reader.XMLReader;

public class DumpSearcher {

    /*
     * 
     */
    public static String entities_to_search = "<title>Andrei Sakharov";

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");


	Set<String> titles = Reader.getLines("/Users/matteo/Desktop/gt.txt");

	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(Configuration.getInputWikipediaDump(), true);

	/*
	 * Wikipedia article parser.
	 * From a span of xml text to a WikiArticle object.
	 */
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageUsed()));


	/* ------ EXECUTION ------ */
	List<String> lines;
	while (!(lines = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    lines.parallelStream().filter(s -> titles.contains(parser.extractsWikid(s))).forEach(System.out::println);	    
	    lines.clear();
	}
	
	reader.closeBuffer();
    }

}
