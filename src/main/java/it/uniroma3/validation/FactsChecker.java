package it.uniroma3.validation;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.TSVReader;
/**
 * 
 * @author matteo
 *
 */
public class FactsChecker {

    public FactsChecker(){

    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	final int SIZE_SAMPLE = 50;
	
	Configuration.init(args);
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
	    Configuration.updateParameter("language", lang);
	    
	    System.out.println("---------");
	    System.out.println("Language: " + lang);
	    System.out.println("---------");
	    
	    // read all the provenance files
	    Map<String, Set<String>> facts_content = new HashMap<String, Set<String>>();
	    Map<String, Integer> facts_sizes = new HashMap<String, Integer>();
	    File model_folder = new File(Configuration.getOutputFolder());
	    for (File facts_folder : model_folder.listFiles()){
		if (facts_folder.getName().equals(".DS_Store"))
		    continue;
		File provenance = new File(facts_folder.getAbsolutePath()+ "/" + lang + "_provenance.bz2");
		Set<String> content =  TSVReader.getLines2Set(provenance.getAbsolutePath());
		facts_content.put(facts_folder.getName(), content);
		facts_sizes.put(facts_folder.getName(), content.size());
	    }
	    System.out.println();
	    
	    // order and pick the three sets
	    Iterator<Entry<String, Integer>> iterator = Ranking.getRanking(facts_sizes).entrySet().iterator();
	    Set<String> externalSet = facts_content.get(iterator.next().getKey());
	    Set<String> mediumSet = facts_content.get(iterator.next().getKey());
	    Set<String> smallSet = facts_content.get(iterator.next().getKey());
	    externalSet.removeAll(mediumSet);
	    mediumSet.removeAll(smallSet);
	    
	    //System.out.println(externalSet.size() + "\t" + externalSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
	    //System.out.println(mediumSet.size() + "\t" + mediumSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
	    //System.out.println(smallSet.size() + "\t" + smallSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
	    
	    /*
	     * pick the size of each random samples from them
	     * based on the whole size of the section
	     */
	    int total_size = externalSet.size() + mediumSet.size() + smallSet.size();
	    double external_part = (double) externalSet.size()/total_size;
	    double medium_part = (double) mediumSet.size()/total_size;
	    double small_part = (double) smallSet.size()/total_size;
	    int fraction_external = (int) Math.floor(SIZE_SAMPLE * external_part);
	    int fraction_medium = (int) Math.floor(SIZE_SAMPLE * medium_part);
	    int fraction_small = (int) Math.floor(SIZE_SAMPLE * small_part);
	    int total = fraction_external + fraction_medium + fraction_small;
	    while ((SIZE_SAMPLE - total) > 0){
		fraction_small +=1;
		total = fraction_external + fraction_medium + fraction_small;
	    }
	    
	    System.out.println(fraction_external);
	    System.out.println(fraction_medium);
	    System.out.println(fraction_small);
	    System.out.println();
	    System.out.println(total);

	}
    }
    


}
