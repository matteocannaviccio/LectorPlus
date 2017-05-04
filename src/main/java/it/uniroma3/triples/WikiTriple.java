package it.uniroma3.triples;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Lector;
/**
 * 
 * @author matteo
 *
 */
public class WikiTriple {

    private String wikid;

    private String subject;
    private String object;
    
    private String wikiSubject;
    private String wikiObject;

    private String subjectType;
    private String objectType;

    private String pre;
    private String phrase;
    private String post;

    private TType type;

    public enum TType {
	MVL,
	JOINABLE,
	JOINABLE_NOTYPE_BOTH,
	JOINABLE_NOTYPE_SBJ,
	JOINABLE_NOTYPE_OBJ,
	NER_BOTH,
	NER_SBJ,
	NER_OBJ,
	DROP
    }

    /**
     * This constructor is used when we retrieve the triple from the DB.
     * 
     * @param wikid
     * @param phrase
     * @param subject
     * @param object
     * @param subjectType
     * @param objectType
     * @param articleType
     */
    public WikiTriple(String wikid, String phrase, String subject,  String object, 
	    String subjectType, String objectType, String tripleType){
	this.wikid = wikid;
	this.subject = subject;
	this.wikiSubject = getWikipediaName(subject);
	this.subjectType = subjectType;
	this.phrase = phrase;
	this.object = object;
	this.wikiObject = getWikipediaName(object);
	this.objectType = objectType;
	this.type = TType.valueOf(tripleType);
    }

    /**
     * This constructor is used when we harvest the triple from the text.
     * 
     * @param wikid
     * @param pre
     * @param subject
     * @param phrase
     * @param object
     * @param post
     */
    public WikiTriple(String wikid, String pre, String subject, String phrase, String object, String post){
	this.wikid = wikid;
	this.subject = subject;
	this.wikiSubject = getWikipediaName(subject);
	this.subjectType = getEntityType(subject);
	this.phrase = phrase;
	this.object = object;
	this.wikiObject = getWikipediaName(object);
	this.objectType = getEntityType(object);
	this.pre = pre;
	this.post = post;
	assignType();
    }

    /**
     * Assign the type based on the entities involved.
     */
    private void assignType(){
	
	if (isWikiEntity(this.subject) && isMVLEntity(this.object)){
	    this.type = TType.MVL;
	    return;
	}
	
	if (isWikiEntity(this.subject) && isWikiEntity(this.object)){
	    if (!getSubjectType().equals("[none]") && !getObjectType().equals("[none]")){
		this.type = TType.JOINABLE;
		return;
	    }
	    if (getSubjectType().equals("[none]") && getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_BOTH;
		return;
	    }
	    if (!getSubjectType().equals("[none]") && getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_OBJ;
		return;
	    }
	    if (getSubjectType().equals("[none]") && !getObjectType().equals("[none]")){
		this.type = TType.JOINABLE_NOTYPE_SBJ;
		return;
	    }
	}
	
	if (isNEREntity(this.subject) && isNEREntity(this.object)){
	    this.type = TType.NER_BOTH;
	    return;
	}
	
	if (isNEREntity(this.subject) && isWikiEntity(this.object)){
	    this.type = TType.NER_SBJ;
	    return;
	}
	
	if (isWikiEntity(this.subject) && isNEREntity(this.object)){
	    this.type = TType.NER_OBJ;
	    return;
	}
	
	this.type = TType.DROP;
    }


    /**
     * Reg-ex that defines primary (PE) o secondary 
     * entities (SE) that have a wikid and a specific type.
     * 
     * e.g. <PE-TITLE<Real_Madrid>>
     * 
     * @return 
     */
    private boolean isWikiEntity(String entity) {
	boolean isJoinable = false;
	Pattern joinableEntity = Pattern.compile("^<(PE|SE)-(AUG|ORG|SEED|TITLE|SUBTITLE|PRON|ALIAS)<[^>]*?>>$");
	Matcher m = joinableEntity.matcher(entity);
	if(m.matches()){
	    isJoinable = true;
	}
	return isJoinable;    
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
     * This method queries the KG to find possible relations between the entities.
     * We use the method only if a triple is JOINABLE.
     * 
     * @return
     */
    public Set<String> getLabels() {
	return Lector.getKg().getRelations(wikiSubject, wikiObject);
    }


    /**
     * This method queries the Types Assigner to find the type for the entities.
     * It assigns [none] in case of no type.
     * 
     * @return
     */
    public String getEntityType(String entity) {
	String type = "[none]";
	// make sure it makes sense to qyery the type
	if (isWikiEntity(entity) && !isMVLEntity(entity) && !isNEREntity(entity)){
	    type = Lector.getKg().getType(getWikipediaName(entity));
	}
	return type;
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
	WikiTriple other = (WikiTriple) obj;
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
     * @return the type
     */
    public TType getType() {
	return type;
    }

    /**
     * 
     */
    public void setType(TType type){
	this.type = type;
    }

    /**
     * @return the wikid
     */
    public String getWikid() {
	return wikid;
    }

    /**
     * @return the wikiSubject
     */
    public String getWikiSubject() {
        return wikiSubject;
    }

    /**
     * @return the wikiObject
     */
    public String getWikiObject() {
        return wikiObject;
    }

}
