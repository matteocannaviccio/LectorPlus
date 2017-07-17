package it.uniroma3.main.util.io.ntriples;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import it.uniroma3.main.util.io.Compressed;
/**
 * It takes a columnns-stye file (e.g. tsv, ttl, nt, etc.), read it, and put it in memory.
 * We read file entirely to avoid malformatted crashes of jena-like streaming parser.
 * 
 * @author matteo
 *
 */
public class NTriplesReader {

    // based on the encoding we chose the right BufferedReader
    public enum Encoding {tsv, compressed};

    /**
     * 
     * @param path
     * @param encoding
     * @return
     */
    private static BufferedReader getReader(String path, Encoding encoding){
	BufferedReader reader = null;
	switch(encoding){
	case tsv:
	    reader = getBufferedReader(path);
	    break;

	case compressed:
	    reader = Compressed.getBufferedReaderForCompressedFile(path);
	    break;
	}
	return reader;

    }

    /**
     * It reads triples file.
     * 		It checks that:
     * 		(1) the subject is a dbpedia resource
     * 		(2) the object is a dbpedia resource
     * 		(3) the predicate is in dbpedia ontology
     * 
     * @param path
     * @param encoding
     */
    public static List<String> readTriples(String path, Encoding encoding, boolean isOntology){
	List<String> triples = new LinkedList<String>();
	BufferedReader reader = getReader(path, encoding);
	String line;
	try {
	    while((line=reader.readLine()) != null){
		/*
		 * it checks that the triple is interesting 
		 * (i.e. dbpedia resources and one propery)
		 */
		String t;
		if (isOntology)
		    t = NTriplesConverter.convertRDFOntologyLines2String(line);
		else
		    t = NTriplesConverter.convertRDFFacts2String(line);
		if (t != null)
		    triples.add(t);
	    }
	    reader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return triples;
    }

    /**
     * It reads a bi-columns file.
     * 
     * @param path
     * @param encoding
     */
    public static List<String> readPairs(String path, Encoding encoding){
	List<String> pairs = new LinkedList<String>();
	BufferedReader reader = getReader(path, encoding);
	String line;
	try {
	    while((line=reader.readLine()) != null){
		String p = NTriplesConverter.convertRDFinstancetypes2String(line);
		if (p != null)
		    pairs.add(p);
	    }
	    reader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return pairs;
    }

    /**
     * Returns a simple BufferedReader for a tsv file.
     * @param path
     * @return
     */
    private static BufferedReader getBufferedReader(String path){
	BufferedReader br = null;
	try {
	    FileInputStream fin = new FileInputStream(path);
	    BufferedInputStream bis = new BufferedInputStream(fin);
	    br = new BufferedReader(new InputStreamReader(bis));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	}
	return br;
    }


}
