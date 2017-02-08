package it.uniroma3.lectorplus;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
/**
 * 
 * @author matteo
 *
 */
public class WikiArticle {

    private String wikid;
    private String id;
    private String namespace;
    private String title;
    private String url;

    private List<String> aliases;
    private ArticleType type;
    private String disambiguation;

    // for the italian language
    private String bio;

    // article composite structures
    private Map<String, List<String>> text;
    private transient Map<String, List<String>> tables;
    private transient Map<String, List<String>> lists;

    private static transient Gson gson = new Gson();

    public enum ArticleType {
	TEMPLATE, ARTICLE, CATEGORY, DISCUSSION, REDIRECT, DISAMBIGUATION, UNKNOWN, MAIN, LIST, PROJECT, PORTAL, FILE, HELP
    };

    /**
     * 
     * @param wikid
     * @param id
     * @param text
     */
    public WikiArticle(String wikid, String id, String title, String namespace, WikiLanguage lang) {
	this.wikid = wikid;
	this.id = id;
	this.title = title;
	this.namespace = namespace;
	this.url = "http://" + lang.toString() + ".wikipedia.org/wiki/" + wikid;
    }

    /**
     * @return the wikid
     */
    public String getWikid() {
	return wikid;
    }

    /**
     * @param wikid the wikid to set
     */
    public void setWikid(String wikid) {
	this.wikid = wikid;
    }

    /**
     * @return the id
     */
    public String getId() {
	return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
	this.id = id;
    }

    /**
     * @return the content
     */
    public Map<String, List<String>> getContent() {
	return text;
    }

    /**
     * @param content the content to set
     */
    public void setContent(Map<String, List<String>> content) {
	this.text = content;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((wikid == null) ? 0 : wikid.hashCode());
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
	WikiArticle other = (WikiArticle) obj;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	if (wikid == null) {
	    if (other.wikid != null)
		return false;
	} else if (!wikid.equals(other.wikid))
	    return false;
	return true;
    }

    /**
     * @return the aliases
     */
    public List<String> getAliases() {
	return aliases;
    }

    /**
     * @param aliases the aliases to set
     */
    public void setAliases(List<String> aliases) {
	this.aliases = aliases;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "WikiArticle \n"
		+ "[wikid=" + this.wikid + ",\n"
		+ " url=" + this.url + ",\n"
		+ " title=" + this.title + ",\n"
		+ " id=" + this.id + ",\n"
		+ " namespace=" + this.namespace + ",\n"
		+ " aliases=" + this.aliases + ",\n"
		+ " type= " + this.type + ",\n"
		+ " disambiguation= " + this.disambiguation + ",\n"
		+ " bio= " + this.bio + ",\n"
		//+ " table= " + getTables() + ",\n"
		//+ " list= " + getLists() + ",\n"
		+ " text= " + "\n" + getText() + "]";
    }

    /**
     * 
     * @return
     */
    private String getText(){
	StringBuffer sb = new StringBuffer();
	for(Map.Entry<String, List<String>> paragraph : this.text.entrySet()){
	    for(String sentence : paragraph.getValue()){
		sb.append(paragraph.getKey() + "\t" + sentence + "\n");
	    }
	}
	return sb.toString();
    }

 
    /**
     * @return the type
     */
    public ArticleType getType() {
	return type;
    }


    /**
     * @param type the type to set
     */
    public void setType(ArticleType type) {
	this.type = type;
    }


    /**
     * @return the namespace
     */
    public String getNamespace() {
	return namespace;
    }


    /**
     * @param namespace the namespace to set
     */
    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }


    /**
     * @return the disambiguation
     */
    public String getDisambiguation() {
	return disambiguation;
    }


    /**
     * @param disambiguation the disambiguation to set
     */
    public void setDisambiguation(String disambiguation) {
	this.disambiguation = disambiguation;
    }


    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }


    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @return the bio
     */
    public String getBio() {
	return bio;
    }

    /**
     * @param bio the bio to set
     */
    public void setBio(String bio) {
	this.bio = bio;
    }

    /**
     * @param tables the tables to set
     */
    public void setTables(Map<String, List<String>> tables) {
	this.tables = tables;
    }


    /**
     * @param lists the lists to set
     */
    public void setLists(Map<String, List<String>> lists) {
	this.lists = lists;
    }

    /**
     * 
     * @return
     */
    public String toJson() {
	return gson.toJson(this);
    }

    /**
     * 
     * @param json
     * @return
     */
    public static WikiArticle fromJson(String json) {
	return gson.fromJson(json, WikiArticle.class);

    }

}
