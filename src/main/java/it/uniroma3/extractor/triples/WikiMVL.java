package it.uniroma3.extractor.triples;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.extractor.bean.Lector;
/**
 * 
 * @author matteo
 *
 */
public class WikiMVL {

    private String code;
    private String section;
    private String wikid;
    private List<String> listWikid;
    private String type;

    /**
     * 
     * @param spanOfText
     */
    public WikiMVL(String spanOfText, String section, String wikid){
	this.wikid = wikid;
	this.section = section;
	this.listWikid = extractList(spanOfText);
	this.code = getId();
	this.type = mineType();
    }
    
    /**
     * This method extracts Wikipedia Id (i.e. wikid) from the annotated entities.
     * 
     * @param entity
     * @return
     */
    private String getWikipediaName(String entity){
	String dbpediaEntity = null;
	Pattern ENTITY = Pattern.compile("<[A-Z-]+<([^>]*?)>>");
	Matcher m = ENTITY.matcher(entity);
	if(m.find()){
	    dbpediaEntity = m.group(1);
	}
	return dbpediaEntity;
    }
    
    /**
     * Check that the entries of a MVL have all the same type.
     * 
     * @return
     */
    public String mineType(){
	String type = Lector.getDBPedia().getType(getWikipediaName(this.listWikid.get(0)));
	for (String entity : this.listWikid){
	    if (!Lector.getDBPedia().getType(entity).equals(type)){
		type = "[none]";
		break;
	    }
	}
	return type;
    }
    
    /**
     * Reg. Ex. used to detect MVL in sentences.
     * 
     * @return
     */
    public static Pattern getRegexMVL(){
	String taggedEntity = "<[A-Z-][^>]*?>>";
	Pattern regEx = Pattern.compile("(" + taggedEntity + "(,)\\s){3,8}" + "((,)?\\sand\\s([A-Za-z0-9 ]+\\s)?" + taggedEntity + ")?");
	return regEx;
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
	return "\"" + this.code + "\",\"" + this.wikid + "\",\"" + this.section +
		"\",\"" +this.listWikid.toString() + "\"";

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
	WikiMVL other = (WikiMVL) obj;
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

    /**
     * @return the section
     */
    public String getSection() {
	return section;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

}
