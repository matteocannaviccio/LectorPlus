package it.uniroma3.extractor.kg.normalizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.hp.hpl.jena.graph.Triple;

import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.reader.RDFReader;
import it.uniroma3.extractor.util.reader.RDFReader.Encoding;
/**
 * 
 * @author matteo
 *
 */
public class TypesNormalizer{

    /**
     * Read the RDF file containing the entity - types mapping and store them in a list of pairs.
     * 
     * @param sourceBzip2File
     * @return
     */
    public static List<Pair<String, String>> normalizeTypesDataset(String sourceBzip2File) {
	String subject;
	String object;
	Iterator<Triple> iter = null;
	RDFReader reader = null;
	List<Pair<String, String>> normalizedKeyValue = new LinkedList<Pair<String, String>>();

	if (!sourceBzip2File.contains("airpedia")){
	    reader = new RDFReader(sourceBzip2File, Encoding.bzip2);
	    iter = reader.readTTLFile();
	}else{
	    reader = new RDFReader(sourceBzip2File, Encoding.gz);
	    try{
		iter = reader.readNTFile();
	    }catch(Exception e){}
	}

	while(iter.hasNext()){
	    Triple t = iter.next();
	    subject = t.getSubject().getURI();
	    object = t.getObject().getURI();
	    if (isDBPediaResource(subject) && !isIntermediateNode(subject) && isInDBPediaOntology(object)){
		normalizedKeyValue.add(Pair.make(getResourceName(subject), getPredicateName(object)));
	    }
	}
	reader.closeReader();
	return normalizedKeyValue;
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static String getResourceName(String uri){
	String namespace = null;
	if(Lector.getLang().equals(Lang.en))
	    namespace = "http://dbpedia.org/resource/";
	else{
	    namespace = "http://" + Lector.getLang() +".dbpedia.org/resource/";
	}
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static String getPredicateName(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static boolean isDBPediaResource(String uri){
	String namespace = null;
	if(Lector.getLang().equals(Lang.en))
	    namespace = "http://dbpedia.org/resource/";
	else{
	    namespace = "http://" + Lector.getLang() +".dbpedia.org/resource/";
	}
	return uri.contains(namespace);
    }

    /**
     * 
     * @param uri
     * @return
     */
    private static boolean isIntermediateNode(String uri){
	return uri.contains("__");
    }

    /**
     * 
     * @param uri
     * @return
     */
    protected static boolean isInDBPediaOntology(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.contains(namespace);
    }

}
