package it.uniroma3.model;

import java.util.HashMap;
import java.util.Map;

import it.uniroma3.triples.WikiTriple;
import it.uniroma3.util.Ranking;
/**
 * 
 * @author matteo
 *
 */
public class ModelNB extends Model{

    /*
     * This is the type of the score-based model. It can be:
     * TYPED_PHRASES: where we handle normal relations and typed phrases such as [Person] was born in [Settlement];
     * TYPED_RELATIONS: where we handle typed relations such as [Person] birthPlace [Settlement] and normal phrases;
     * NO_TYPES: where we handle normal relations and normal phrases.
     */
    public ModelNBType type;
    public enum ModelNBType {TYPED_RELATIONS, CLASSIC};


    private QueryDB db_read;

    private Map<String, Double> priors;
    private Map<String, Integer> labeledFactsByRelation;

    private Map<String, Map<String, Integer>> typedRelationPhraseCount;
    private Map<String, Integer> typeRelationCount;
    private Map<String, Integer> typeRelationPhraseCount;

    /**
     * 
     * @param db
     */
    public ModelNB(DBlite db, ModelNBType type, int minRel) {
	super();
	this.type = type;
	this.db_read = new QueryDB(db);
	priors = new HashMap<String, Double>();

	createModel(minRel);
    }

    /**
     * 
     * @return
     */
    private ModelNBType getType() {
	return this.type;
    }


    /**
     * A bayesian classifier model is essentially a generative model. Given a phrase, and a pair of types, 
     * it is able to estimate the probability of each relation to produce them.
     * 
     * @return
     */
    private void createModel(int minRel){
	System.out.println("Loading " + type.name() + " naive bayes model ...");
	
	switch(type){
	case TYPED_RELATIONS:
	    labeledFactsByRelation = db_read.getTypedRelationsCount(minRel);
	    break;
	case CLASSIC:
	    labeledFactsByRelation = db_read.getCleanRelationCount(minRel);
	    break;
	}

	// fill the map of priors for each relation
	int countLabeledFacts = db_read.countValues(labeledFactsByRelation);
	for(Map.Entry<String, Integer> entry : labeledFactsByRelation.entrySet()){
	    priors.put(entry.getKey(), (double) entry.getValue()/countLabeledFacts);
	}

	switch(type){
	case TYPED_RELATIONS:
	    this.typedRelationPhraseCount = db_read.getTypedRelationPhrasesCount(null);
	    break;
	case CLASSIC:
	    this.typeRelationPhraseCount = db_read.getRelationTypesPhraseCounts(minRel);
	    break;
	}
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
	int numerator = 0;
	if (this.typeRelationCount.containsKey(toSerach))
	    numerator = this.typeRelationCount.get(toSerach);
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
	int numerator = 0;
	if (this.typeRelationPhraseCount.containsKey(toSearch))
	    numerator = this.typeRelationPhraseCount.get(toSearch);
	toSearch = typeSubject + "\t" + relation + "\t" + typeObject;
	int denominator = 0;
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
	int numerator = 0;
	
	if (typedRelationPhraseCount.containsKey(relationToSearch))
	    if (typedRelationPhraseCount.get(relationToSearch).containsKey(phrase))
		numerator = typedRelationPhraseCount.get(relationToSearch).get(phrase);
	
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
	return  this.priors.get(relation) *
		this.probTypesGivenRel(typeSubject, typeObject, relation) *
		this.probPhraseGivenTypesAndRel(phrase, typeSubject, typeObject, relation);
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
    protected String predictRelation(WikiTriple t) {
	Map<String, Double> ranking = new HashMap<String, Double>();

	for (String relation : priors.keySet()){
	    double prob = 0.0;
	    if (this.getType().equals(ModelNBType.TYPED_RELATIONS)){
		prob = getProbabilityTypedRelation(t.getPhrase(), relation);
	    }else
		prob = getProbabilityClassic(t.getPhrase(), relation, t.getSubjectType(), t.getObjectType());
	    if (prob > 0.0)
		ranking.put(relation, prob);
	}

	if (ranking.size() > 0){
	    String rel = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getKey();
	    double prob = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getValue();
	    System.out.println(rel + "\t" + prob + "\t" + t);
	    return rel;
	}
	return null;
    }

}
