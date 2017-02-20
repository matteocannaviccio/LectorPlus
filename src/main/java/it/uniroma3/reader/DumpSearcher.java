package it.uniroma3.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniroma3.configuration.Configuration;

public class DumpSearcher {

    /*
     * 
     */
    public static String entities_to_search = "of the Joker under strict conditions that dictated a high salary";

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

	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/resources/config.properties");


	long start_time = System.currentTimeMillis();


	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(Configuration.getInputWikipediaDump(), true);


	/* ------ EXECUTION ------ */
	List<String> lines;
	while((lines = reader.nextChunk(Configuration.getChunkSize())) != null){
	    lines.parallelStream().filter(s -> s.contains(entities_to_search)).forEach(System.out::println);
	}
	reader.closeBuffer();
	long end_time = System.currentTimeMillis();

	System.out.println("Time: " + (end_time - start_time) + " ms.");
    }

}
