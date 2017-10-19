package it.uniroma3.model.model;

import java.util.HashMap;
import java.util.Map;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.model.db.DBLector;

/**
 * A Naive Bayes model can predict one of the KG relations or it can abstain from the prediction.
 * 
 * @author matteo
 *
 */
public class ModelNaiveBayes extends Model {

  // a prediction is valid only if the most probable relation has a probability greather than this
  protected double majorityThreshold = 0.0;

  /**
   * 
   * @param db_model
   * @param evidence_table
   * @param minF
   * @param percentage
   * @param modelType
   * @param ruleOfMajority
   */
  public ModelNaiveBayes(DBLector db_model, String evidence_table, int minF, int percentage,
      ModelType modelType, double majorityThreshold) {
    super(db_model, evidence_table, minF, percentage, modelType);
    this.majorityThreshold = majorityThreshold;
  }

  /**
   * Pick the best relation from the Naive Bayes ranking.
   */
  protected Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder,
      String object_type) {
    // pick first from the ranking ...
    Map<String, Double> ranking =
        predictRelationsRanking(subject_type, phrase_placeholder, object_type);
    Pair<String, Double> finalPrediction = Pair.make(ranking.entrySet().iterator().next());
    String prediction = finalPrediction.key;
    double probability = finalPrediction.value;

    // if we are using ruleOfMajority, checks the constraint
    if (!prediction.equals("NONE") && probability < majorityThreshold) {
      // String typedPhrase = subject_type + " " + phrase_placeholder + " " + object_type;
      // System.out.printf("%s\t%s\t%s\t%s\t%s\n", typedPhrase, finalPrediction.key, probability,
      // NegativePredictions.MAJRULE.name(), ranking.toString());
      finalPrediction = Pair.make(NegativePredictions.MAJRULE.name(), probability);
    }

    return finalPrediction;
  }

  /**
   * Calculates Naive Bayes for each relation and returns a ranking of possible relations.
   * 
   * @param subject_type
   * @param phrase_placeholder
   * @param object_type
   * @return
   */
  private Map<String, Double> predictRelationsRanking(String subject_type,
      String phrase_placeholder, String object_type) {
    // input ...
    String typedPhrase = subject_type + "\t" + phrase_placeholder + "\t" + object_type;
    Map<String, Double> relations2prob = new HashMap<String, Double>();

    for (String relation : this.relations.keySet()) {
      double relationProbability = getRelationProbability(typedPhrase, relation);
      if (relationProbability > 0.0)
        relations2prob.put(relation, relationProbability);
    }

    // if we have never labeled any typed phrase...
    if (relations2prob.isEmpty())
      relations2prob.put("UNKNOWN", 1.0);

    return Ranking.getRanking(relations2prob);
  }

  /**
   * Returns the probability that the input typed-phrase describes the given relation using
   * NaiveBayes posterior calculation.
   * 
   * @param typedPhrase
   * @param relation
   * @return
   */
  public double getRelationProbability(String typedPhrase, String relation) {
    double relationProbability;
    if (this.typedPhrases2relations.containsKey(typedPhrase)
        && this.typedPhrases2relations.get(typedPhrase).containsKey(relation)) {
      int totRel = this.typedPhrases2relations.get(typedPhrase).get(relation);
      int totLab = 0;
      if (this.typedPhrasesLabeled.containsKey(typedPhrase))
        totLab = this.typedPhrasesLabeled.get(typedPhrase);
      int totUnl = 0;
      if (this.typedPhrasesUnlabeled.containsKey(typedPhrase))
        totUnl = this.typedPhrasesUnlabeled.get(typedPhrase);
      relationProbability = (double) totRel / (totLab + totUnl);
    } else {
      relationProbability = 0.0;
    }
    return relationProbability;
  }


  @Override
  public Pair<String, Double> predict(String subject_type, String phrase_placeholder,
      String object_type) {
    return predictRelation(subject_type, phrase_placeholder, object_type);
  }

  /**
   * 
   * @return
   */
  public String getName() {
    return this.model.name() + "-" + this.getMinF() + "-" + this.percentageUnlabeled + "-" + this.majorityThreshold;
  }



}
