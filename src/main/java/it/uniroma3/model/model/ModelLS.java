package it.uniroma3.model.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVWriter;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.DB;
/**
 * This is the implementation of a score-based model.
 * Assuming that a phrase can represent only one relation, a score-based model can be 
 * easily implemented using a map that link a phrase to the relative relation.
 * 
 * 
 * @author matteo
 *
 */
public class ModelLS extends Model{

    /*
     * A ModelScore model is essentially a mapping between phrases and relations.
     * This mapping is obtained using scoring function applied to each phrase.
     */
    private Map<String, String> model = new HashMap<String, String>();

    // parameters
    private double alpha;
    private double beta;
    private int topk;

    /**
     * 
     * @param db
     */
    public ModelLS(DB db, String labeled, int minFreq, int topk, double alpha, double beta, PhraseType modelType) {
	super(db, labeled, modelType, minFreq);
	this.alpha = alpha;
	this.beta = beta;
	this.topk = topk;
	this.model = createModel();
    }


    /**
     * A score-based model is essentially a map that link a phrase to the relative relation.
     * Here we assume that a phrase can represent only one relation.
     * 
     * @return
     */
    private Map<String, String> createModel(){
	if(verbose)
	    System.out.println("-> Creating score-based model");

	CounterMap<String> unlab_phrases = availableUnlabeledPhrases(this.available_phrases.keySet());
	Map<String, CounterMap<String>> relation2phrasesCount = availableRelations2phrases(this.available_phrases.keySet());

	/*
	 * get the counts of seed phrases, both from labeled and unlabeled triples
	 */
	if(verbose)
	    System.out.println("-> Obtaining seed statistics");

	CounterMap<String> seed_labeledPhrases = db_read.getAvailablePhrases(0);
	CounterMap<String> seed_unlabeledPhrases = db_read.getUnlabeledPhrasesCount(seed_labeledPhrases.keySet());
	Map<String, CounterMap<String>> seed_relphraseCounts = db_read.getRelationPhrasesCount(seed_labeledPhrases.keySet());



	if(verbose){
	    System.out.println("\t--------");
	    System.out.println("\t -> Total labeled distinct phrases(> " + minFreq + " occ.): " + this.available_phrases.keySet().size());
	    System.out.println("\t -> Total unlabeled distinct phrases: " + unlab_phrases.size());
	    System.out.println("\t -> Total relations: " + relation2phrasesCount.keySet().size());
	    //System.out.println("Total relations (shrinked): " + relation2phrasesCount_shrinked.keySet().size());
	    System.out.println("\t-----------------------");

	    System.out.println("-> Creating model");
	}

	Map<String, String> model = createScoredBasedModel(
		this.available_phrases, 
		unlab_phrases, 
		relation2phrasesCount,
		seed_labeledPhrases,
		seed_unlabeledPhrases,
		seed_relphraseCounts);

	return model;
    }  

    /**
     * 
     * @param lab_phrases
     * @param unlab_phrases
     * @param relation2phrasesCount
     * @return
     */
    private Map<String, String> createScoredBasedModel(
	    CounterMap<String> lab_phrases, 
	    CounterMap<String> unlab_phrases, 
	    Map<String, CounterMap<String>> relation2phrasesCount,
	    CounterMap<String> seed_lab_phrases,
	    CounterMap<String> seed_unlab_phrases,
	    Map<String, CounterMap<String>> seed_relphrasesCount){

	// keep track of the probability values relative to the phrases that we are using in the model
	// this is used to replace the constraint that a phrase can be linked to a relation only if it 
	// has more than 0.5 in the probability
	Map<String, Double> phrase2prob = new HashMap<String, Double>();
	Map<String, Map<String, Double[]>> relations2phrase_details = new HashMap<String, Map<String, Double[]>>();

	// for each relation ...
	for(Map.Entry<String, CounterMap<String>> entry : relation2phrasesCount.entrySet()){
	    String relation = entry.getKey();
	    for(Map.Entry<String, Integer> phraseCnt : relation2phrasesCount.get(relation).entrySet()){
		String phrase = phraseCnt.getKey();
		double pR = phraseCnt.getValue();
		double pL = lab_phrases.get(phrase);
		double pU = 0;
		if (unlab_phrases.containsKey(phrase))
		    pU = unlab_phrases.get(phrase);

		String phraseSeed = phrase.split("\t")[1];
		double pSeedR = 0;
		if(seed_relphrasesCount.containsKey(relation)){
		    if(seed_relphrasesCount.get(relation).containsKey(phraseSeed)){
			pSeedR = seed_relphrasesCount.get(relation).get(phraseSeed);
		    }
		}
		double pSeedL = seed_lab_phrases.get(phraseSeed);
		double pSeedU = 0;
		if (seed_unlab_phrases.containsKey(phraseSeed))
		    pSeedU = seed_unlab_phrases.get(phraseSeed);

		double probLab = pR/pL;
		double probLabUnlab = pR/(pL+pU);
		double probSeedLab = pSeedR/pSeedL;
		double probSeedLabUnlab = pSeedR/(pSeedL+pSeedU);
		double scoreLectorV1 = probLab * Math.log(pR+1);
		double combinedProb = alpha * probLabUnlab + beta * probSeedLabUnlab;
		double scoreLectorV2 = combinedProb * Math.log(pR + 1);

		// add details ...
		Double[] details = new Double[]{
			pR, 
			pL, 
			pU, 
			pSeedR, 
			pSeedL, 
			pSeedU, 
			probLab, 
			probLabUnlab, 
			probSeedLab, 
			probSeedLabUnlab, 
			scoreLectorV1, 
			combinedProb, 
			scoreLectorV2};

		if(!relations2phrase_details.containsKey(relation))
		    relations2phrase_details.put(relation, new HashMap<String, Double[]>());
		relations2phrase_details.get(relation).put(phrase, details);

		/*
		 * add to the model only one relation for each phrase
		 * i.e. save the highest probability value associated 
		 * to each phrase with a relation
		 * 
		 */
		if (!phrase2prob.containsKey(phrase) || phrase2prob.get(phrase) < probLab){
		    if (probSeedLabUnlab > 0.15){
			phrase2prob.put(phrase, probLab);
			model.put(phrase, relation);
		    }
		}
	    }
	}

	// filter out phrases that are not used for the relative relation, i.e. prob. not max
	for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
	    Map<String, Double[]> phrases = new HashMap<String, Double[]>();
	    for (Map.Entry<String, Double[]> p : relation.getValue().entrySet())
		if(model.containsKey(p.getKey()) && model.get(p.getKey()).equals(relation.getKey()))
		    phrases.put(p.getKey(), p.getValue());
	    relations2phrase_details.put(relation.getKey(), phrases);
	}

	printDetails(relations2phrase_details);
	model.clear();

	for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
	    for (Map.Entry<String, Double[]> phrase : Ranking.getDoubleKRanking(relation.getValue(), 12, topk).entrySet()){
		model.put(phrase.getKey(), relation.getKey());
	    }
	}

	return model;
    }

    /**
     * 
     * @param relations2phrase_details
     */
    private void printDetails(Map<String, Map<String, Double[]>> relations2phrase_details){
	try {
	    CSVWriter writer = new CSVWriter(new FileWriter("./score_model.csv"), ',');

	    // header
	    writer.writeNext(new String[]{
		    "relation", 
		    "phrase", 
		    "c(pR)", 
		    "c(pL)", 
		    "c(pU)", 
		    "c(pSeedR)",
		    "c(pSeedL)",
		    "c(pSeedU)",
		    "P(p|r)", 
		    "P(p|r,u)", 
		    "P(pSeed|r)", 
		    "P(pSeed|r,u)", 
		    "score lector v1", 
		    "combinedProb", 
	    "score lector v2"});

	    // content
	    for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
		for (Map.Entry<String, Double[]> phrase : Ranking.getDoubleKRanking(relation.getValue(), 12, -1).entrySet()){
		    String[] values = new String[15];
		    values[0] = relation.getKey();
		    values[1] = phrase.getKey();
		    values[2] = String.valueOf(phrase.getValue()[0]); 	//c(PR)
		    values[3] = String.valueOf(phrase.getValue()[1]); 	//c(uP)
		    values[4] = String.valueOf(phrase.getValue()[2]); 	//c(lP)
		    values[5] = String.valueOf(phrase.getValue()[3]); 	//c(pSeedR)
		    values[6] = String.valueOf(phrase.getValue()[4]); 	//c(pSeedL)
		    values[7] = String.valueOf(phrase.getValue()[5]); 	//c(pSeedU)
		    values[8] = String.valueOf(phrase.getValue()[6]); 	//P(p|r)
		    values[9] = String.valueOf(phrase.getValue()[7]); 	//P(p|r,u)
		    values[10] = String.valueOf(phrase.getValue()[8]); 	//P(pSeed|r)
		    values[11] = String.valueOf(phrase.getValue()[9]); 	//P(pSeed|r,u)
		    values[12] = String.valueOf(phrase.getValue()[10]); //score lector v1
		    values[13] = String.valueOf(phrase.getValue()[11]); //possible new prob
		    values[14] = String.valueOf(phrase.getValue()[12]); //possible new score
		    writer.writeNext(values);
		}
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     */
    public Pair<String, Double> predictRelation(WikiTriple t) {
	String relation = null;
	String phrase = t.getPhrasePlaceholders();
	if (this.type.equals(PhraseType.TYPED_PHRASES))
	    phrase = t.getSubjectType() + "\t" + phrase + "\t" + t.getObjectType();
	if (model.containsKey(phrase)){
	    relation = model.get(phrase);
	}
	return Pair.make(relation, 1.0);
    }

    /**
     * 
     * @return
     */
    public Set<String> getPredictableRelations(){
	return new HashSet<String>(this.model.values());
    }


}
