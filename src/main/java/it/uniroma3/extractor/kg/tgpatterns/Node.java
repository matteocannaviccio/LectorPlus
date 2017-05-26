package it.uniroma3.extractor.kg.tgpatterns;

/**
 * 
 * @author matteo
 *
 */
public class Node implements Comparable<Node>{

    private String name;
    private Double weight;
    private Double INITIAL_WEIGHT = 1.0;
    
    /**
     * 
     * @param name
     * @param weight
     */
    private Node(String name){
	this.name = name;
	this.weight = INITIAL_WEIGHT;
    }
    
    /**
     * 
     * @param name
     * @param weight
     */
    private Node(String name, double weight){
	this.name = name;
	this.weight = weight;
    }
    
    /**
     * 
     * @param name
     * @param weight
     * @return
     */
    public static Node make(String name){
	return new Node(name);
    }
    
    /**
     * 
     * @param name
     * @param weight
     * @return
     */
    public static Node make(String name, double weight){
	return new Node(name, weight);
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
	return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(Double weight) {
	this.weight = weight;
    }
    
    /**
     * 
     */
    public String toString(){
	return "[" + this.name + ":" + this.weight + "]";
    }

    @Override
    public int compareTo(Node o) {
	return (int) (this.weight - o.weight);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Node other = (Node) obj;
	if (name == null) {
	    if (other.name != null)
		return false;
	} else if (!name.equals(other.name))
	    return false;
	return true;
    }

}
