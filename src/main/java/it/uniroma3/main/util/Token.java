package it.uniroma3.main.util;
/**
 * 
 * @author matteo
 *
 */
public class Token {

    private String renderedToken;
    private String POS;
    private String lemma;
    private String ner;
    
    /**
     * 
     * @param renderedToken
     * @param POS
     */
    public Token(String renderedToken, String POS){
	this.renderedToken = renderedToken;
	this.POS = POS;
    }
    
    /**
     * 
     * @param renderedToken
     * @param POS
     * @param lemma
     * @param ner
     */
    public Token(String renderedToken, String POS, String lemma, String ner){
	this.renderedToken = renderedToken;
	this.POS = POS;
	this.lemma = lemma;
	this.ner = ner;
    }

    /**
     * @return the renderedToken
     */
    public String getRenderedToken() {
        return renderedToken;
    }

    /**
     * @param renderedToken the renderedToken to set
     */
    public void setRenderedToken(String renderedToken) {
        this.renderedToken = renderedToken;
    }

    /**
     * @return the pOS
     */
    public String getPOS() {
        return POS;
    }

    /**
     * @param pOS the pOS to set
     */
    public void setPOS(String pOS) {
        POS = pOS;
    }

    /**
     * @return the lemma
     */
    public String getLemma() {
        return lemma;
    }

    /**
     * @param lemma the lemma to set
     */
    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    /**
     * @return the ner
     */
    public String getNer() {
        return ner;
    }

    /**
     * @param ner the ner to set
     */
    public void setNer(String ner) {
        this.ner = ner;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return renderedToken + "(" + POS + ")";
    }
    
    
}
