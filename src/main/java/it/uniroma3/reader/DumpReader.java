package it.uniroma3.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniroma3.entitydetection.EntityAugmenter;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;

public class DumpReader {

    /* --- EN ---- */
    public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/en/enwiki-20161220-first.bz2";
    //public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/en/enwiki-20161220-pages-articles.xml.bz2";
    public static String language = "en";


    /* --- ES ----
     * public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/es/eswiki-20161220-pages-articles.xml.bz2";
     * public static String language = "es";
     */

    /* --- IT ----
     * public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/it/itwiki-20170120-pages-articles.xml.bz2";
     * public static String language = "it";
     */

    /*
     * Chunk of articles to read
     */
    public static int chunkSize = 1000;

    /*
     * 
     */
    public static String external_filter_path = "/Users/matteo/Work/Repository/ualberta/lectorplus/groundtruths/first_sentence/en_complete.tsv";

    /**
     * 
     * @return
     * @throws IOException
     */
    public static Set<String> getEntitiesFromFile(String path) throws IOException{
	Set<String> entities = new HashSet<String>();
	BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	String line;
	while((line = br.readLine()) != null){
	    entities.add(line.split("\t")[0]);
	}
	br.close();
	return entities;

    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	long start_time = System.currentTimeMillis();


	/* ------------------------------------ PIPELINE COMPONENTS ------------------------------------ */
	// reader
	XMLReader reader = new XMLReader(dump_path, true);

	// parser
	WikiParser parser = new WikiParser(new WikiLanguage(language));


	// writer json
	PrintStream out_json = new PrintStream(
		new FileOutputStream("/Users/matteo/Work/wikipedia-parsing/output/" 
			+ language + ".json"), false, "UTF-8");

	//Set<String> filterEntities = getEntitiesFromFile(external_filter_path);

	/* ------------------------------------------ EXECUTION ------------------------------------------ */

	List<String> lines;
	while((lines = reader.nextChunk(chunkSize)) != null){
	    
	    System.out.println("Working on next: " + lines.size() + " articles.");

	    lines.parallelStream()		
	    .map(s -> parser.createArticleFromXml(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .map(s -> EntityAugmenter.findPrimaryEntitiesHooks(s))
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
