package it.uniroma3.kg.normalizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.graph.Triple;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.util.reader.RDFReader;
/**
 * 
 * @author matteo
 *
 */
public class RelationsNormalizer extends NormalizerDBPediaDataset{
    
    public static void normalizeRelations(){
	System.out.print("Normalizing DBPedia relations ...");
	long start_time = System.currentTimeMillis();
	try {
	    normalizeMappingBasedDBPediaDump();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	long end_time = System.currentTimeMillis();
	System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
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
    private static void normalizeMappingBasedDBPediaDump() throws IOException{
	int cont = 0;
	String subject;
	String object;
	String pred;

	// first iteration: save second parts
	RDFReader reader = new RDFReader(Configuration.getSourceMappingBasedFile(), true);
	Map<String, List<String>> subject2secondparts = new HashMap<String, List<String>>();
	Iterator<Triple> iter = reader.readTTLFile();

	while(iter.hasNext()){
	    cont++;
	    if (cont % 6000000 == 0)
		System.out.println("First iteration:\t" + cont);

	    Triple t = iter.next();
	    subject = getResourceName(t.getSubject().getURI());
	    object = getResourceName(t.getObject().getURI());
	    pred = getPredicateName(t.getPredicate().getURI());

	    if (isIntermediateNode(subject)){
		if (!subject2secondparts.containsKey(subject))
		    subject2secondparts.put(subject, new LinkedList<String>());
		subject2secondparts.get(subject).add(object +"\t"+ pred);
	    }
	}
	reader.closeReader();

	// second iteration: print clean triples in the following file
	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Configuration.getIndexableDBPediaNormalizedRelationsFile())));

	reader = new RDFReader(Configuration.getSourceMappingBasedFile(), true);
	cont = 0;
	iter = reader.readTTLFile();

	while(iter.hasNext()){
	    cont++;
	    if (cont % 6000000 == 0)
		System.out.println("Second iteration:\t" + cont);

	    Triple t = iter.next();

	    // both subject and object have to be DBPedia resources (e.g. not common http addresses)
	    if (isDBPediaResource(t.getSubject().getURI()) && 
		    isDBPediaResource(t.getObject().getURI()) && 
		    isInDBPediaOntology(t.getPredicate().getURI())){

		subject = getResourceName(t.getSubject().getURI());
		object = getResourceName(t.getObject().getURI());
		pred = getPredicateName(t.getPredicate().getURI());

		if (!isIntermediateNode(subject)){
		    if (isIntermediateNode(object)){
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
}
