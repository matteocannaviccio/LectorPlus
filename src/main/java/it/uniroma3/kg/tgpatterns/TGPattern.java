package it.uniroma3.kg.tgpatterns;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
/**
 * 
 * @author matteo
 *
 */
public class TGPattern {

    private List<List<Node>> path;
    private int instances;

    /**
     * 
     */
    public TGPattern(List<List<Node>> path, int instances){
	this.path = path;
	this.instances = instances;
    }

    /**
     * 
     * @param levels
     * @return
     */
    public static TGPattern make(List<List<Node>> levels, int instances) {
	return new TGPattern(levels, instances);
    }

    /**
     * 
     * @param path
     * @return
     */
    public String toString(){
	return StringUtils.join(this.path, " --- > ");
    }

    /**
     * @return the cardinality
     */
    private int getCardinality() {
	return (int) path.get(0).get(0).getWeight();
    }

    /**
     * To avoid NullPointer when we ask for the level.
     * 
     * @param pos
     * @return
     */
    private List<Node> getLevel(int pos){
	List<Node> level = new LinkedList<Node>();
	if(this.path.size() > pos){
	    level = this.path.get(pos);
	}
	return level;
    }
    
    /**
     * 
     */
    public TGPattern normalize(){
	List<List<Node>> normalizedPath = new LinkedList<List<Node>>();
	int card = getCardinality();
	for(List<Node> level : this.path){
	    List<Node> normLevel = new LinkedList<Node>();
	    for (Node node : level){
		normLevel.add(Node.make(node.getName(), node.getWeight()/card));
	    }
	    normalizedPath.add(normLevel);
	}
	return new TGPattern(normalizedPath, this.getInstances());
    }
    
    /**
     * 
     */
    public TGPattern getMainPath(double treshold){
	List<List<Node>> mainPath = new LinkedList<List<Node>>();
	// make sure it is normalized
	TGPattern normalizedTGPattern = this.normalize();
	
	for(List<Node> level : normalizedTGPattern.path){
	  if (level.get(0).getWeight() > treshold){
	      List<Node> mainLevel = new LinkedList<Node>();
	      mainLevel.add(Node.make(level.get(0).getName(), level.get(0).getWeight()));
	      mainPath.add(mainLevel);
	  }
	}
	return new TGPattern(mainPath, this.getInstances());
    }
    

    /**
     * 
     */
    public TGPattern combine(TGPattern ext){
	int maxLevel = Math.max(this.path.size(), ext.path.size());
	List<List<Node>> combinedPath = new LinkedList<List<Node>>();
	
	for(int numLevel = 0; numLevel < maxLevel; numLevel++){	
	    List<Node> combinedLevel = new LinkedList<Node>();
	    List<Node> internalLevel = this.getLevel(numLevel);
	    List<Node> externalLevel = ext.getLevel(numLevel);
	    for (Node node : internalLevel){
		if(externalLevel.contains(node)){
		    int pos = externalLevel.indexOf(node);
		    double weight = externalLevel.get(pos).getWeight() + node.getWeight();
		    Node combinedNode = Node.make(node.getName(), weight);
		    combinedLevel.add(combinedNode);
		}else{
		    combinedLevel.add(node);
		}
	    }
	    for (Node node : externalLevel){
		if(!combinedLevel.contains(node)){
		    combinedLevel.add(node);
		}
	    }
	    combinedPath.add(combinedLevel);
	}
	return new TGPattern(combinedPath, this.getInstances() + ext.getInstances());
    }

    /**
     * @return the instances
     */
    public int getInstances() {
        return instances;
    }
    
    /**
     * 
     * @return
     */
    public int getDepth(){
	return this.path.size();
    }

}
