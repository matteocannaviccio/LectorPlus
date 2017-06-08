package it.uniroma3.model.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

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
    protected CounterMap<String> unlabeled_phrases;
    protected Map<String, CounterMap<String>> relation2phrasesCount;
    
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
	this.db_read = new QueryDB(dbModel, labeled);
	this.type = type;
	this.minFreq = minFreq;

	System.out.println("---------------------------------------------------------------");
	System.out.println("Scoring model");
	System.out.println("---------------------------------------------------------------");
	System.out.printf("%-30s %s\n", "model: ", "LectorScore with " + type);
	System.out.printf("%-30s %s\n", "minFreq: ", minFreq);
	this.available_phrases = availabePhrases(minFreq);
	System.out.printf("%-30s %s\n", "labeled phrases: ", this.available_phrases.keySet().size());
	this.unlabeled_phrases = availableUnlabeledPhrases(available_phrases.keySet());
	System.out.printf("%-30s %s\n", "un-labeled phrases: ", unlabeled_phrases.keySet().size());
	this.relation2phrasesCount = availableRelations2phrases(available_phrases.keySet());
	System.out.printf("%-30s %s\n", "relations: ", relation2phrasesCount.keySet().stream().map(s -> s.replace("(-1)", "")).collect(Collectors.toSet()).size());
	System.out.printf("%-30s %s\n", "avg. phrases/relation: ", String.format("%.2f", calcAvgValue(relation2phrasesCount)) + " p/r");
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
	CounterMap<String> phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    phrases = db_read.getAvailableTypedPhrases(minFreq);
	    break;
	case NO_TYPES_PHRASES:
	    phrases = db_read.getAvailablePhrases(minFreq);
	    break;
	}
	return phrases;
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
     * Obtain the mapping counts between (typed) phrases and relations. 
     * 
     * @param lab_phrases
     * @return
     */
    protected CounterMap<String> availableRelations(Set<String> lab_phrases){
	CounterMap<String> labeledFactsByRelation = null;
	switch(this.type){
	case TYPED_PHRASES:
	    System.out.println("-> get typed relations count");
	    labeledFactsByRelation = db_read.getCleanRelationCountByTypedPhrases(this.available_phrases.keySet());
	    break;

	case NO_TYPES_PHRASES:
	    System.out.println("-> get relations count");
	    labeledFactsByRelation = db_read.getCleanRelationCount(this.available_phrases.keySet());
	    break;
	}
	System.out.println("-> "+ labeledFactsByRelation.size() +" relations.");
	return labeledFactsByRelation;
    }
    

    /**
     * 
     * @param map
     * @return
     */
    private double calcAvgValue(Map<String, CounterMap<String>> map){
	int countPhrases = 0;
	for (Map.Entry<String, CounterMap<String>> entry : map.entrySet()){
	    countPhrases += entry.getValue().size();
	}
	return (double)countPhrases/map.size();
    }

    /**
     * This is the only method that can be used from the clients.
     * 
     * @param phrase
     * @return
     */
    public abstract Pair<String, Double> predictRelation(WikiTriple t);

    /**
     * 
     * @param expectedRelation
     * @return
     */
    public abstract boolean canPredict(String expectedRelation);
    
}
