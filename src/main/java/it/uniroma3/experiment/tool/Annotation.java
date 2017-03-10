package it.uniroma3.experiment.tool;

import com.google.gson.annotations.SerializedName;

public class Annotation {

    @SerializedName("@URI")
    private String uri;
    private String wikid;

    @SerializedName("@support")
    private int support;

    @SerializedName("@surfaceForm")
    private String surfaceForm;

    @SerializedName("@@similarityScore")
    private double similarityScore;

    /**
     * @return the uri
     */
    public String getUri() {
	return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
	this.uri = uri;
    }

    /**
     * @return the support
     */
    public int getSupport() {
	return support;
    }

    /**
     * @param support the support to set
     */
    public void setSupport(int support) {
	this.support = support;
    }

    /**
     * @return the surfaceForm
     */
    public String getSurfaceForm() {
	return surfaceForm;
    }

    /**
     * @param surfaceForm the surfaceForm to set
     */
    public void setSurfaceForm(String surfaceForm) {
	this.surfaceForm = surfaceForm;
    }

    /**
     * @return the similarityScore
     */
    public double getSimilarityScore() {
	return similarityScore;
    }

    /**
     * @param similarityScore the similarityScore to set
     */
    public void setSimilarityScore(double similarityScore) {
	this.similarityScore = similarityScore;
    }

    /**
     * 
     */
    public void setWikid() {
	this.wikid = uri.replaceAll("http://dbpedia.org/resource/", "");
    }

    /**
     * 
     */
    public String toString(String type){
	return this.surfaceForm + "\t" + type + "<" + this.wikid + ">";

    }

    /**
     * @return the wikid
     */
    public String getWikid() {
	return wikid;
    }




}
