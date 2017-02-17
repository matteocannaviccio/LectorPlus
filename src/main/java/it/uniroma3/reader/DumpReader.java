package it.uniroma3.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.EntityAugmenter;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;

public class DumpReader {

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/resources/config.properties");


	/*
	 * ------------------------------------ PIPELINE COMPONENTS ------------------------------------
	 */

	// reader
	XMLReader reader = new XMLReader(Configuration.getInputWikipediaDump(), true);

	// parser
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageUsed()));

	// writer json
	PrintStream out_json = new PrintStream(new FileOutputStream(Configuration.getOutputFolder() + Configuration.getLanguageUsed() + ".json"), false, "UTF-8");


	/*
	 * ------------------------------------------ EXECUTION  ------------------------------------------
	 */

	List<String> lines;
	long start_time = System.currentTimeMillis();
	while ((lines = reader.nextChunk(Configuration.getChunkSize())) != null) {

	    System.out.println("Working on next: " + lines.size() + " articles.");

	    lines.parallelStream()
	    .map(s -> parser.createArticleFromXml(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .map(s -> EntityAugmenter.augmentEntities(s))
	    .forEach(s -> out_json.println(s.toJson()));

	    lines.clear();
	    System.out.println("Reading for next batch.");
	}

	reader.closeBuffer();
	out_json.close();

	long end_time = System.currentTimeMillis();

	System.out.println("Time: " + (end_time - start_time) + " ms.");
    }

}
