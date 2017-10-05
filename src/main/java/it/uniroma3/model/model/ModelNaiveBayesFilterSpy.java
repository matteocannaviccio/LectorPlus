package it.uniroma3.model.model;

import java.util.Map;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBLector;
import it.uniroma3.model.spy.SpyThresholds;

public class ModelNaiveBayesFilterSpy extends ModelNaiveBayes {

  private Map<String, Double> spyThresholds = SpyThresholds.getSpyThresholds();


  /**
   * 
   * @param db_model
   * @param evidence_table
   * @param minF
   * @param modelType
   * @param ruleOfMajority
   */
  public ModelNaiveBayesFilterSpy(DBLector db_model, String evidence_table, int minF,
      ModelType modelType, double majorityThreshold) {
    super(db_model, evidence_table, minF, 100, modelType, majorityThreshold);
  }

  /**
   * 
   */
  public Pair<String, Double> predict(String subject_type, String phrase_placeholder,
      String object_type) {
    Pair<String, Double> prediction = this.predict(subject_type, phrase_placeholder, object_type);
    if (!prediction.getKey().equals("UNKNOWN") && !prediction.getKey().equals("MAJRUL")
        && !prediction.getKey().equals("NONE")) {
      if (prediction.value < this.spyThresholds.get(prediction.key))
        prediction = Pair.make("SPY", prediction.getValue());
    }
    return prediction;
  }
}
