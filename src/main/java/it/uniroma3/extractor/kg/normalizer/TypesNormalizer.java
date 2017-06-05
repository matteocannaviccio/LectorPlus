package it.uniroma3.extractor.kg.normalizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.graph.Triple;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.util.reader.RDFReader;
import it.uniroma3.extractor.util.reader.RDFReader.Encoding;
/**
 * 
 * @author matteo
 *
 */
public class TypesNormalizer{

    public static void normalizeTypesFile(){
	System.out.println("Normalizing all DBPedia type-mapping files ...");
	long start_time = System.currentTimeMillis();
	try {
	    if (!new File(Configuration.getIndexableDBPediaNormalizedTypesFile()).exists()){
		System.out.println("[main instance types]");
		normalizeTypesDataset(Configuration.getSourceMainInstanceTypes(), Configuration.getIndexableDBPediaNormalizedTypesFile());
	    }

	    if (!new File(Configuration.getIndexableDBPediaAirpediaFile()).exists()){
		System.out.println("[airpedia instance types]");
		normalizeTypesDataset(Configuration.getSourceAirpediaInstanceTypes(), Configuration.getIndexableDBPediaAirpediaFile());
	    }

	    /*
	     * for the english version we rely on three more types datasets 
	     * but the usage is conditioned to some rules
	     */
	    if (Lector.getLang().equals(Lang.en)){
		if (!new File(Configuration.getIndexableDBPediaLHDFile()).exists()){
		    System.out.println("[lhd instance types]");
		    normalizeTypesDataset(Configuration.getSourceLHDInstanceTypes(), Configuration.getIndexableDBPediaLHDFile());
		}

		if (!new File(Configuration.getIndexableDBPediaDBTaxFile()).exists()){
		    System.out.println("[dbtax instance types]");
		    normalizeTypesDataset(Configuration.getSourceDBTaxInstanceTypes(), Configuration.getIndexableDBPediaDBTaxFile());
		}

		if (!new File(Configuration.getIndexableDBPediaSDTypedFile()).exists()){
		    System.out.println("[sdtyped instance types]");
		    normalizeTypesDataset(Configuration.getSourceSDTypedInstanceTypes(), Configuration.getIndexableDBPediaSDTypedFile());
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	long end_time = System.currentTimeMillis();
	System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
    }

    /**
     * 
     * @param sourceBzip2File
     * @param normalizedFile
     * @throws IOException
     */
    private static void normalizeTypesDataset(String sourceBzip2File, String normalizedFile) throws IOException {
	String subject;
	String object;
	Iterator<Triple> iter = null;
	BufferedWriter bw = null;
	RDFReader reader = null;

	if (!sourceBzip2File.contains("airpedia")){
	    reader = new RDFReader(sourceBzip2File, Encoding.bzip2);
	    bw = new BufferedWriter(new FileWriter(new File(normalizedFile)));
	    iter = reader.readTTLFile();
	}else{
	    reader = new RDFReader(sourceBzip2File, Encoding.gz);
	    bw =  new BufferedWriter(new FileWriter(new File(normalizedFile)));
	    iter = reader.readNTFile();
	}


	while(iter.hasNext()){
	    Triple t = iter.next();
	    subject = t.getSubject().getURI();
	    object = t.getObject().getURI();

	    if (isDBPediaResource(subject) && !isIntermediateNode(subject) && isInDBPediaOntology(object)){
		bw.write(getResourceName(subject) + "\t" + getPredicateName(object));
		bw.write("\n");
	    }

	}
	bw.close();
	reader.closeReader();
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
