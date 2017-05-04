package it.uniroma3.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVWriter;

import it.uniroma3.triples.WikiTriple;
import it.uniroma3.util.Ranking;
/**
 * This is the implementation of a score-based model.
 * Assuming that a phrase can represent only one relation, a score-based model can be 
 * easily implemented using a map that link a phrase to the relative relation.
 * 
 * 
 * @author matteo
 *
 */
public class ModelScore extends Model{

    /*
     * This is the type of the score-based model. It can be:
     * TYPED_PHRASES: where we handle normal relations and typed phrases such as [Person] was born in [Settlement];
     * TYPED_RELATIONS: where we handle typed relations such as [Person] birthPlace [Settlement] and normal phrases;
     * NO_TYPES: where we handle normal relations and normal phrases.
     */
    public ModelScoreType type;
    public enum ModelScoreType {TYPED_PHRASES, TYPED_RELATIONS, NO_TYPES};

    /*
     * A ModelScore model is essentially a mapping between phrases and relations.
     * This mapping is obtained using scoring function applied to each phrase.
     */
    private Map<String, String> model = new HashMap<String, String>();
    private QueryDB db_read;
    private int minFreq;
    private int minFreqWithR;
    private int topk;

    /**
     * 
     * @param db
     */
    public ModelScore(DBlite db, int minFreq, int minFreqWithR, int topk, ModelScoreType type) {
	super();
	this.type = type;
	this.db_read = new QueryDB(db);
	this.minFreq = minFreq;
	this.minFreqWithR = minFreqWithR;
	this.topk = topk;
	this.model = createModel();
    }

    /**
     * 
     * @return
     */
    private ModelScoreType getType() {
	return this.type;
    }

    /**
     * Obtain the vocabulary of phrases. The model type is used to express 
     * the query to perform to retrieve the right kind of (*labeled) phrases.
     * 
     * @return
     */
    private Map<String, Integer> getLabeledPhrases(){
	Map<String, Integer> lab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    lab_phrases = db_read.getLabeledTypedPhrasesCount(minFreq);
	    break;
	case TYPED_RELATIONS:
	case NO_TYPES:
	    lab_phrases = db_read.getLabeledPhrasesCount(minFreq);
	    break;
	}
	return lab_phrases;
    }

    /**
     * Obtain the vocabulary of unlabeled phrases. The model type is used to express 
     * the query to perform to retrieve the right kind of (*unlabeled) phrases.
     * 
     * @param lab_phrases
]     * @return
     */
    private Map<String, Integer> getUnLabeledPhrases(Set<String> lab_phrases){
	Map<String, Integer> unlab_phrases = null;
	switch(type){
	case TYPED_PHRASES:
	    unlab_phrases = db_read.getUnlabeledTypedPhrasesCount(lab_phrases);
	    break;
	case TYPED_RELATIONS:
	case NO_TYPES:
	    unlab_phrases = db_read.getUnlabeledPhrasesCount(lab_phrases);
	    break;
	}
	return unlab_phrases;
    } 

    /**
     * Obtain the mapping counts between phrases and relations of unlabeled phrases. 
     * The model type is used to express the query to perform to retrieve the right 
     * kind of (*labeled) phrases and relations.
     * 
     * @param lab_phrases
     * @return
     */
    private Map<String, Map<String, Integer>> getRelationsAndPhrases(Set<String> lab_phrases){
	Map<String, Map<String, Integer>> relations2phrasesAndCount = null;
	switch(type){
	case TYPED_PHRASES:
	    relations2phrasesAndCount = db_read.getRelationTypedPhrasesCount(lab_phrases);
	    break;
	case TYPED_RELATIONS:
	    relations2phrasesAndCount = db_read.getTypedRelationPhrasesCount(lab_phrases);
	    break;
	case NO_TYPES:
	    relations2phrasesAndCount = db_read.getRelationPhrasesCount(lab_phrases);
	    break;
	}
	return relations2phrasesAndCount;
    } 

    /**
     * 
     * @param lab_phrases
     * @param unlab_phrases
     * @param relation2phrasesCount
     * @return
     */
    private Map<String, String> createScoredBasedModel(
	    Map<String, Integer> lab_phrases, 
	    Map<String, Integer> unlab_phrases, 
	    Map<String, Map<String, Integer>> relation2phrasesCount){

	/*
	 * get the counts of seed phrases, both from labeled and unlabeled triples
	 */
	System.out.print("query for seed phrases ...");
	Map<String, Integer> labeled_seedphrases = db_read.getLabeledPhrasesCount(0);
	Map<String, Integer> unlabeled_seedphrases = db_read.getUnlabeledPhrasesCount(labeled_seedphrases.keySet());
	Map<String, Map<String, Integer>> relphraseSeedCounts = db_read.getRelationPhrasesCount(labeled_seedphrases.keySet());
	System.out.println(" done!");

	// keep track of the probability values relative to the phrases that we are using in the model
	// this is used to replace the constraint that a phrase can be linked to a relation only if it 
	// has more than 0.5 in the probability
	Map<String, Double> phrase2prob = new HashMap<String, Double>();
	Map<String, Map<String, Double[]>> relations2phrase_details = new HashMap<String, Map<String, Double[]>>();

	// for each relation ...
	for(Map.Entry<String, Map<String, Integer>> entry : relation2phrasesCount.entrySet()){
	    String relation = entry.getKey();
	    for(Map.Entry<String, Integer> phraseCnt : relation2phrasesCount.get(relation).entrySet()){
		String phrase = phraseCnt.getKey();
		double pR = phraseCnt.getValue();
		double pL = lab_phrases.get(phrase);
		double pU = 0;
		if (unlab_phrases.containsKey(phrase))
		    pU = unlab_phrases.get(phrase);

		double pSeedR = 0;
		if(relphraseSeedCounts.containsKey(relation)){
		    if(relphraseSeedCounts.get(relation).containsKey(phrase.split("\t")[1])){
			pSeedR = relphraseSeedCounts.get(relation).get(phrase.split("\t")[1]);
		    }
		}
		double pSeedL = labeled_seedphrases.get(phrase.split("\t")[1]);
		double pSeedU = 0;
		if (unlab_phrases.containsKey(phrase))
		    pSeedU = unlabeled_seedphrases.get(phrase.split("\t")[1]);

		double probLab = pR/pL;
		double probLabUnlab = pR/(pL+pU);
		double probSeedLab = pSeedR/pSeedL;
		double probSeedLabUnlab = pSeedR/(pSeedL+pSeedU);
		double scoreLectorV1 = probLab * Math.log(pR+1);

		// add details ...
		Double[] details = new Double[]{pR, pL, pU, pSeedR, pSeedL, pSeedU, probLab, probLabUnlab, 
			probSeedLab, probSeedLabUnlab, scoreLectorV1};
		if(!relations2phrase_details.containsKey(relation))
		    relations2phrase_details.put(relation, new HashMap<String, Double[]>());
		relations2phrase_details.get(relation).put(phrase, details);

		// add to the model only one relation for each phrase
		if (!phrase2prob.containsKey(phrase) || phrase2prob.get(phrase) < probLab){
		    model.put(phrase, relation);
		    phrase2prob.put(phrase, probLab);
		}
	    }
	}

	// filter out phrases that are not used for the relative relation, i.e. prob. not max
	for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
	    Map<String, Double[]> phrases = new HashMap<String, Double[]>();
	    for (Map.Entry<String, Double[]> p : phrases.entrySet())
		if(model.containsKey(p.getKey()) && model.get(p.getKey()).equals(relation.getKey()))
		    phrases.put(p.getKey(), p.getValue());
	    relations2phrase_details.put(relation.getKey(), phrases);
	}

	printDetails(relations2phrase_details);
	return model;
    }

    /**
     * 
     * @param relations2phrase_details
     */
    private void printDetails(Map<String, Map<String, Double[]>> relations2phrase_details){
	try {
	    CSVWriter writer = new CSVWriter(new FileWriter("/Users/matteo/Desktop/score_model.csv"), ',');
	    writer.writeNext(new String[]{"relation", "phrase", "c(pR)", "c(pL)", "c(pU)", "c(pSeedR)","c(pSeedL)", "c(pSeedU)", "P(p|r)", "P(p|r,u)", "P(pSeed|r)", "P(pSeed|r,u)", "score lector v1", "new prob", "new score"});
	    for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
		for (Map.Entry<String, Double[]> phrase : Ranking.getDoubleKRanking(relation.getValue(), 6, topk).entrySet()){
		    String[] values = new String[15];
		    values[0] = relation.getKey();
		    values[1] = phrase.getKey();
		    values[2] = String.valueOf(phrase.getValue()[0]); //c(PR)
		    values[3] = String.valueOf(phrase.getValue()[1]); //c(uP)
		    values[4] = String.valueOf(phrase.getValue()[2]); //c(lP)
		    values[5] = String.valueOf(phrase.getValue()[3]); //c(pSeedR)
		    values[6] = String.valueOf(phrase.getValue()[4]); //c(pSeedL)
		    values[7] = String.valueOf(phrase.getValue()[5]); //c(pSeedU)
		    values[8] = String.valueOf(phrase.getValue()[6]); //P(p|r)
		    values[9] = String.valueOf(phrase.getValue()[7]); //P(p|r,u)
		    values[10] = String.valueOf(phrase.getValue()[8]); //P(pSeed|r)
		    values[11] = String.valueOf(phrase.getValue()[9]); //P(pSeed|r,u)
		    values[12] = String.valueOf(phrase.getValue()[10]); //score lector v1
		    values[13] = String.valueOf(0.5 * phrase.getValue()[7] + 0.5 * phrase.getValue()[9]); //possible new prob
		    values[14] = String.valueOf(Math.log(phrase.getValue()[0] + 1) * Double.valueOf(values[13])); //possible new score
		    writer.writeNext(values);
		}
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /**
     * A score-based model is essentially a map that link a phrase to the relative relation.
     * Here we assume that a phrase can represent only one relation.
     * 
     * @return
     */
    private Map<String, String> createModel(){
	System.out.println("Counting for " + type.name() + " score-based model ...");
	Map<String, Integer> lab_phrases = getLabeledPhrases(); 
	Map<String, Integer> unlab_phrases = getUnLabeledPhrases(lab_phrases.keySet());
	Map<String, Map<String, Integer>> relation2phrasesCount = getRelationsAndPhrases(lab_phrases.keySet());
	Map<String, Map<String, Integer>> relation2phrasesCount_shrinked = shrinkRelationPhrasesCount(relation2phrasesCount);

	System.out.println("--------");
	System.out.println("Total labeled phrases(>" + minFreq + " occ.): " + lab_phrases.size());
	System.out.println("Total unlabeled phrases: " + unlab_phrases.size());
	System.out.println("Total relations: " + relation2phrasesCount.keySet().size());
	System.out.println("Total relations (shrinked): " + relation2phrasesCount_shrinked.keySet().size());
	System.out.println("-----------------------");
	System.out.println("Creating model ...");

	Map<String, String> model = createScoredBasedModel(lab_phrases, unlab_phrases, relation2phrasesCount_shrinked);
	return model;
    }  


    /**
     * It removes the phrases that do not appear an enough number of times with the relation.
     * As a consequence, it removes the relations that we consider.
     * 
     * @param relation2phrasesCount
     * @return
     */
    private Map<String, Map<String, Integer>> shrinkRelationPhrasesCount(Map<String, Map<String, Integer>> relation2phrasesCount) {
	Map<String, Map<String, Integer>> shrinked = new HashMap<String, Map<String, Integer>>();
	for(Map.Entry<String, Map<String, Integer>> entry : relation2phrasesCount.entrySet()){
	    for(Map.Entry<String, Integer> entryPhrase : entry.getValue().entrySet()){
		int count = entryPhrase.getValue();
		if (count >= this.minFreqWithR){
		    if (!shrinked.containsKey(entry.getKey()))
			shrinked.put(entry.getKey(), new HashMap<String, Integer>());
		    shrinked.get(entry.getKey()).put(entryPhrase.getKey(), entryPhrase.getValue());
		}
	    }
	}
	return shrinked;
    }

    /**
     * 
     */
    protected String predictRelation(WikiTriple t) {
	String phrase = t.getPhrase();
	if (this.getType().equals(ModelScoreType.TYPED_PHRASES))
	    phrase = t.getSubjectType() + "\t" + phrase + "\t" + t.getObjectType();
	if (model.containsKey(phrase)){
	    return model.get(phrase);
	}else{
	    return null;
	}
    }


}
