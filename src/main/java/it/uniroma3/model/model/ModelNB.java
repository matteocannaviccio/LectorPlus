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
public class ModelNB extends Model{
    private CounterMap<String> r_count;
    private CounterMap<String> pt_count;
    private CounterMap<String> ptr_count;
    // private CounterMap<String> tr_count;
    private Map<String, CounterMap<String>> pt2rCount;
    private int count_all_LT_UT;

    protected boolean includeNone = true;

    /**
     * 
     * @param db
     * @param typeNBModel
     * @param minRel
     * @param minFreq
     * @param minFreqWithR
     */
    public ModelNB(DB db, String labeled, int minFreq) {
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
	count_all_LT_UT = calculateSum(pt_LT_counts) + calculateSum(pt_UT_counts);
	System.out.printf("\t%-35s %s\n", "all triples: ", count_all_LT_UT);

	r_count = crud.getR_count(p_available.keySet(), includeNone);
	System.out.printf("\t%-35s %s\n", "relations (documents): ", r_count.size());

	ptr_count = crud.getPTR_count(p_available.keySet(), includeNone);
	System.out.printf("\t%-35s %s\n", "all labeled triples: ", calculateSum(ptr_count));

	pt_count = crud.getPT_count(p_available.keySet(), includeNone);
	pt2rCount = crud.getPTtoCountedR_LT(p_available.keySet(), includeNone);
    }

    /**
     * [DIPENDENT APPROACH]
     * Returns the prior probability of a relation.
     * 
     * @param relation
     * @return
     */
    public double calculatePrior(String relation) {
	//System.out.println("Prior: " + relation + "\t"+ (double)r_count.get(relation)/count_all_LT_UT);
	//int count_total = count_all_LT_UT - r_count.get("NONE");
	return (double)1/r_count.size();
    }

    /**
     * 
     * @param relation
     * @return
     */
    public int getRCount(String relation){
	return this.r_count.get(relation);
    }

    /**
     * 
     * @param relation
     * @return
     */
    public int getPTRCount(String relation, String phrase, String typeSubject, String typeObject){
	return this.ptr_count.get(phrase + "\t" + typeSubject + "\t" + relation + "\t"  + typeObject);
    }

    /**
     * 
     * @param relation
     * @return
     */
    public CounterMap<String> getPTtoRCount(String phrase, String typeSubject, String typeObject){
	CounterMap<String> rels = new CounterMap<String>();
	if (pt2rCount.containsKey(typeSubject + "\t" + phrase + "\t" + typeObject)){
	    rels =  pt2rCount.get(typeSubject + "\t" + phrase + "\t" + typeObject);
	}
	return rels;
    }


    /**
     * [DIPENDENT APPRAOCH]
     * Returns the probability of a phrase and types given the relation.
     * 
     * 			   	     |<p,ts,to,r>|
     *      	P(p, <ts,to> |r) = -----------------
     * 			   	      |<-,-,-,r>|
     * 
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    private double probPhraseTypesGivenRel(String phrase, String typeSubject, String typeObject, String relation){
	// NUMERATOR
	int numerator = 0;
	String toSearch = phrase + "\t" + typeSubject + "\t" + relation + "\t"  + typeObject;
	if (this.ptr_count.containsKey(toSearch))
	    numerator = this.ptr_count.get(toSearch);

	// DENOMINATOR
	int denominator = 0;
	if (this.r_count.containsKey(relation))
	    denominator = this.r_count.get(relation);

	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }

    /**
     * [DIPENDENT APPRAOCH]
     * Returns the probability of a phrase and types.
     * 
     * 			   	|<p,ts,to,->|
     *      	P(p,<ts,to>) = ---------------
     * 			         |<-,-,-,->|
     * 
     * @param typeSubject
     * @param typeObject
     * @param relation
     * @return
     */
    private double probPhraseTypes(String phrase, String typeSubject, String typeObject){
	// NUMERATOR
	int numerator = 0;
	String toSearch = typeSubject + "\t" + phrase + "\t" + typeObject;
	if (this.pt_count.containsKey(toSearch))
	    numerator = this.pt_count.get(toSearch);

	// DENOMINATOR
	int denominator = count_all_LT_UT;

	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
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
    /*
    private double probPhraseGivenRel(String phrase, String relation){

	// NUMERATOR
	int numerator = 0;
	if (pt2rCount.containsKey(phrase))
	    if (pt2rCount.get(phrase).containsKey(relation))
		numerator = pt2rCount.get(phrase).get(relation);

	// DENOMINATOR
	int denominator = r_count.get(relation);

	double prob = 0.0;
	if (numerator>0 && denominator>0)
	    prob = (double) numerator / denominator;
	return prob;
    }
     */
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
    /*
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
     */
    /**
     * [FREQUENTISTIC APPROACH]
     * Returns the probability of a relation given the phrase and the types.
     * 
     * 				       |<p,ts,to,r> in LT|
     *	        P(r | p, <ts, to>) =  ---------------------
     * 				       |<p,ts,to,-> in LT|
     * 
     * @param relation
     * @param phrase
     * @param typeSubject
     * @param typeObject
     * @return
     */
    private double probRelationGivenPhraseAndTypes(String relation, String phrase, String typeSubject, String typeObject){
	// NUMERATOR
	int numerator = 0;
	String toSearch = phrase + "\t" + typeSubject + "\t" + relation + "\t"  + typeObject;
	if (this.ptr_count.containsKey(toSearch))
	    numerator = this.ptr_count.get(toSearch);

	// DENOMINATOR
	int denominator = 0;
	toSearch = typeSubject + "\t" + phrase + "\t" + typeObject;
	if (this.pt_count.containsKey(toSearch))
	    denominator = this.pt_count.get(toSearch);

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
    /*
    private double getProbabilityIndependent(String phrase, String relation, String typeSubject, String typeObject){
	return calculatePrior(relation) * probPhraseGivenRel(phrase, relation) * probTypesGivenRel(typeSubject, typeObject, relation);	
    }
     */
    /**
     * 
     * @param phrase
     * @param relation
     * @param subjectType
     * @param objectType
     * @return
     */
    private double getProbabilityStupid(String phrase, String relation, String typeSubject, String typeObject){
	return probRelationGivenPhraseAndTypes(relation, phrase, typeSubject, typeObject);
    }

    /**
     * 
     * @param phrase
     * @param relation
     * @param subjectType
     * @param objectType
     * @return
     */
    private double getProbabilityClassic(String phrase, String relation, String typeSubject, String typeObject){
	double numerator =  calculatePrior(relation) * probPhraseTypesGivenRel(phrase, typeSubject, typeObject, relation);
	double denominator = probPhraseTypes(phrase, typeSubject, typeObject);
	return numerator/denominator;
    }


    @Override
    public Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder, String object_type) {
	String relation = null;
	double prob = 0.0;

	Map<String, Double> ranking = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityStupid(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		ranking.put(r, prob);
	}

	if (ranking.size() > 0){
	    if (ranking.containsKey("NONE")){
		if (ranking.get("NONE") < 0.75){
		    ranking.remove("NONE");
		    relation = Ranking.getTopKRanking(ranking, 1).entrySet().iterator().next().getKey();
		    prob = Ranking.getTopKRanking(ranking, 1).entrySet().iterator().next().getValue();
		}else{
		    relation = "NONE";
		    prob = ranking.get("NONE");
		}
	    }else{
		relation = Ranking.getTopKRanking(ranking, 1).entrySet().iterator().next().getKey();
		prob = Ranking.getTopKRanking(ranking, 1).entrySet().iterator().next().getValue();
	    }
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
	Map<String, Double> rankingClassic = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityClassic(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		rankingClassic.put(r, prob);
	}
	System.out.println(Ranking.getTopKRanking(rankingClassic, 10));

	prob = 0.0;
	Map<String, Double> rankingStupid = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityStupid(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		rankingStupid.put(r, prob);
	}
	System.out.println(Ranking.getTopKRanking(rankingStupid, 10));

	/*
	prob = 0.0;
	Map<String, Double> rankingIndependent = new HashMap<String, Double>();
	for (String r : r_count.keySet()){
	    prob = getProbabilityIndependent(phrase_placeholder, r, subject_type, object_type);
	    if (prob > 0.0)
		rankingIndependent.put(r, prob);
	}
	System.out.println(Ranking.getTopKRanking(rankingIndependent, 10));
	 */
	System.out.println();

	return Ranking.getTopKRanking(rankingStupid, 10);
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

	System.out.println("Prediction:");
	model.predictRelationList("[Person]", "lived in", "[Settlement]");
	model.predictRelationList("[Person]", "met", "[Person]");
	model.predictRelationList("[Person]", "married", "[Person]");
	model.predictRelationList("[Person]", "returned to", "[PopulatedPlace]");
	model.predictRelationList("[Person]", "graduated from", "[College]");

    }

}
