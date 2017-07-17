package it.uniroma3.main.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CounterMap<K extends Comparable<K>> implements Map<K,Integer> {

    HashMap<K,Integer> map;

    public CounterMap() {
	this(256);
    }

    public CounterMap(int size) {
	map = new HashMap<K,Integer>(size);
    }

    public void add(K value, int num) {
	if (!map.containsKey(value))
	    map.put(value, num);
	else
	    map.put(value, num + map.get(value));
    }

    public void add(K value) {
	this.add(value, 1);
    }

    public void addAll(Map<? extends K, ? extends Integer> m) {
	for (Map.Entry<? extends K, ? extends Integer> p: m.entrySet())
	    this.add(p.getKey(), p.getValue());
    }

    public String toString() {
	StringBuilder sb = new StringBuilder();
	String separator = "";
	sb.append("{");
	for (Map.Entry<? extends K, ? extends Integer> p: map.entrySet()) {
	    sb.append(separator);
	    separator = ",";
	    sb.append(p.getKey());
	    sb.append("=");
	    sb.append(p.getValue());
	}
	sb.append("}");
	return sb.toString();	    
    }

    @Override
    public int size() {
	return map.size();
    }

    @Override
    public boolean isEmpty() {
	return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
	return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	return map.containsValue(value);
    }

    @Override
    public Integer get(Object key) {
	return map.get(key);
    }

    @Override
    public Integer put(K key, Integer value) {
	return map.put(key, value);
    }

    @Override
    public Integer remove(Object key) {
	return map.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends Integer> m) {
	throw new UnsupportedOperationException();    
    }

    @Override
    public void clear() {
	map.clear();
    }

    @Override
    public Set<K> keySet() {
	return map.keySet();
    }

    @Override
    public Collection<Integer> values() {
	return this.map.values();
    }

    @Override
    public Set<java.util.Map.Entry<K, Integer>> entrySet() {
	return map.entrySet();
    }
}
