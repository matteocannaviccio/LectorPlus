package it.uniroma3.triples;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * @author matteo
 *
 */
public class MultiValue {
    
    private String code;
    private String section;
    private String wikid;
    private List<String> listWikid;
    
    /**
     * 
     * @param spanOfText
     */
    public MultiValue(String spanOfText, String section, String wikid){
	this.wikid = wikid;
	this.section = section;
	this.listWikid = extractList(spanOfText);
	this.code = getId();
    }
    
    /**
     * 
     * @param spanOfText
     * @return
     */
    private List<String> extractList(String spanOfText){
	List<String> wikids = new LinkedList<String>();
	// find entities
	String taggedEntity = "<[A-Z-][^>]*?>>";	
	Pattern ENTITIES = Pattern.compile(taggedEntity);
	Matcher m = ENTITIES.matcher(spanOfText);
	while(m.find()){
	    wikids.add(m.group(0));
	}
	return wikids;
    }
    
    /**
     * 
     * @return
     */
    private String getId(){
	return UUID.randomUUID().toString();
    }
    
    /**
     * 
     */
    public String toString(){
	return "\"" + this.code + "\",\"" + this.wikid + "\",\"" + this.section + "\",\"" +this.listWikid.toString() + "\"";

    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((code == null) ? 0 : code.hashCode());
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
	MultiValue other = (MultiValue) obj;
	if (code == null) {
	    if (other.code != null)
		return false;
	} else if (!code.equals(other.code))
	    return false;
	return true;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the wikid
     */
    public String getWikid() {
        return wikid;
    }

    /**
     * @return the listWikid
     */
    public List<String> getListWikid() {
        return listWikid;
    }

}
