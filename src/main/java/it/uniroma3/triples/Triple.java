package it.uniroma3.triples;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Lector;
/**
 * 
 * @author matteo
 *
 */
public class Triple {

    private String subject;
    private String object;

    private String subjectType;
    private String objectType;

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
	this.subjectType = getEntityType(subject);
	this.phrase = phrase;
	this.object = object;
	this.objectType = getEntityType(object);
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
	return this.subject + this.subjectType + "\t" + this.phrase + "\t" + this.object + this.objectType;
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
     * A triple is a MV triple, if the subject is joinable and the object is a list.
     * 
     * @return 
     */
    public boolean isMVTriple() {
	return isJoinableEntity(this.subject) && isMVLEntity(this.object);
    }

    /**
     * A triple is an NER triple, if the subject or the object are annotated with an NER label.
     * 
     * @return 
     */
    public boolean isNERTriple() {
	return isNEREntity(this.subject) || isNEREntity(this.object);
    }

    /**
     * A triple is joinable only if both the subject and the object:
     *  (1) are joinable --> they are in DBpedia
     *  (2) have a type in DBpedia
     * Indeed, entities annotated with an NER are not joinable.
     * 
     * @return 
     */
    public boolean isJoinableTriple() {
	return (isJoinableEntity(this.subject) &&
		isJoinableEntity(this.object) &&
		!this.subjectType.equals("[none]") &&
		!this.objectType.equals("[none]"));
    }

    /**
     * Reg-ex that defines an NER entity.
     * Those annotated entities are the ones that can be found using a NER tool.
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
     * Reg-ex that defines a multi-values list (MVL) entity.
     * 
     * @return 
     */
    private boolean isMVLEntity(String entity) {
	boolean isMVL = false;
	Pattern MVLEntity = Pattern.compile("^<(MVL)<[^>]*?>>$");
	Matcher m = MVLEntity.matcher(entity);
	if(m.matches()){
	    isMVL = true;
	}
	return isMVL;    
    }

    /**
     * Reg-ex that defines joinable entities.
     * An annotated entity is "joinable" if:
     * 	(1) it has been annotated with a wikid
     * Indeed, entities annotated with an NER are not joinable.
     * 
     * @return 
     */
    private boolean isJoinableEntity(String entity) {
	boolean isJoinable = false;
	Pattern joinableEntity = Pattern.compile("^<[A-Z]+-[A-Z]+<[^>]*?>>$");
	Matcher m = joinableEntity.matcher(entity);
	if(m.matches()){
	    isJoinable = true;
	}
	return isJoinable;    
    }

    /**
     * This method extracts DBPedia names (i.e. Wikipedia ids) from the annotated entities.
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
     * This method queries the KG to find possible relations between the entities.
     * 
     * @return
     */
    public Set<String> getLabels() {
	String dbpediaSubject = getDBPediaName(subject);
	String dbpediaObject = getDBPediaName(object);
	return Lector.getKg().getRelations(dbpediaSubject, dbpediaObject);
    }


    /**
     * This method queries the Types Assigner to find the type for the entities.
     * It assigns [none] in case of no type.
     * 
     * @return
     */
    public String getEntityType(String entity) {
	String dbpediaEntity = getDBPediaName(entity);
	String type = "[none]";
	if (isJoinableEntity(entity) && !isMVLEntity(entity) && !isNEREntity(entity)){
	    List<String> types = Lector.getTypesAssigner().assignTypes(dbpediaEntity);
	    if (!types.isEmpty()){
		type = "[" + types.get(0) + "]";
	    }
	}
	return type;
    }

    /**
     * @return the subjectType
     */
    public String getSubjectType() {
	return subjectType;
    }

    /**
     * @return the objectType
     */
    public String getObjectType() {
	return objectType;
    }

}
