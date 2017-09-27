package it.uniroma3.main.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Basic functionality that is needed in multiple places.
 * @author denilson
 *
 */

public abstract class Ranking {


    /**
     * Sorts an input map based on value, in decreasing order, producing key ranking of the keys.
     * @param map
     * @return
     */
    public static <K extends Comparable <? super K>,V extends Comparable <? super V>> Map<K,V> getRanking(Map<K,V> map){
	List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});

	Map<K,V> sortedMap = new LinkedHashMap<K,V>();

	for(Map.Entry<K,V> entry: entries){
	    sortedMap.put(entry.getKey(), entry.getValue());
	}

	return sortedMap;
    }


    /**
     * Sorts an input map based on value, in decreasing order, producing key ranking of the keys.
     * @param map
     * @return
     */
    public static <K extends Comparable <? super K>,V extends Comparable <? super V>> Map<K,V> getTopKRanking(Map<K,V> map, int k){
	List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {
	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});
	Map<K,V> sortedMap = new LinkedHashMap<K,V>();
	int c = 0;
	for(Map.Entry<K,V> entry: entries){
	    if (c < k || k == -1){
		sortedMap.put(entry.getKey(), entry.getValue());
		c++;
	    }
	}
	return sortedMap;
    }
    

    /**
     * Sorts an input map based on value, in decreasing order, producing key ranking of the keys.
     * @param map
     * @return
     */
    public static <K extends Comparable <? super K>,V extends Comparable <? super V>> Map<K,V> getInverseTopKRanking(Map<K,V> map, int k){
	List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {
	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o1.getValue().compareTo(o2.getValue());
	    }
	});
	Map<K,V> sortedMap = new LinkedHashMap<K,V>();
	int c = 0;
	for(Map.Entry<K,V> entry: entries){
	    if (c < k || k == -1){
		sortedMap.put(entry.getKey(), entry.getValue());
		c++;
	    }
	}
	return sortedMap;
    }



    /**
     * 
     * @param corpus
     * @return
     */
    public static <K extends Comparable <? super K>> Map<K,Double> getTFIDFWeights(List<Map<K,Integer>> corpus){
	Map<K,Double> result = new HashMap<K,Double>();
	Map<K,Set<Integer>> idfMap = new HashMap<K,Set<Integer>>();
	Map<K,Integer> tfMap = new HashMap<K,Integer>();
	double N = 1.0 * corpus.size();
	for (Map<K,Integer> doc: corpus){
	    for (Map.Entry<K,Integer> termFreq: doc.entrySet()){
		K term = termFreq.getKey();
		int freq = termFreq.getValue();
		//update the term frequencies
		if (tfMap.containsKey(term))
		    tfMap.put(term, freq+ tfMap.get(term));
		else
		    tfMap.put(term, freq);

		//associate terms and documents
		if (!idfMap.containsKey(term))
		    idfMap.put(term, new TreeSet<Integer>());
		idfMap.get(term).add(doc.hashCode());
	    }
	}
	for (Map.Entry<K,Integer> termFreq : tfMap.entrySet()){
	    double w = termFreq.getValue() * Math.log(N/(1.0+idfMap.get(termFreq.getKey()).size()));
	    result.put(termFreq.getKey(), w);
	}
	return result;
    }

}
