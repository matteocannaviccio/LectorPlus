package it.uniroma3.model.model;

import java.util.HashMap;
import java.util.Map;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.DB;
/**
 * 
 * @author matteo
 *
 */
public class ModelNB extends Model{

    /*
     * A naive-bayes classifier can be:
     * TYPED_RELATIONS: where we handle normal relations and typed phrases such as [Person] was born in [Settlement];
     * CLASSIC: where we handle typed relations such as [Person] birthPlace [Settlement] and normal phrases;
     */
    public ModelNBType nbType;
    public enum ModelNBType {TYPED_RELATIONS, CLASSIC};

    private Map<String, Double> priors = new HashMap<String, Double>();
    private CounterMap<String> labeledFactsByRelation = new CounterMap<String>();
    private CounterMap<String> typeRelationCount = new CounterMap<String>();

    private Map<String, CounterMap<String>> typedRelationPhraseCount = new HashMap<String, CounterMap<String>>();
    private CounterMap<String> typeRelationPhraseCount = new CounterMap<String>();

    /**
     * 
     * @param db
     * @param typeNBModel
     * @param minRel
     * @param minFreq
     * @param minFreqWithR
     */
    public ModelNB(DB db, String labeled, int minFreq, ModelNBType typeNBModel) {
	super(db, labeled, PhraseType.NO_TYPES_PHRASES, minFreq);
	this.nbType = typeNBModel;
	createModel();
    }


    /**
     * A bayesian classifier model is essentially a generative model. Given a phrase, and a pair of types, 
     * it is able to estimate the probability of each relation to produce them.
     * 
     * @return
     */
    private void createModel(){
	if(verbose)
	    System.out.println("*** Loading " + nbType.name() + " NAIVE BAYES model ***");

	// get the count of each relation
	if(verbose)
	    System.out.print("\t-> Get relations count ... ");
	switch(nbType){
	case TYPED_RELATIONS:
	    labeledFactsByRelation = db_read.getTypedRelationsCount(this.available_phrases.keySet());
	    break;
	case CLASSIC:
	    labeledFactsByRelation = db_read.getCleanRelationCount(this.available_phrases.keySet());
	    break;
	}
	if(verbose)
	    System.out.println(labeledFactsByRelation.size() + " relations.");

	// fill the map of priors for each relation
	if(verbose)
	    System.out.println("\t-> Fill the map of priors for each relation.");
	int countLabeledFacts = db_read.countValues(labeledFactsByRelation);
	for(Map.Entry<String, Integer> entry : labeledFactsByRelation.entrySet()){
	    priors.put(entry.getKey(), (double) entry.getValue()/countLabeledFacts);
	}
	
	// get the count of each typed relation
	if(verbose)
	    System.out.print("\t-> Get typed relation count ... ");
	typeRelationCount = db_read.getTypedRelationsCount(this.available_phrases.keySet());
	if(verbose)
	    System.out.println(typeRelationCount.size() + " typed relations.");
	
	// get the count of each (typed, based on the type) relation and phrases
	if(verbose)
	    System.out.println("\t-> Get typed relation / phrases count ... ");
	switch(nbType){
	case TYPED_RELATIONS:
	    this.typedRelationPhraseCount = db_read.getTypedRelationPhrasesCount(this.available_phrases.keySet());
	    break;
	case CLASSIC:
	    this.typeRelationPhraseCount = db_read.getRelationTypesPhraseCounts(this.available_phrases.keySet());
	    break;
	}
	
	if(verbose)
	    System.out.println("**************************************");
    }

    /**
     * Returns the probability of a pair of types given the relation.
     * It is calculated as the number of times the types and the relation have been found 
     * in the labeled triples, divided by the number of times we find the relation only:
     * 
     * 			  	 |<-,ts,to,r> in LT|
     *      	P(<ts,to>|r) =	---------------------
     * 			  	  |<-,-,-,r> in LT|
     * 
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    public double probTypesGivenRel(String typeSubject, String typeObject, String relation){
	String toSerach = typeSubject + "###" + relation + "###" + typeObject;
	
	// NUMERATOR
	int numerator = 0;
	if (this.typeRelationCount.containsKey(toSerach))
	    numerator = this.typeRelationCount.get(toSerach);
	
	// DENOMINATOR
	int denominator = this.labeledFactsByRelation.get(relation);
	
	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }

    /**
     * Returns the probability of a phrase given the types and the relation.
     * It is calculated as the number of times the phrase, the types and the relation 
     * have been found in the labeled triples, divided by the number of times we find the 
     * types and the relation only:
     * 
     * 			 |<p,ts,to,r> in LT|
     *	        P(r) =  ---------------------
     * 			 |<-,ts,to,r> in LT|
     * 
     * @param phrase
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    public double probPhraseGivenTypesAndRel(String phrase, String typeSubject, String typeObject, String relation){
	String toSearch = phrase + "###" + typeSubject + "###" + relation + "###" + typeObject;
	
	// NUMERATOR
	int numerator = 0;
	if (this.typeRelationPhraseCount.containsKey(toSearch))
	    numerator = this.typeRelationPhraseCount.get(toSearch);

	// DENOMINATOR
	int denominator = 0;
	toSearch = typeSubject + "###" + relation + "###" + typeObject;
	if (this.typeRelationCount.containsKey(toSearch))
	    denominator = this.typeRelationCount.get(toSearch);
	
	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }

    /**
     * Returns the probability of a phrase given a typed relation.
     * It is calculated as the number of times the phrase and the typed relation 
     * have been found in the labeled triples, divided by the number of times we find the 
     * typed relation only:
     * 
     * 			 |<p,ts/r/to> in LT|
     *	        P(r) =  ---------------------
     * 			 |<-,ts/r/to> in LT|
     * 
     * @param phrase
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    public double probPhraseGivenTypedRel(String phrase, String relationToSearch){
	
	// NUMERATOR
	int numerator = 0;
	if (typedRelationPhraseCount.containsKey(relationToSearch))
	    if (typedRelationPhraseCount.get(relationToSearch).containsKey(phrase))
		numerator = typedRelationPhraseCount.get(relationToSearch).get(phrase);

	// DENOMINATOR
	int denominator = 0;
	if (labeledFactsByRelation.containsKey(relationToSearch))
	    denominator = labeledFactsByRelation.get(relationToSearch);

	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }

    /**
     * 
     * @param phrase
     * @param relation
     * @param subjectType
     * @param objectType
     * @return
     */
    public double getProbabilityClassic(String phrase, String relation, String typeSubject, String typeObject){
	return  this.probTypesGivenRel(typeSubject, typeObject, relation) *
		this.probPhraseGivenTypesAndRel(phrase, typeSubject, typeObject, relation) * 
		this.priors.get(relation);
    }

    /**
     * 
     * @param phrase
     * @param typedRelation
     * @return
     */
    public double getProbabilityTypedRelation(String phrase, String typedRelation){
	return  priors.get(typedRelation) * 
		probPhraseGivenTypedRel(phrase, typedRelation);
    }


    @Override
    public Pair<String, Double> predictRelation(WikiTriple t) {
	String relation = null;
	double prob = 0.0;
	
	Map<String, Double> ranking = new HashMap<String, Double>();
	for (String r : priors.keySet()){
	    if (this.getType().equals(ModelNBType.TYPED_RELATIONS)){
		prob = getProbabilityTypedRelation(t.getPhrasePlaceholders(), r);
	    }else
		prob = getProbabilityClassic(t.getPhrasePlaceholders(), r, t.getSubjectType(), t.getObjectType());
	    if (prob > 0.0)
		ranking.put(r, prob);
	}

	if (ranking.size() > 0){
	    relation = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getKey();
	    prob = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getValue();
	}

	return Pair.make(relation, prob);
    }
    

    @Override
    public boolean canPredict(String expectedRelation) {
	return true;
    }

}
