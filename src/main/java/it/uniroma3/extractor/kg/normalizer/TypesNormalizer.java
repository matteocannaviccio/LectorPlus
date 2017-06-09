package it.uniroma3.extractor.kg.normalizer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.graph.Triple;

import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.reader.LectorRDFReader;
import it.uniroma3.extractor.util.reader.LectorRDFReader.Encoding;
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

	List<Pair<String, String>> normalizedKeyValue = new LinkedList<Pair<String, String>>();
	LectorRDFReader reader = new LectorRDFReader(sourceBzip2File, Encoding.bzip2);
	Iterator<Triple> iter = reader.readTTLFile();

	while(iter.hasNext()){
	    Triple t = iter.next();
	    if (t.isConcrete()){
		try{
		    subject = t.getSubject().getURI();
		    object = t.getObject().getURI();
		    if (isDBPediaResource(subject) && !isIntermediateNode(subject) && isInDBPediaOntology(object)){
			normalizedKeyValue.add(Pair.make(getResourceName(subject), getPredicateName(object)));
		    }
		}catch(Exception e){
		    System.exit(1);
		}
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
	if(Lector.getWikiLang().getLang().equals(Lang.en))
	    namespace = "http://dbpedia.org/resource/";
	else{
	    namespace = "http://" + Lector.getWikiLang().getLang() +".dbpedia.org/resource/";
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
	if(Lector.getWikiLang().getLang().equals(Lang.en))
	    namespace = "http://dbpedia.org/resource/";
	else{
	    namespace = "http://" + Lector.getWikiLang().getLang() +".dbpedia.org/resource/";
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
