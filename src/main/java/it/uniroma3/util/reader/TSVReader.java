package it.uniroma3.util.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TSVReader {
    
    /**
     * 
     * @param path
     * @return
     */
    public static Set<String> getLines(String path){
	Set<String> entities = new HashSet<String>();
	try {
	    BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	    String line;
	    while((line = br.readLine()) != null){
	        entities.add(line);
	    }
	    br.close();
	    
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return entities;
    }

}
