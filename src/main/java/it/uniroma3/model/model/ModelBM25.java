package it.uniroma3.model.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVWriter;

import it.uniroma3.config.Configuration;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.DB;

public class ModelBM25 extends Model{
    /*
     * A ModelScore model is essentially a mapping between phrases and relations.
     * This mapping is obtained using scoring function applied to each phrase.
     */
    private Map<String, String> model = new HashMap<String, String>();

    // parameters
    private double k = 1;
    private double b = 1;


    /**
     * 
     * @param db
     */
    public ModelBM25(DB db, String labeled, int minFreq) {
	super(db, labeled, PhraseType.TYPED_PHRASES, minFreq);
	this.model = createModel();
    }

    /**
     * A score-based model is essentially a map that link a phrase to the relative relation.
     * Here we assume that a phrase can represent only one relation.
     * 
     * @return
     */
    private Map<String, String> createModel(){
	CounterMap<String> r_count = crud.getR_count(p_available.keySet(), false);
	Map<String, CounterMap<String>> r2ptCount = crud.getRtoCountedPT_LT(this.p_available.keySet(), false);
	Map<String, CounterMap<String>> pt2rCount = crud.getPtoCountedR_LT(this.p_available.keySet(), false);
	double avgRelationsLength = calculateRelationsAverage(r2ptCount);
	

	Map<String, String> model = createBM25Model(
		avgRelationsLength, 
		this.pt_LT_counts, 
		this.pt_UT_counts, 
		r_count,
		pt2rCount, 
		r2ptCount);

	return model;
    }

    /**
     * 
     * @param avgRelationsLength
     * @param p_available
     * @param unlab_phrases
     * @param phrases2relationsCount
     * @param relation2phrasesCount
     * @return
     */
    private Map<String, String> createBM25Model(double avgRelationsLength, CounterMap<String> labeled_phrases,
	    CounterMap<String> unlab_phrases, CounterMap<String> relations_counts, 
	    Map<String, CounterMap<String>> phrases2relationsCount, Map<String, CounterMap<String>> relation2phrasesCount) {

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
		double pL = labeled_phrases.get(phrase);
		double pU = 0;
		if (unlab_phrases.containsKey(phrase))
		    pU = unlab_phrases.get(phrase);

		double lengthRatio = (double)relations_counts.get(relation)/avgRelationsLength;
		double nDifRel = phrases2relationsCount.get(phrase).size();
		double idf = Math.log(relations_counts.size() / nDifRel);
		double factor = ((k+1) * pR) / k * ((1-b) + b * (lengthRatio));
		double scoreBM25 = idf * factor;

		// add details ...
		Double[] details = new Double[]{pR, pL, pU, nDifRel, idf, factor, scoreBM25};
		if(!relations2phrase_details.containsKey(relation))
		    relations2phrase_details.put(relation, new HashMap<String, Double[]>());
		relations2phrase_details.get(relation).put(phrase, details);

		// add to the model only one relation for each phrase
		if (!phrase2prob.containsKey(phrase) || phrase2prob.get(phrase) < scoreBM25){
		    model.put(phrase, relation);
		    phrase2prob.put(phrase, scoreBM25);
		}
	    }
	}
	// filter out phrases that are not used for the relative relation, i.e. prob. not max
	for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
	    Map<String, Double[]> phrases = new HashMap<String, Double[]>();
	    for (Map.Entry<String, Double[]> p : relation.getValue().entrySet()){
		if(model.containsKey(p.getKey()) && model.get(p.getKey()).equals(relation.getKey()))
		    phrases.put(p.getKey(), p.getValue());
		relations2phrase_details.put(relation.getKey(), phrases);
	    }
	}

	printDetails(relations2phrase_details);
	model.clear();

	for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
	    for (Map.Entry<String, Double[]> phrase : Ranking.getDoubleKRanking(relation.getValue(), 6, -1).entrySet()){
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
	    CSVWriter writer = new CSVWriter(new FileWriter(Configuration.getLectorFolder() + "/score_model_BM25.csv"), ',');
	    writer.writeNext(new String[]{"relation", "phrase", "c(pR)", "c(pL)", "c(pU)", "nDifRel", "idf","factor", "scoreBM25"});
	    for (Map.Entry<String, Map<String, Double[]>> relation : relations2phrase_details.entrySet()){
		for (Map.Entry<String, Double[]> phrase : Ranking.getDoubleKRanking(relation.getValue(), 6, -1).entrySet()){
		    String[] values = new String[9];
		    values[0] = relation.getKey();
		    values[1] = phrase.getKey();
		    values[2] = String.valueOf(phrase.getValue()[0]); //c(PR)
		    values[3] = String.valueOf(phrase.getValue()[1]); //c(uP)
		    values[4] = String.valueOf(phrase.getValue()[2]); //c(lP)
		    values[5] = String.valueOf(phrase.getValue()[3]); //nDifRel
		    values[6] = String.valueOf(phrase.getValue()[4]); //idf
		    values[7] = String.valueOf(phrase.getValue()[5]); //factor
		    values[8] = String.valueOf(phrase.getValue()[6]); //scoreBM25
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
     * @param marks
     * @return
     */
    private double calculateRelationsAverage(Map<String, CounterMap<String>> relations2phrasesCount) {
	double sum = 0.0;
	for(Map.Entry<String, CounterMap<String>> entry : relations2phrasesCount.entrySet()){
	    sum += entry.getValue().size();
	}
	return sum / relations2phrasesCount.size();
    }


    @Override
    public Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder, String object_type) {
	String relation = null;
	String phrase = subject_type + "\t" + phrase_placeholder + "\t" + object_type;
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

    @Override
    public boolean canPredict(String expectedRelation) {
	return getPredictableRelations().contains(expectedRelation);
    }
}
