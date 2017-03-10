package it.uniroma3.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;

import it.uniroma3.util.Reader;

public class Outcome {
    
    private String name;
    private Map<String, Integer> wikid2peAbstractCount = new HashMap<String, Integer>();
    private Map<String, Integer> wikid2peBodyCount = new HashMap<String, Integer>();
    private Map<String, Integer> wikid2peCompleteCount = new HashMap<String, Integer>();
    private Map<String, List<String>> wikid2seAbstractList = new HashMap<String, List<String>>();
    private Map<String, List<String>> wikid2seBodyList = new HashMap<String, List<String>>();
    private Map<String, List<String>> wikid2seCompleteList = new HashMap<String, List<String>>();
    
    /**
     * 
     * @param path
     */
    public Outcome(String path, String name){
	this.name = name;
	fillMaps(path);
    }
    
    /**
     * 
     * @param path
     */
    @SuppressWarnings("unchecked")
    public void fillMaps(String path){
	for (String entry : Reader.getLines(path)){
	    String[] fields = entry.split("\t");
	    String wikid = fields[0];
	    
	    /*
	     * PRIMARY ENTITIES
	     */
	    int peAbstract = Integer.parseInt(fields[1]);
	    wikid2peAbstractCount.put(wikid, peAbstract); 
	    int peBody = Integer.parseInt(fields[2]);
	    wikid2peBodyCount.put(wikid, peBody);
	    wikid2peCompleteCount.put(wikid, peAbstract+peBody);
	    
	    /*
	     * SECONDARY ENTITIES
	     */
	    List<String> seAbstract = readList(fields[3]);
	    wikid2seAbstractList.put(wikid, seAbstract);
	    List<String> seBody = readList(fields[4]);
	    wikid2seBodyList.put(wikid, seBody);
	    wikid2seCompleteList.put(wikid, ListUtils.union(seAbstract, seBody));
	}
    }
    
    /**
     * 
     * @param text
     * @return
     */
    private static List<String> readList(String text){
	return new ArrayList<String>(Arrays.asList(text.split(" ")));
    }

    /**
     * @return the wikid2peAbstractCount
     */
    public Map<String, Integer> getWikid2peAbstractCount() {
        return wikid2peAbstractCount;
    }

    /**
     * @return the wikid2peBodyCount
     */
    public Map<String, Integer> getWikid2peBodyCount() {
        return wikid2peBodyCount;
    }

    /**
     * @return the wikid2peCompleteCount
     */
    public Map<String, Integer> getWikid2peCompleteCount() {
        return wikid2peCompleteCount;
    }

    /**
     * @return the wikid2seAbstractList
     */
    public Map<String, List<String>> getWikid2seAbstractList() {
        return wikid2seAbstractList;
    }

    /**
     * @return the wikid2seBodyList
     */
    public Map<String, List<String>> getWikid2seBodyList() {
        return wikid2seBodyList;
    }

    /**
     * @return the wikid2seCompleteList
     */
    public Map<String, List<String>> getWikid2seCompleteList() {
        return wikid2seCompleteList;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
