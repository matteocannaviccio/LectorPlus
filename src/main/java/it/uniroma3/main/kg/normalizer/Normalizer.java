package it.uniroma3.main.kg.normalizer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.inout.ntriples.NTriplesReader;
import it.uniroma3.main.util.inout.ntriples.NTriplesReader.Encoding;
/**
 * 
 * @author matteo
 *
 */
public class Normalizer{

    /**
     * Read an RDF .ttl file containing the entity - types mapping and store them in a list of pairs.
     * 
     * @param sourceTTLfile
     * @return
     */
    public static List<Pair<String, String>> normalizeInstanceTypesDataset(String sourceFile) {
	String subject;
	String object;
	List<Pair<String, String>> normalizedKeyValue = new LinkedList<Pair<String, String>>();
	List<String> pairs = NTriplesReader.readPairs(sourceFile, Encoding.compressed); 
	for(String pair : pairs){
	    subject = pair.split("\t")[0];
	    object = pair.split("\t")[1];
	    if (!DBPedia.isIntermediateNode(subject)){
		normalizedKeyValue.add(Pair.make(subject, object));
	    }
	}
	return normalizedKeyValue;
    }
    
    /**
     * It iterated two times over the rdf dump in order to normalize
     * "functional nodes". For example, some occupations of people are expressed 
     * with functional nodes such as: http://dbpedia.org/page/Allan_Dwan__1.
     * 
     * The method removes those nodes and normalize them replacing with the relative
     * object (if it is a entity) or eliminating the triple if it is a literal.
     * 
     * @return
     */
    public static List<Pair<String, String>> normalizeMappingBasedDBPediaDump(String dumpFile){
	String subject;
	String object;
	String pred;

	List<Pair<String, String>> entityPair2relation = new LinkedList<Pair<String, String>>();

	// FIRST iteration: save second parts
	List<String> triples = NTriplesReader.readTriples(dumpFile, Encoding.compressed, false); 
	Map<String, List<String>> subject2secondparts = new HashMap<String, List<String>>();

	for(String triple : triples){
	    subject = triple.split("\t")[0];
	    pred = triple.split("\t")[1];
	    object = triple.split("\t")[2];

	    // only check if the subject contains __, otherwise skip
	    if (DBPedia.isIntermediateNode(subject)){
		if (!subject2secondparts.containsKey(subject))
		    subject2secondparts.put(subject, new LinkedList<String>());
		subject2secondparts.get(subject).add(object +"\t"+ pred);
	    }
	}

	// SECOND iteration: print clean triples in the following file
	for(String triple : triples){
	    subject = triple.split("\t")[0];
	    pred = triple.split("\t")[1];
	    object = triple.split("\t")[2];

	    if (!DBPedia.isIntermediateNode(subject)){
		if (DBPedia.isIntermediateNode(object)){
		    if (subject2secondparts.containsKey(object))
			for (String secPart : subject2secondparts.get(object)){
			    String obj = secPart.split("\t")[0];
			    String p = secPart.split("\t")[1];
			    entityPair2relation.add(Pair.make(subject + "###" + obj, p));
			}
		}else
		    entityPair2relation.add(Pair.make(subject + "###" + object, pred));
	    }
	}
	return entityPair2relation;
    }



}
