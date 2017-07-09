package it.uniroma3.model.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.DBModel;
/**
 * This is the implementation of a Naive Bayes Classifier.
 * @author matteo
 *
 */
public class ModelNBind extends Model{
    private CounterMap<String> r_count;
    private CounterMap<String> tr_count;
    private Map<String, CounterMap<String>> p2rCount;
    private int count_all_triples;

    protected boolean includeNone = false;

    /**
     * 
     * @param db
     * @param typeNBModel
     * @param minRel
     * @param minFreq
     * @param minFreqWithR
     */
    public ModelNBind(DB db, String labeled, int minFreq) {
	super(db, labeled, PhraseType.NO_TYPES_PHRASES, minFreq);
	createModel();
    }

    /**
     * A bayesian classifier model is essentially a generative model. Given a phrase, and a pair of types, 
     * it is able to estimate the probability of each relation to produce them.
     * 
     * @return
     */
    private void createModel(){
	count_all_triples = calculateSum(pt_LT_counts);
	if(includeNone) 
	    count_all_triples += calculateSum(pt_UT_counts);
	if(verbose)
	    System.out.printf("\t%-35s %s\n", "all triples: ", count_all_triples);

	r_count = crud.getR_count(p_available.keySet(), includeNone);
	if(verbose)
	    System.out.printf("\t%-35s %s\n", "relations (documents): ", r_count.size());

	tr_count = crud.getTR_count(p_available.keySet(), includeNone);
	if(verbose)
	    System.out.printf("\t%-35s %s\n", "all labeled pair of types: ", tr_count.size());

	p2rCount = crud.getPtoCountedR_LT(p_available.keySet(), includeNone);
    }

    /**
     * Returns the prior probability of a relation.
     * 
     * @param relation
     * @return
     */
    public double calculatePrior(String relation) {
	return (double)r_count.get(relation)/count_all_triples;
    }

    /**
     * [INDIPENDENT APPRAOCH]
     * Returns the probability of a phrase given the relation.
     * It is calculated as the number of times the phrase and the relation have been found 
     * in the labeled triples, divided by the number of times we find the relation only:
     * 
     * 			   |<p,-,-,r> in LT|
     *      	P(p|r) = ---------------------
     * 			    |<-,-,-,r> in LT|
     * 
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    private double probPhraseGivenRel(String phrase, String relation){
	// NUMERATOR
	int numerator = 0;
	if (p2rCount.containsKey(phrase))
	    if (p2rCount.get(phrase).containsKey(relation))
		numerator = p2rCount.get(phrase).get(relation);

	// DENOMINATOR
	int denominator = r_count.get(relation);

	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }


    /**
     * [INDIPENDENT APPRAOCH]
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
    private double probTypesGivenRel(String typeSubject, String typeObject, String relation){
	String toSearch = typeSubject + "\t" + relation + "\t" + typeObject;

	// NUMERATOR
	int numerator = 0;
	if (tr_count.containsKey(toSearch))
	    numerator = this.tr_count.get(toSearch);

	// DENOMINATOR
	int denominator = r_count.get(relation);

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
    private double getProbabilityIndependent(String phrase, String relation, String typeSubject, String typeObject){
	return calculatePrior(relation) * probPhraseGivenRel(phrase, relation) * probTypesGivenRel(typeSubject, typeObject, relation);	
    }



    @Override
    public Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder, String object_type) {
	String relation = null;
	double prob = 0.0;

	Map<String, Double> ranking = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityIndependent(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		ranking.put(r, prob);
	}

	if (ranking.size() > 0){
	    relation = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getKey();
	    prob = Ranking.getNormalizedTopKRanking(ranking, 1).entrySet().iterator().next().getValue();
	}

	return Pair.make(relation, prob);
    }

    /**
     * 
     * @param subject_type
     * @param phrase_placeholder
     * @param object_type
     * @return
     */
    public Map<String, Double> predictRelationList(String subject_type, String phrase_placeholder, String object_type) {
	double prob = 0.0;
	Map<String, Double> rankingIndependent = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityIndependent(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		rankingIndependent.put(r, prob);
	}
	System.out.println(Ranking.getTopKRanking(rankingIndependent, 10));
	System.out.println();

	return Ranking.getTopKRanking(rankingIndependent, 10);
    }


    @Override
    public boolean canPredict(String expectedRelation) {
	return true;
    }

    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));

	DBModel dbmodel = new DBModel(Configuration.getDBModel());
	ModelNB model = new ModelNB(dbmodel, "labeled_triples", 5);

    }

}
