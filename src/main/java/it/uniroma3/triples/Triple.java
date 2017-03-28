package it.uniroma3.triples;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Lector;

public class Triple {

    private String subject;
    private String object;

    private String pre;
    private String phrase;
    private String post;

    /**
     * 
     * @param subject
     * @param phrase
     * @param object
     */
    public Triple(String pre, String subject, String phrase, String object, String post){
	this.subject = subject;
	this.phrase = phrase;
	this.object = object;
	this.pre = pre;
	this.post = post;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
	return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
	this.subject = subject;
    }

    /**
     * @return the object
     */
    public String getObject() {
	return object;
    }

    /**
     * @param object the object to set
     */
    public void setObject(String object) {
	this.object = object;
    }

    /**
     * @return the phrase
     */
    public String getPhrase() {
	return phrase;
    }

    /**
     * @param phrase the phrase to set
     */
    public void setPhrase(String phrase) {
	this.phrase = phrase;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((object == null) ? 0 : object.hashCode());
	result = prime * result + ((phrase == null) ? 0 : phrase.hashCode());
	result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
	Triple other = (Triple) obj;
	if (object == null) {
	    if (other.object != null)
		return false;
	} else if (!object.equals(other.object))
	    return false;
	if (phrase == null) {
	    if (other.phrase != null)
		return false;
	} else if (!phrase.equals(other.phrase))
	    return false;
	if (subject == null) {
	    if (other.subject != null)
		return false;
	} else if (!subject.equals(other.subject))
	    return false;
	return true;
    }

    /**
     * 
     */
    public String toString(){
	return this.pre + "\t" + this.subject + "\t" + this.phrase + "\t" + this.object + "\t" + this.post;
    }

    /**
     * @return the pre
     */
    public String getPre() {
	return pre;
    }

    /**
     * @return the post
     */
    public String getPost() {
	return post;
    }

    /**
     * @return 
     */
    public boolean isMVTriple() {
	return !isMVLEntity(this.subject) && isMVLEntity(this.object);
    }

    /**
     * @return 
     */
    public boolean isNERTriple() {
	return isNEREntity(this.subject) || isNEREntity(this.object);
    }

    /**
     * @return 
     */
    public boolean isJoinableTriple() {
	return isJoinableEntity(this.subject) && isJoinableEntity(this.object);
    }

    /**
     * @return 
     */
    private boolean isNEREntity(String entity) {
	boolean isNER = false;
	Pattern NEREntity = Pattern.compile("^<(PERSON|LOCATION|ORGANIZATION)<[^>]*?>>$");
	Matcher m = NEREntity.matcher(entity);
	if(m.matches()){
	    isNER = true;
	}
	return isNER;    
    }

    /**
     * @return 
     */
    private boolean isMVLEntity(String entity) {
	boolean isNER = false;
	Pattern NEREntity = Pattern.compile("^<(MVL)<[^>]*?>>$");
	Matcher m = NEREntity.matcher(entity);
	if(m.matches()){
	    isNER = true;
	}
	return isNER;    
    }

    /**
     * @return 
     */
    private boolean isJoinableEntity(String entity) {
	boolean isNER = false;
	Pattern NEREntity = Pattern.compile("^<[A-Z]+-[A-Z]+<[^>]*?>>$");
	Matcher m = NEREntity.matcher(entity);
	if(m.matches()){
	    isNER = true;
	}
	return isNER;    
    }

    /**
     * 
     * @param entity
     * @return
     */
    private String getDBPediaName(String entity){
	String dbpediaEntity = null;
	Pattern ENTITY = Pattern.compile("<[A-Z-]+<([^>]*?)>>");
	Matcher m = ENTITY.matcher(entity);
	if(m.find()){
	    dbpediaEntity = m.group(1);
	}
	return dbpediaEntity;
    }

    /**
     * 
     * @return
     */
    public Set<String> getLabels() {
	String dbpediaSubject = getDBPediaName(subject);
	String dbpediaObject = getDBPediaName(object);
	return Lector.getKg().getRelations(dbpediaSubject, dbpediaObject);
    }

}
