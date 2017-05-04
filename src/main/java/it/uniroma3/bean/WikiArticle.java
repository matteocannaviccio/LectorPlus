package it.uniroma3.bean;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 *
 * 
 * 
 * @author matteo
 *
 */
public class WikiArticle {

    // Info about the article (maybe a bit redundant)
    private String wikid;
    private String id;
    private String namespace;
    private String title;
    private String url;
    private ArticleType type;
    private transient String originalMarkup;

    // Primary entity replacements
    private String pronoun;
    private String subName;
    private List<String> aliases;
    private List<String> seeds;
    private String disambiguation;

    // Secondary entities replacements 
    private Map<String, Set<String>> wikilinks;

    // content
    private Map<String, String> blocks;
    private Map<String, List<String>> sentences; // used only for DBpedia Spotlight
    private String firstSentence;

    // Article composite structures
    private Map<String, List<String>> tables;
    private Map<String, List<String>> lists;
    
    // DBpedia types
    private List<String> dbpedia_types;

    private static transient Gson gson_pp = new GsonBuilder()
	    .disableHtmlEscaping()
	    .setPrettyPrinting()
	    .create();
    
    private static transient Gson gson = new GsonBuilder()
	    .disableHtmlEscaping()
	    .create();

    public enum ArticleType {
	TEMPLATE, ARTICLE, CATEGORY, DISCUSSION, REDIRECT, DISAMBIGUATION, 
	DATE, OUTLINE, LIST, PROJECT, PORTAL, FILE, HELP, ERROR
    };

    /**
     * 
     * @param wikid
     * @param id
     * @param text
     */
    public WikiArticle(String wikid, String id, String title, String namespace, String langCode, String originalMarkup) {
	this.wikid = wikid;
	this.id = id;
	this.title = title;
	this.namespace = namespace;
	this.url = "http://" + langCode + ".wikipedia.org/wiki/" + wikid;
	this.wikilinks = new HashMap<String, Set<String>>();
	this.originalMarkup = originalMarkup;
    }
    
    /**
     * 
     * @return
     */
    public static WikiArticle makeDummyArticle() { 
	return new WikiArticle("dummy", "dummy", "dummy", "dummy", "dummy", "dummy");
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
    public Map<String, List<String>> getSentences() {
	if (this.sentences == null)
	    this.sentences = new LinkedHashMap<String, List<String>>();
	return sentences;
    }
    
    /**
     * 
     * @return
     */
    public String getWholeText(){
	StringBuffer sb = new StringBuffer();
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    sb.append(block + "\n");
	}
	return sb.toString();
    }

    /**
     * @param content the content to set
     */
    public void setSentences(Map<String, List<String>> sentences) {
	this.sentences = sentences;
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
	return this.toJsonPP();
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
     * @return
     */
    public String toJsonPP() {
	return gson_pp.toJson(this);
    }

    /**
     * 
     * @param json
     * @return
     */
    public static WikiArticle fromJson(String json) {
	return gson.fromJson(json, WikiArticle.class);

    }

    /**
     * @return the wikilinks
     */
    public Map<String, Set<String>> getWikilinks() {
	return wikilinks;
    }

    /**
     * @param wikilinks the wikilinks to set
     */
    public void addWikilinks(Map<String, Set<String>> wikilinks) {
	for(Map.Entry<String, Set<String>> link : wikilinks.entrySet()){
	    if(this.wikilinks.containsKey(link.getKey())){
		this.wikilinks.get(link.getKey()).addAll(link.getValue());
	    }else{
		this.wikilinks.put(link.getKey(), link.getValue());
	    }
	}
    }

    /**
     * @return the tables
     */
    public Map<String, List<String>> getTables() {
	return tables;
    }

    /**
     * @return the lists
     */
    public Map<String, List<String>> getLists() {
	return lists;
    }

    /**
     * @return the seeds
     */
    public List<String> getSeeds() {
	return seeds;
    }

    /**
     * @param seeds the seeds to set
     */
    public void setSeeds(List<String> seeds) {
	this.seeds = seeds;
    }

    public String getPrimaryTag(){
	return "PE-TITLE<" + this.wikid + ">";
    }

    /**
     * @return the pronoun
     */
    public String getPronoun() {
	return pronoun;
    }

    /**
     * @param pronoun the pronoun to set
     */
    public void setPronoun(String pronoun) {
	this.pronoun = pronoun;
    }

    /**
     * @return the blocks
     */
    public Map<String, String> getBlocks() {
	return blocks;
    }

    /**
     * @param blocks the blocks to set
     */
    public void setBlocks(Map<String, String> blocks) {
	this.blocks = blocks;
    }

    /**
     * @return the originalMarkup
     */
    public String getOriginalMarkup() {
	return originalMarkup;
    }

    /**
     * @param originalMarkup the originalMarkup to set
     */
    public void setOriginalMarkup(String originalMarkup) {
	this.originalMarkup = originalMarkup;
    }

    /**
     * @return the firstSentence
     */
    public String getFirstSentence() {
	return firstSentence;
    }

    /**
     * @param firstSentence the firstSentence to set
     */
    public void setFirstSentence(String firstSentence) {
	this.firstSentence = firstSentence;
    }

    /**
     * @return the subName
     */
    public String getSubName() {
	return subName;
    }

    /**
     * @param subName the subName to set
     */
    public void setSubName(String subName) {
	this.subName = subName;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the types
     */
    public List<String> getDBpediaTypes() {
        return dbpedia_types;
    }

    /**
     * @param types the types to set
     */
    public void setDBpediaTypes(List<String> types) {
        this.dbpedia_types = types;
    }





}
