package it.uniroma3.lectorplus;

import java.util.List;

/**
 * 
 * @author matteo
 *
 */
public class WikiArticle {
    
    private String wikid;
    private String id;
    private String content;
    private List<String> aliases;
    private String firstSentence;
    
    /**
     * 
     * @param wikid
     * @param id
     * @param content
     */
    public WikiArticle(String wikid, String id, String content) {
	this.wikid = wikid;
	this.id = id;
	this.content = content;
    }

    /**
     * @return the wikid
     */
    protected String getWikid() {
        return wikid;
    }

    /**
     * @param wikid the wikid to set
     */
    protected void setWikid(String wikid) {
        this.wikid = wikid;
    }

    /**
     * @return the id
     */
    protected String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    protected void setId(String id) {
        this.id = id;
    }

    /**
     * @return the content
     */
    protected String getContent() {
        return content;
    }

    /**
     * @param content the content to set
     */
    protected void setContent(String content) {
        this.content = content;
        this.firstSentence = content.split("\\.")[0];
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
    protected List<String> getAliases() {
        return aliases;
    }

    /**
     * @param aliases the aliases to set
     */
    protected void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "WikiArticle \n"
		+ "[wikid=" + wikid + ",\n"
		+ " id=" + id + ",\n"
		+ " aliases=" + aliases + ",\n"
		+ " first sentence= " + firstSentence + "]";
    }

 

   
    
    
    

}
