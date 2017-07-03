package it.uniroma3.model.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import it.uniroma3.config.Configuration;
import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;

import it.uniroma3.model.DB;
import it.uniroma3.model.db.CRUD;
/**
 * 
 * @author matteo
 *
 */
public abstract class Model{

    // print the computation
    protected static boolean verbose = true;
    
    // those are the possible variations of the model.
    // It can uses typed phrases (e.g. [Person] was born in [Settlement]) or not (e.g. was born in).
    public enum PhraseType {TYPED_PHRASES, NO_TYPES_PHRASES};
    public enum ModelType {LectorScore, BM25, NB, TextExtChallenge};
    
    /*
     * these are the phrases that are contained in the model which are present at least minFreq
     * times in the whole Wikipedia.
     */
    protected int minF;
    protected CounterMap<String> available_phrases;
    
    /*
     * Those are the associations between relation and (typed) phrases.
     */
    protected PhraseType type;
    protected CounterMap<String> labeled_phrases;
    protected CounterMap<String> unlabeled_phrases;
    protected CounterMap<String> labeled_relations;
    protected Map<String, CounterMap<String>> relation2phrasesCount;
    protected Map<String, CounterMap<String>> phrases2relationsCount;

    protected CRUD db_read;

    /**
     * A model takes a db (that can be a DBModel or a DBCrossValidation), the name of the labeled table
     * (note that it can change for the cross-validation) and the parameters for creating the model.
     * 
     * @param db
     * @param type
     * @param minFreq
     * @param minFreqWithR
     */
    public Model(DB dbModel, String labeled, PhraseType type, int minF){
	System.out.println("\nScoring model");
	System.out.println("-------------");
	
	this.db_read = new CRUD(dbModel, labeled);
	this.type = type;
	this.minF = minF;
	initializeModel(minF);
	System.out.printf("\t%-35s %s\n", "model: ", Configuration.getLectorModelName() + " with " + type);
	System.out.printf("\t%-35s %s\n", "minFreq: ", minF);
	System.out.printf("\t%-35s %s\n", "available seed-phrases: ", this.available_phrases.size());
	System.out.printf("\t%-35s %s\n", "labeled phrases: ", this.labeled_phrases.keySet().size());
	System.out.printf("\t%-35s %s\n", "un-labeled phrases: ", this.unlabeled_phrases.keySet().size());
	System.out.printf("\t%-35s %s\n", "relations: ", this.relation2phrasesCount.keySet().stream().map(s -> s.replace("(-1)", "")).collect(Collectors.toSet()).size());
	System.out.printf("\t%-35s %s\n", "avg. phrases/relation (before): ", String.format("%.2f", calcAvgValue(relation2phrasesCount)) + " p/r");
    }
    
    /**
     * Initializes all the phrases that are included in the model, considering
     * the threshold that filter out rare phrases (i.e. minFreq).
     * 
     * @param minFreq
     */
    private void initializeModel(int minFreq){
	this.available_phrases = availabePhrases(minFreq);
	this.labeled_phrases = getAllLabeledPhrases(available_phrases.keySet());
	this.unlabeled_phrases = getAllUnlabeledPhrases(available_phrases.keySet());
	this.relation2phrasesCount = getAllRelations2phrases(available_phrases.keySet());
	this.phrases2relationsCount = getAllPhrases2relations(available_phrases.keySet());
	this.labeled_relations = calculateRelationsCount(relation2phrasesCount);
    }
    
    /**
     * @return the type
     */
    public PhraseType getType() {
	return type;
    }


    /**
     * Obtain the vocabulary of labeled phrases that exists more than minFreq times.
     * We always consider un-typed phrases in this filtering.
     * 
     * @param minFreq
     * @return
     */
    private CounterMap<String> availabePhrases(int minFreq) {
	return db_read.getAvailablePhrases(minFreq);
    }
    
    /**
     * Obtain the vocabulary of labeled phrases.
     * 
     * @param available
     * @return
     */
    protected CounterMap<String> getAllLabeledPhrases(Set<String> available){
	CounterMap<String> unlab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    unlab_phrases = db_read.getLabeledTypedPhrasesCount(available);
	    break;
	case NO_TYPES_PHRASES:
	    unlab_phrases = db_read.getLabeledPhrasesCount(available);
	    break;
	}
	return unlab_phrases;
    } 
    
    /**
     * Obtain the vocabulary of unlabeled phrases.
     * 
     * @param lab_phrases
     * @return
     */
    protected CounterMap<String> getAllUnlabeledPhrases(Set<String> available){
	CounterMap<String> unlab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    unlab_phrases = db_read.getUnlabeledTypedPhrasesCount(available);
	    break;
	case NO_TYPES_PHRASES:
	    unlab_phrases = db_read.getUnlabeledPhrasesCount(available);
	    break;
	}
	return unlab_phrases;
    } 

    /**
     * Obtain the mapping counts between relation and (typed) phrases. 
     * 
     * @param available
     * @return
     */
    protected Map<String, CounterMap<String>> getAllRelations2phrases(Set<String> available){
	Map<String, CounterMap<String>> relations2phrasesAndCount = null;
	switch(type){
	case TYPED_PHRASES:
	    relations2phrasesAndCount = db_read.getRelationTypedPhrasesCount(available);
	    break;
	case NO_TYPES_PHRASES:
	    relations2phrasesAndCount = db_read.getRelationPhrasesCount(available);
	    break;
	}
	return relations2phrasesAndCount;
    } 

    /**
     * Obtain the mapping counts between (typed) phrases and relations. 
     * 
     * @param available
     * @return
     */
    protected Map<String, CounterMap<String>> getAllPhrases2relations(Set<String> available){
	Map<String, CounterMap<String>> phrases2relationsAndCount = null;
	switch(type){
	case TYPED_PHRASES:
	    phrases2relationsAndCount = db_read.getTypedPhrasesRelationsCount(available);
	    break;
	case NO_TYPES_PHRASES:
	    phrases2relationsAndCount = db_read.getPhrasesRelationsCount(available);
	    break;
	}
	return phrases2relationsAndCount;
    }
    
    /**
     * 
     * @param marks
     * @return
     */
    private CounterMap<String> calculateRelationsCount(Map<String, CounterMap<String>> relations2phrasesCount) {
	CounterMap<String> relCounts = new CounterMap<String>();
	for(Map.Entry<String, CounterMap<String>> entry : relations2phrasesCount.entrySet()){
	    relCounts.put(entry.getKey(), calculateSum(entry.getValue().values()));
	}
	return relCounts;
    }
    
    /**
     * 
     * @param marks
     * @return
     */
    private int calculateSum(Collection<Integer> marks) {
	int sum = 0;
	if(!marks.isEmpty()) {
	    for (Integer mark : marks) {
		sum += mark;
	    }
	}
	return sum;
    }
    

    /**
     * 
     * @param map
     * @return
     */
    protected double calcAvgValue(Map<String, CounterMap<String>> map){
	int countPhrases = 0;
	for (Map.Entry<String, CounterMap<String>> entry : map.entrySet()){
	    countPhrases += entry.getValue().size();
	}
	return (double)countPhrases/map.size();
    }
    
    /**
     * 
     * @param map
     * @return
     */
    protected double calcAvgValueAfterCutoff(Map<String, String> map){
	int countPhrases = 0;
	
	CounterMap<String> relations = new CounterMap<>();
	for (String relation : map.values())
	    relations.add(relation);
	
	for (String entry : relations.keySet()){
	    countPhrases += relations.get(entry);
	}
	return (double)countPhrases/relations.size();
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
