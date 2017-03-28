package it.uniroma3.triples;

import java.util.List;
/**
 * 
 * @author matteo
 *
 */
public class Sentence {

    private String text;
    private List<Triple> triples;
    private List<String> wikiEntities;
    private List<String> nerEntities;
    private boolean isMultiValues;
    
    public Sentence(String text, List<Triple> triples){
	this.text = text;
	this.triples = triples;
    }

    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the triples
     */
    public List<Triple> getTriples() {
        return triples;
    }

    /**
     * @param triples the triples to set
     */
    public void setTriples(List<Triple> triples) {
        this.triples = triples;
    }

    /**
     * @return the wikiEntities
     */
    public List<String> getWikiEntities() {
        return wikiEntities;
    }

    /**
     * @param wikiEntities the wikiEntities to set
     */
    public void setWikiEntities(List<String> wikiEntities) {
        this.wikiEntities = wikiEntities;
    }

    /**
     * @return the nerEntities
     */
    public List<String> getNerEntities() {
        return nerEntities;
    }

    /**
     * @param nerEntities the nerEntities to set
     */
    public void setNerEntities(List<String> nerEntities) {
        this.nerEntities = nerEntities;
    }

    /**
     * @return the isMultiValues
     */
    public boolean isMultiValues() {
        return isMultiValues;
    }

    /**
     * @param isMultiValues the isMultiValues to set
     */
    public void setMultiValues(boolean isMultiValues) {
        this.isMultiValues = isMultiValues;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((text == null) ? 0 : text.hashCode());
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
	Sentence other = (Sentence) obj;
	if (text == null) {
	    if (other.text != null)
		return false;
	} else if (!text.equals(other.text))
	    return false;
	return true;
    }
}
