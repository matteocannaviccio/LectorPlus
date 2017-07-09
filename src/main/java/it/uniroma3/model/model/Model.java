package it.uniroma3.model.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.CRUD;
/**
 * This is an abstract model. Each possible implementation of a model extends it.
 * In order to create a new model, we always need to filter out noisy phrases,
 * either if we use TYPED_PHRASES or NO_TYPES_PHRASES kind of phrases: 
 * we filter out them using minF parameter retrieving the available_phrases list.
 * 
 * Then, using such available_phrases we are able to retrieve:
 *  - p_LT_counts: phrases that are labeled with a relation and their counts
 *  - p_UT_counts: phrases that are not labeled with a relation and their counts
 *  - r_counts: counts of relations that are involved in the model
 *  
 *  We also build two mappings that are useful for some implementations, such as:
 *   - relation2phrasesCount: map the relations to the relative counted phrases
 *   - phrases2relationsCount: map the phrases to the relative counted relations
 * 
 * 
 * @author matteo
 *
 */
public abstract class Model{

    // print the computation
    protected static boolean verbose = false;
    protected CRUD crud;

    // those are the possible variations of the model.
    // It can uses typed phrases (e.g. [Person] was born in [Settlement]) or not (e.g. was born in).
    public enum PhraseType {TYPED_PHRASES, NO_TYPES_PHRASES};
    public enum ModelType {BM25, NB, NBind, TextExtChallenge, NBfilter};

    /*
     * these are the phrases that are contained in the model which are present at least minFreq
     * times in the whole Wikipedia.
     */
    protected PhraseType type;
    protected int minF;
    protected CounterMap<String> p_available;
    protected CounterMap<String> pt_LT_counts;
    protected CounterMap<String> pt_UT_counts;

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
	if (verbose){
	    System.out.println("\nScoring model");
	    System.out.println("-------------");
	}
	this.crud = new CRUD(dbModel, labeled);
	this.type = type;
	this.minF = minF;
	initializeModel(minF);
    }

    /**
     * Initializes all the phrases that are included in the model, considering
     * the threshold that filter out rare phrases (i.e. minFreq).
     * 
     * @param minFreq
     */
    private void initializeModel(int minFreq){
	this.p_available = crud.getAvailablePhrases(minFreq);
	this.pt_LT_counts = getAllLabeledPhrases(p_available.keySet());
	this.pt_UT_counts = getAllUnlabeledPhrases(p_available.keySet());

	if (verbose){
	    System.out.printf("\t%-35s %s\n", "initializing model: ", type);
	    System.out.printf("\t%-35s %s\n", "minFreq: ", minFreq);
	    System.out.printf("\t%-35s %s\n", "n° different available phrases: ", this.p_available.size());
	    System.out.printf("\t%-35s %s\n", "n° different labeled phrases: ", calculateSum(this.pt_LT_counts));
	    System.out.printf("\t%-35s %s\n", "n° different un-labeled phrases: ", calculateSum(this.pt_UT_counts));
	}
    }

    /**
     * @return the type
     */
    public PhraseType getType() {
	return type;
    }

    /**
     * Obtain the vocabulary of labeled phrases.
     * 
     * @param available
     * @return
     */
    protected CounterMap<String> getAllLabeledPhrases(Set<String> available){
	CounterMap<String> lab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    lab_phrases = crud.getPT_LT_counts(available);
	    break;
	case NO_TYPES_PHRASES:
	    lab_phrases = p_available;
	    break;
	}
	return lab_phrases;
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
	    unlab_phrases = crud.getPT_UT_counts(available);
	    break;
	case NO_TYPES_PHRASES:
	    unlab_phrases = crud.getP_UT_counts(available);
	    break;
	}
	return unlab_phrases;
    } 


    /**
     * 
     * @param marks
     * @return
     */
    protected CounterMap<String> calculateRelationsCount(Map<String, CounterMap<String>> relations2phrasesCount) {
	CounterMap<String> relCounts = new CounterMap<String>();
	for(Map.Entry<String, CounterMap<String>> entry : relations2phrasesCount.entrySet()){
	    if (!entry.getKey().equals("NONE"))
		relCounts.put(entry.getKey(), calculateSum(entry.getValue().values()));
	}
	return relCounts;
    }


    /**
     * 
     * @param marks
     * @return
     */
    protected int calculateSum(Collection<Integer> marks) {
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
     * @param marks
     * @return
     */
    protected int calculateSum(CounterMap<String> marks) {
	int sum = 0;
	for (String entry : marks.keySet()){
	    sum += marks.get(entry);
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
     * This is the only method that can be used from the clients.
     * 
     * @param phrase
     * @return
     */
    public abstract Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder, String object_type);

    /**
     * 
     * @param expectedRelation
     * @return
     */
    public abstract boolean canPredict(String expectedRelation);

}
