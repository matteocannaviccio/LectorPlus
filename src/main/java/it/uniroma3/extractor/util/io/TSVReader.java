package it.uniroma3.extractor.util.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TSVReader {
    
    /**
     * 
     * @param path
     * @return
     */
    public static Set<String> getLines2Set(String path){
	Set<String> entities = new HashSet<String>();
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	    String line;
	    while((line = br.readLine()) != null){
		String[] fields = line.split("\t");
	        entities.add(fields[0]);
	    }
	    br.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return entities;
    }
    
    /**
     * 
     * @param path
     * @return
     */
    public static Map<String, String> getLines2Map(String path){
	Map<String, String> entities = new HashMap<String, String>();
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	    String line;
	    while((line = br.readLine()) != null){
		String[] fields = line.split("\t");
	        entities.put(fields[0], fields[1]);
	    }
	    br.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return entities;
    }

}
