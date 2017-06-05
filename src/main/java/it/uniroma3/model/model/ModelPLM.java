package it.uniroma3.model.model;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.opencsv.CSVWriter;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.DBModel;

public class ModelPLM extends Model{

    private Map<String, Map<String, Double>> model;

    /**
     * 
     * @param db
     * @param labeled
     * @param minFreq
     * @param modelType
     */
    public ModelPLM(DB db, String labeled, int minFreq, PhraseType modelType){
	super(db, labeled, modelType, minFreq);
	model = createModel();
	printDetails(model);
    }

    /**
     * 
     * @return
     */
    private Map<String, Map<String, Double>> createModel() {
	Map<String, Map<String, Double>> relations_probabilities = new HashMap<String, Map<String, Double>>();

	System.out.println("-> get typed phrases count for the relations");
	Map<String, CounterMap<String>> relation2phrasesCount = availableRelations2phrases(this.available_phrases.keySet());
	System.out.println("-> "+ relation2phrasesCount.size() +" relations.");
	CounterMap<String> labeledFactsByRelation = availableRelations(this.available_phrases.keySet());

	System.out.println("-> creating distributions");
	for (Map.Entry<String, CounterMap<String>> relation : relation2phrasesCount.entrySet()){
	    if (!relations_probabilities.containsKey(relation.getKey()))
		relations_probabilities.put(relation.getKey(), new HashMap<String, Double>());
	    for (Map.Entry<String, Integer> phrase : relation.getValue().entrySet()){
		double prob = (double)phrase.getValue()/labeledFactsByRelation.get(relation.getKey());
		relations_probabilities.get(relation.getKey()).put(phrase.getKey(), prob);
	    }
	}
	return relations_probabilities;
    }

    /**
     * 
     * @param relations2phrase_details
     */
    private void printDetails(Map<String, Map<String, Double>> relations2phrase_details){
	try {
	    CSVWriter writer = new CSVWriter(new FileWriter(Configuration.getLectorFolder() + "/score_model_PLM.csv"), ',');

	    // header
	    writer.writeNext(new String[]{
		    "relation", 
		    "phrase", 
	    "prob"});

	    // content
	    for (Map.Entry<String, Map<String, Double>> relation : relations2phrase_details.entrySet()){
		for (Map.Entry<String, Double> phrase : Ranking.getTopKRanking(relation.getValue(), 20).entrySet()){
		    String[] values = new String[3];
		    values[0] = relation.getKey();
		    values[1] = phrase.getKey();
		    values[2] = String.valueOf(phrase.getValue()); 	//prob
		    if(values[0].equals("child") || values[0].equals("parent(-1)"))
			writer.writeNext(values);
		}
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    @Override
    public Pair<String, Double> predictRelation(WikiTriple t) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean canPredict(String expectedRelation) {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * 
     * @param relationA
     * @param relationB
     * @return
     */
    private Double measureKLDivergenvece(String relationA, String relationB){
	double kl_div = 0.0;
	int intersect = 0;
	Map<String, Double> phrasesProbRelationA = this.model.get(relationA);
	Map<String, Double> phrasesProbRelationB = this.model.get(relationB);

	for(String p : this.available_phrases.keySet()){
	    double p1 = 0.000001;
	    double q1 = 0.000001;

	    if (phrasesProbRelationA.containsKey(p)){
		p1 = phrasesProbRelationA.get(p);
		if (phrasesProbRelationB.containsKey(p)){
		    intersect += 1;
		    q1 = phrasesProbRelationB.get(p);
		}
	    }else{
		if (phrasesProbRelationB.containsKey(p)){
		    q1 = phrasesProbRelationB.get(p);
		}
	    }

	    kl_div += p1 * Math.log(p1/q1);
	}

	if (intersect>10)
	    return kl_div;
	else
	    return null;
    }

    /**
     * 
     * @param relation
     * @return
     */
    private Map<String, Double> findSimilarRelations(String relation){
	Map<String, Double> similarRelations = new HashMap<String, Double>();
	for (String otherRel : model.keySet()){
	    if (!relation.equals(otherRel)){
		Double kl = measureKLDivergenvece(relation, otherRel);
		if (kl != null)
		    similarRelations.put(otherRel, kl);
	    }

	}
	return Ranking.getIncreasingRanking(similarRelations);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	ModelPLM model = new ModelPLM(new DBModel("model.db"), "labeled_triples", 100, PhraseType.NO_TYPES_PHRASES);
	for (Map.Entry<String, Integer> relation : Ranking.getRanking(model.availableRelations(model.available_phrases.keySet())).entrySet()){
	    //System.out.println("RELATION: " + relation.getKey() + "(" + relation.getValue() + ")" +"\t SIMILAR: " +  Ranking.getInverseDoubleKRanking(model.findSimilarRelations(relation.getKey()), 3));
	    Map<String, Double> rank = Ranking.getInverseDoubleKRanking(model.findSimilarRelations(relation.getKey()), -1);
	    if (rank.size() > 0){
		String similarRel = rank.entrySet().iterator().next().getKey();
		double prob = rank.entrySet().iterator().next().getValue();
		if (relation.getKey().contains("(-1)")){
		    if (!similarRel.contains("(-1)") && prob < 1.0){
			System.out.println(relation.getKey() + " === " + similarRel);
		    }
		}
	    }
	}
    }
}
