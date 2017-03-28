package it.uniroma3.kg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.graph.Triple;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.reader.RDFReader;

public class DBPedia {

    private KeyValueIndex indexKG;

    /**
     * @throws IOException 
     * 
     */
    public DBPedia(){
	if (!new File(Configuration.getKGIndex()).exists()){
	    if (!new File(Configuration.getNormalizedDBPediaFile()).exists()){
		System.out.print("Normalizing DBPedia ...");
		long start_time = System.currentTimeMillis();
		try {
		    this.normalizeDBPediaDump();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		long end_time = System.currentTimeMillis();
		System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	    }
	    System.out.print("Creating KG resolver ...");
	    long start_time = System.currentTimeMillis();
	    this.indexKG = new KeyValueIndex(Configuration.getNormalizedDBPediaFile(), Configuration.getKGIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");

	}
	else // we already have the index
	    this.indexKG = new KeyValueIndex(Configuration.getKGIndex());
    }

    /**
     * 
     * @param uri
     * @return
     */
    private String getResourceName(String uri){
	String namespace = "http://dbpedia.org/resource/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private String getPredicateName(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    private boolean isDBPediaResource(String uri){
	String namespace = "http://dbpedia.org/resource/";
	return uri.contains(namespace);
    }

    /**
     * 
     * @param uri
     * @return
     */
    private boolean isDBPediaPredicate(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.contains(namespace);
    }

    /**
     * It iterated two times over the rdf dump in order to normalize
     * functional nodes. For example, the occupations of people are axpressed with
     * functional nodes such as: http://dbpedia.org/page/Allan_Dwan__1
     * 
     * The method removes those nodes and noralize them replacing with the relative
     * object (if it is a entity) or eliminating the triple if it is a literal.
     * 
     * @return
     * @throws IOException 
     */
    private void normalizeDBPediaDump() throws IOException{
	int cont = 0;
	String subject;
	String object;
	String pred;

	// first iteration: save second parts
	RDFReader reader = new RDFReader(Configuration.getMappingBasedDBPediaSourceFile());
	Map<String, List<String>> subject2secondparts = new HashMap<String, List<String>>();
	Iterator<Triple> iter = reader.readTTLBzip2File();

	while(iter.hasNext()){
	    cont++;
	    if (cont % 1000000 == 0)
		System.out.println("First iteration:\t" + cont);

	    Triple t = iter.next();
	    subject = getResourceName(t.getSubject().getURI());
	    object = getResourceName(t.getObject().getURI());
	    pred = getPredicateName(t.getPredicate().getURI());

	    if (subject.contains("__")){
		if (!subject2secondparts.containsKey(subject))
		    subject2secondparts.put(subject, new LinkedList<String>());
		subject2secondparts.get(subject).add(object +"\t"+ pred);
	    }
	}
	reader.closeReader();

	// second iteration: print clean triples in the following file
	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Configuration.getNormalizedDBPediaFile())));

	reader = new RDFReader(Configuration.getMappingBasedDBPediaSourceFile());
	cont = 0;
	iter = reader.readTTLBzip2File();

	while(iter.hasNext()){
	    cont++;
	    if (cont % 1000000 == 0)
		System.out.println("Second iteration:\t" + cont);

	    Triple t = iter.next();

	    // both subject and object have to be DBPedia resources (e.g. not common http addresses)
	    if (isDBPediaResource(t.getSubject().getURI()) && 
		    isDBPediaResource(t.getObject().getURI()) && 
		    isDBPediaPredicate(t.getPredicate().getURI())){

		subject = getResourceName(t.getSubject().getURI());
		object = getResourceName(t.getObject().getURI());
		pred = getPredicateName(t.getPredicate().getURI());

		if (!subject.contains("__")){
		    if (object.contains("__")){
			if (subject2secondparts.containsKey(object))
			    for (String secPart : subject2secondparts.get(object))
				bw.write(subject + "###" + secPart + "\n");
		    }else
			bw.write(subject + "###" + object + "\t" + pred + "\n");
		}
	    }
	}
	bw.close();
	reader.closeReader();
    }

    /**
     * 
     * @param wikidSubject
     * @param wikidObject
     * @return
     */
    public Set<String> getRelations(String wikidSubject, String wikidObject) {
	Set<String> relations = new HashSet<String>();
	for (String relation : indexKG.retrieveValues(wikidSubject + "###" + wikidObject))
	    relations.add(relation);
	for (String relation : indexKG.retrieveValues(wikidObject + "###" + wikidSubject)){
	    if(!relations.contains(relation))
		relations.add(relation + "(-1)");
	}
	return relations;
    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
	Configuration.init("/Users/matteo/Desktop/data/config.properties");
	DBPedia res = new DBPedia();

	String subject = "Steven_Spielberg";
	String object = "Amistad_(film)";

	System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
	for (String relation : res.getRelations(subject, object))
	    System.out.println("\t" + relation);

    }

}
