package it.uniroma3.util;

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
     * @param map
     * @return
     */
    public static Map<String, Double[]> getDoubleKRanking(Map<String,Double[]> map, int posSortableInArray, int k){
	List<Map.Entry<String,Double[]>> entries = new LinkedList<Map.Entry<String,Double[]>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<String,Double[]>>() {
	    @Override
	    public int compare(Entry<String,Double[]> o1, Entry<String,Double[]> o2) {
		return o2.getValue()[posSortableInArray].compareTo(o1.getValue()[posSortableInArray]);
	    }
	});

	int c = 0;

	Map<String,Double[]> sortedMap = new LinkedHashMap<String,Double[]>();
	for(Map.Entry<String,Double[]> entry: entries){
	    if (c < k){
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
	    if (c < k){
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
    public static Map<String,Double> getNormalizedTopKRanking(Map<String,Double> map, int k){
	List<Map.Entry<String,Double>> entries = new LinkedList<Map.Entry<String,Double>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<String,Double>>() {
	    @Override
	    public int compare(Entry<String,Double> o1, Entry<String,Double> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});

	Map<String,Double> sortedMap = new LinkedHashMap<String,Double>();

	int c = 0;
	double tot = 0;
	for(Map.Entry<String,Double> entry: entries){
	    tot += entry.getValue();
	}

	for(Map.Entry<String,Double> entry: entries){
	    if (c < k){
		sortedMap.put(entry.getKey(), entry.getValue()/tot);
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
    public static <K extends Comparable <? super K>,V extends Comparable <? super V>> List<K> getTopKElements(Map<K,V> map, int k){
	List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());
	Collections.sort(entries, new Comparator<Map.Entry<K,V>>() {

	    @Override
	    public int compare(Entry<K, V> o1, Entry<K, V> o2) {
		return o2.getValue().compareTo(o1.getValue());
	    }
	});

	List<K> result= new LinkedList<K>();

	int c = 0;

	for(Map.Entry<K,V> entry: entries){
	    if (c < k){
		result.add(entry.getKey());
		c++;
	    }
	}

	return result;
    }

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
