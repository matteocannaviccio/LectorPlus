package it.uniroma3.util;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.reader.XMLReader;

public class DumpSearcher {

    /*
     * 
     */
    public static String entities_to_search = "<title>An American in Paris";

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	Configuration.init("/Users/matteo/Work/Repository/java/lectorplus/config.properties");
	Set<String> titles = Reader.getLines("/Users/matteo/Desktop/gt.tsv");

	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(Configuration.getOriginalArticlesFile(), true);

	/* ------ EXECUTION ------ */
	List<String> lines;
	while (!(lines = reader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    lines.parallelStream().filter(s -> titles.contains(entities_to_search)).forEach(System.out::println);	    
	    lines.clear();
	}
	
	reader.closeBuffer();
    }

}
