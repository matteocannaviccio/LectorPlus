package it.uniroma3.model.model;

import java.util.Map;
import java.util.Set;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.QueryDB;
/**
 * 
 * @author matteo
 *
 */
public abstract class Model{

    // print the computation
    protected static boolean verbose = true;
    
    // the type of the phrases used in the model
    protected PhraseType type;
    public enum PhraseType {TYPED_PHRASES, NO_TYPES_PHRASES};
    
    // these are all the phrases that are considered in the model
    protected CounterMap<String> available_phrases;    
    protected QueryDB db_read;
    protected int minFreq;

    /**
     * A model takes a db (that can be a DBModel or a DBCrossValidation), the name of the labeled table
     * (it changes for the cross-validation) and the parameters fro creating the model.
     * @param db
     * @param type
     * @param minFreq
     * @param minFreqWithR
     */
    public Model(DB dbModel, String labeled, PhraseType type, int minFreq){
	if(verbose)
	    System.out.println("**** Filtering " + type +" phrases by (minFreq: " + minFreq +") ****");
	    
	this.db_read = new QueryDB(dbModel, labeled);
	this.type = type;
	this.minFreq = minFreq;
	this.available_phrases = availabePhrases(minFreq);

	if(verbose){
	    System.out.println(" --> "+ this.available_phrases.size() + " phrases.");
	}
    }
    
    /**
     * @return the type
     */
    public PhraseType getType() {
	return type;
    }


    /**
     * Obtain the vocabulary of labeled phrases that exists more than minFreq times.
     * 
     * @param minFreq
     * @return
     */
    private CounterMap<String> availabePhrases(int minFreq) {
	CounterMap<String> available_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    available_phrases = db_read.getAvailableTypedPhrases(minFreq);
	    break;
	case NO_TYPES_PHRASES:
	    available_phrases = db_read.getAvailablePhrases(minFreq);
	    break;
	}
	return available_phrases;
    }
    
    /**
     * Obtain the vocabulary of unlabeled phrases.
     * 
     * @param lab_phrases
     * @return
     */
    protected CounterMap<String> availableUnlabeledPhrases(Set<String> lab_phrases){
	CounterMap<String> unlab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    unlab_phrases = db_read.getUnlabeledTypedPhrasesCount(lab_phrases);
	    break;
	case NO_TYPES_PHRASES:
	    unlab_phrases = db_read.getUnlabeledPhrasesCount(lab_phrases);
	    break;
	}
	return unlab_phrases;
    } 

    /**
     * Obtain the mapping counts between relation and (typed) phrases. 
     * 
     * @param lab_phrases
     * @return
     */
    protected Map<String, CounterMap<String>> availableRelations2phrases(Set<String> lab_phrases){
	Map<String, CounterMap<String>> relations2phrasesAndCount = null;
	switch(type){
	case TYPED_PHRASES:
	    relations2phrasesAndCount = db_read.getRelationTypedPhrasesCount(lab_phrases);
	    break;
	case NO_TYPES_PHRASES:
	    relations2phrasesAndCount = db_read.getRelationPhrasesCount(lab_phrases);
	    break;
	}
	return relations2phrasesAndCount;
    } 

    /**
     * Obtain the mapping counts between (typed) phrases and relations. 
     * 
     * @param lab_phrases
     * @return
     */
    protected Map<String, CounterMap<String>> availablePhrases2relations(Set<String> lab_phrases){
	Map<String, CounterMap<String>> relations2phrasesAndCount = null;
	switch(type){
	case TYPED_PHRASES:
	    relations2phrasesAndCount = db_read.getTypedPhrasesRelationsCount(lab_phrases);
	    break;
	case NO_TYPES_PHRASES:
	    relations2phrasesAndCount = db_read.getPhrasesRelationsCount(lab_phrases);
	    break;
	}
	return relations2phrasesAndCount;
    } 


    /**
     * This is the only method that can be used from the clients.
     * 
     * @param phrase
     * @return
     */
    public abstract Pair<String, Double> predictRelation(WikiTriple t);
    


}