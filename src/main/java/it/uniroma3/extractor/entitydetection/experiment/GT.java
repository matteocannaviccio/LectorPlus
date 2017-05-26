package it.uniroma3.extractor.entitydetection.experiment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;

import it.uniroma3.extractor.util.reader.TSVReader;

public class GT {
    
    private Map<String, String> wikid2domain = new HashMap<String, String>();
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
    public GT(String path){
	fillMaps(path);
    }
    
    /**
     * 
     * @param path
     */
    @SuppressWarnings("unchecked")
    public void fillMaps(String path){
	for (String entry : TSVReader.getLines(path)){
	    String[] fields = entry.split("\t");
	    String domain = fields[0];
	    String wikid = fields[1];
	    wikid2domain.put(wikid, domain);
	    
	    /*
	     * PRIMARY ENTITIES
	     */
	    int peAbstractTitle = Integer.parseInt(fields[2]);
	    int peAbstractSubtitle = Integer.parseInt(fields[3]);
	    int peAbstractSeed = Integer.parseInt(fields[4]);
	    int peAbstractPron = Integer.parseInt(fields[5]);
	    wikid2peAbstractCount.put(wikid, peAbstractTitle+peAbstractSubtitle+peAbstractSeed+peAbstractPron);
	    
	    int peBodyTitle = Integer.parseInt(fields[6]);
	    int peBodySubtitle = Integer.parseInt(fields[7]);
	    int peBodySeed = Integer.parseInt(fields[8]);
	    int peBodyPron = Integer.parseInt(fields[9]);
	    wikid2peBodyCount.put(wikid, peBodyTitle+peBodySubtitle+peBodySeed+peBodyPron);
	    wikid2peCompleteCount.put(wikid, peAbstractTitle+peAbstractSubtitle+peAbstractSeed+peAbstractPron+peBodyTitle+peBodySubtitle+peBodySeed+peBodyPron);

	    /*
	     * SECONDARY ENTITIES
	     */
	    List<String> seAbstract = readList(fields[10]);
	    wikid2seAbstractList.put(wikid, seAbstract);
	    List<String> seBody = readList(fields[11]);
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
     * @return the wikid2domain
     */
    public Map<String, String> getWikid2domain() {
        return wikid2domain;
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

}
