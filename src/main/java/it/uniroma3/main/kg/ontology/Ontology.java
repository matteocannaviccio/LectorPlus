package it.uniroma3.main.kg.ontology;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.io.ntriples.NTriplesReader;
import it.uniroma3.main.util.io.ntriples.NTriplesReader.Encoding;

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
	List<String> pairs = NTriplesReader.readTriples(pathOntology, Encoding.tsv, true); 
	for(String pair : pairs){
	    String subject = pair.split("\t")[0];
	    String object = pair.split("\t")[1];
	    this.subClassOF.put(subject, object);
	    if (!this.intermediateNodes.contains(subject))
		this.leavesNodes.add(subject);
	    this.intermediateNodes.add(object);
	}
    }

    /**
     * 
     * @param subject
     * @param object
     */
    public OntPath getOntPath(String node){

	if (node.equals("[none]")){
	    return OntPath.make(null, 0);
	}

	if (node.startsWith("[") && node.endsWith("]"))
	    node = node.substring(1, node.length()-1);

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
	return OntPath.make(levels, 1);
    }

    /**
     * Returns true if the first argument is a parent type of the second.
     * 
     * @param possibleParent
     * @param possibleChild
     * @return
     */
    public boolean isChildOf(String possibleParent, String possibleChild){
	OntPath pathChild = getOntPath(possibleChild);
	if (possibleParent.startsWith("[") && possibleParent.endsWith("]"))
	    possibleParent = possibleParent.substring(1, possibleParent.length()-1);
	return pathChild.contains(possibleParent);
    }

    /**
     * 
     * @param node
     * @return
     */
    public int depthNode(String node){
	return getOntPath(node).getDepth();
    }

}
