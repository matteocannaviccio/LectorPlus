package it.uniroma3.extractor.kg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.kg.normalizer.Node;
import it.uniroma3.extractor.kg.normalizer.TGPattern;
import it.uniroma3.extractor.util.reader.LectorRDFReader;
import it.uniroma3.extractor.util.reader.LectorRDFReader.Encoding;

/**
 * 
 * @author matteo
 *
 */
public class Ontology {

    private Map<String, String> subClassOF;
    private Set<String> leavesNodes;
    private Set<String> intermediateNodes;

    /**
     * 
     */
    public Ontology(){
	subClassOF = new HashMap<String, String>();
	leavesNodes = new HashSet<String>();
	intermediateNodes = new HashSet<String>();
	initOntology(Configuration.getDBPediaOntologyFile());
    }

    /**
     * Read the .ttl file with the ontology and fill the map.
     * 
     * @param pathOntology
     */
    private void initOntology(String pathOntology){
	LectorRDFReader reader = new LectorRDFReader(pathOntology, Encoding.tsv);
	Iterator<Triple> iter = reader.readNTFile();
	String ontologyNameSpace = "http://dbpedia.org/ontology/";
	String schemaNameSpace = "http://www.w3.org/2000/01/rdf-schema#subClassOf";

	while(iter.hasNext()){
	    Triple t = iter.next();
	    String subject = t.getSubject().toString();
	    String object = t.getObject().toString();
	    String relation = t.getPredicate().toString();

	    if (relation.equals(schemaNameSpace) && subject.contains(ontologyNameSpace) && object.contains(ontologyNameSpace)){
		subject = subject.replaceAll(ontologyNameSpace, "");
		object = object.replaceAll(ontologyNameSpace, "");

		this.subClassOF.put(subject, object);
		if (!this.intermediateNodes.contains(subject))
		    this.leavesNodes.add(subject);
		this.intermediateNodes.add(object);
	    }
	}
    }

    /**
     * 
     * @param subject
     * @param object
     */
    public TGPattern getTGPattern(String node){
	
	if (node.equals("[none]")){
	    return TGPattern.make(null, 0);
	}
	List<List<Node>> levels = new LinkedList<List<Node>>();
	List<Node> levelLeaf = new LinkedList<Node>();
	levelLeaf.add(Node.make(node));
	levels.add(levelLeaf);
	
	/* RULE TO MAKE INTER. NODES --> LEAF
	if (this.intermediateNodes.contains(node)){
	    List<Node> levelFirst = new LinkedList<Node>();
	    levelFirst.add(Node.make(node + "Leaf"));
	    levels.add(levelFirst);
	}
	*/

	String currentNode = node;
	while (currentNode != null){
	    List<Node> currentLevel = new LinkedList<Node>();
	    currentNode = this.subClassOF.get(currentNode);
	    if(currentNode == null){
		currentLevel.add(Node.make("Thing"));
		levels.add(0, currentLevel);
	    }else{
		currentLevel.add(Node.make(currentNode));
		levels.add(0, currentLevel);
	    }
	}
	return TGPattern.make(levels, 1);
    }
    
    /**
     * 
     * @param node
     * @return
     */
    public int depthNode(String node){
	return getTGPattern(node).getDepth();
    }

}
