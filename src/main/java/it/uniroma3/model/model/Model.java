package it.uniroma3.model.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import it.uniroma3.config.Lector;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBLector;

/**
 * A Model is able to predict a DBpedia relation given an input typed phrase. It reads the typed
 * phrases stored in the DB and count the associations among them.
 * 
 * A Model can be: - Naive Bayes (based on a naive bayes classifier) - TextExt (based on a
 * bag-of-patterns that are assigned for each relation)
 * 
 * It is also built consiering two paramenters: - minFrequency a threshold on the minimum
 * occurrences of a typed phrase - percentageUnlabeled the percentage of all unlabeled triples to
 * consider
 * 
 * 
 * @author matteo
 *
 */
public abstract class Model {

  protected DBLector db_model; // the DB that contains typed phrases and the rest
  protected String evidence_table; // the specific table that contains associations (useful to
                                   // distinguish for cross-validation)
  protected int minFrequency; // a threshold on the minimum occurrences of a typed phrase
  protected int percentageUnlabeled; // a weight for the unlabeled triples

  /* Those are the parameters */
  public enum ModelType {
    NaiveBayes, NaiveBayesFilterSpy, ModelTextExt
  };

  protected ModelType model;

  /* Those are needed for the NAIVE BAYES calculation */
  protected Map<String, CounterMap<String>> typedPhrases2relations;
  protected CounterMap<String> typedPhrasesLabeled;
  protected CounterMap<String> typedPhrasesUnlabeled;
  protected CounterMap<String> relations;
  protected int totTypedPhrasesLabeled;
  protected int totTypedPhrasesUnlabeled;

  /* Those are possible negative predictions types by the model */
  public enum NegativePredictions {
    MAJRULE, UNKNOWN, SPY, NONE
  };


  /**
   * 
   * @param db_model
   * @param evidence_table
   * @param minF
   * @param modelType
   */
  public Model(DBLector db_model, String evidence_table, int minF, int percUnl,
      ModelType modelType) {
    /* new parameters */
    this.db_model = db_model;
    this.evidence_table = evidence_table;
    this.minFrequency = minF;
    this.percentageUnlabeled = percUnl;
    this.model = modelType;
    initModel(evidence_table);
  }

  /**
   * 
   * @param evidenceTableName
   */
  protected void initModel(String model_triples) {
    typedPhrases2relations = new HashMap<String, CounterMap<String>>();
    typedPhrasesLabeled = new CounterMap<String>();
    typedPhrasesUnlabeled = new CounterMap<String>();
    relations = new CounterMap<String>();

    // retrieve all the evidences: typed phrases - relation - occ
    for (Map.Entry<String, Integer> entry_model : this.db_model
        .retrieveEvidence(model_triples, minFrequency, percentageUnlabeled).entrySet()) {
      String sbj_type = entry_model.getKey().split("\t")[0];
      String phrase = entry_model.getKey().split("\t")[1];
      String obj_type = entry_model.getKey().split("\t")[2];
      String relation = entry_model.getKey().split("\t")[3];
      String typed_phrase = sbj_type + "\t" + phrase + "\t" + obj_type;
      int occurrences = entry_model.getValue();

      if (!typedPhrases2relations.containsKey(typed_phrase))
        typedPhrases2relations.put(typed_phrase, new CounterMap<String>());
      typedPhrases2relations.get(typed_phrase).add(relation, occurrences);

      if (relation.equals("NONE"))
        typedPhrasesUnlabeled.add(typed_phrase, occurrences);
      else
        typedPhrasesLabeled.add(typed_phrase, occurrences);

      relations.add(relation, occurrences);
    }
    totTypedPhrasesUnlabeled = typedPhrasesUnlabeled.calculateSum();
    totTypedPhrasesLabeled = typedPhrasesLabeled.calculateSum();
  }

  /**
   * 
   */
  public void printStats() {
    System.out.printf("\t%-35s %s\n", "model: ", model);
    System.out.printf("\t%-35s %s\n", "minFrequency: ", minFrequency);
    System.out.printf("\t%-35s %s\n", "percentageUnlabeled: ", percentageUnlabeled + "%");

    System.out.printf("\t%-35s %s\n", "relations (docs):", this.relations.size());
    System.out.printf("\t%-35s %s\n", "relations (real):", countEffectiveRelations(this.relations));

    System.out.printf("\t%-35s %s\n", "labeled t-phrases:", totTypedPhrasesLabeled);
    System.out.printf("\t%-35s %s\n", "unlabeled t-phrases:", totTypedPhrasesUnlabeled);
    System.out.printf("\t%-35s %s\n", "all t-phrases:",
        totTypedPhrasesUnlabeled + totTypedPhrasesLabeled);

  }

  /**
   * 
   * @param relations
   */
  private int countEffectiveRelations(CounterMap<String> relations) {
    Set<String> effectiveRelations = new HashSet<String>();
    for (String relation : relations.keySet()) {
      relation = relation.replace("(-1)", "");
      effectiveRelations.add(relation);
    }
    return effectiveRelations.size();
  }

  /**
   * 
   * @param db_model
   * @param evidenceTable
   * @param minF
   * @param modelType
   * @return
   */
  public static Model getNewModel(DBLector db_model, String evidenceTable, int minF, int percUnl,
      ModelType modelType, double majorityThreshold) {
    Lector.getDbmodel(false).deriveModelTable();
    Model model = null;

    switch (modelType) {
      case NaiveBayesFilterSpy:
        model = new ModelNaiveBayesFilterSpy(db_model, evidenceTable, minF, modelType,
            majorityThreshold);
        break;

      case NaiveBayes:
        model = new ModelNaiveBayes(db_model, evidenceTable, minF, percUnl, modelType,
            majorityThreshold);
        break;

      case ModelTextExt:
        model = new ModelTextExt(db_model, evidenceTable, minF, modelType);
        break;
    }

    return model;
  }


  /**
   * @return the db_model
   */
  public DBLector getDb_model() {
    return db_model;
  }

  /**
   * @return the evidence_table
   */
  public String getEvidence_table() {
    return evidence_table;
  }

  /**
   * @return the minF
   */
  public int getMinF() {
    return minFrequency;
  }

  /**
   * @return the typedPhrases2relations
   */
  public Map<String, CounterMap<String>> getTypedPhrases2relations() {
    return typedPhrases2relations;
  }

  /**
   * @return the typedPhrasesLabeled
   */
  public CounterMap<String> getTypedPhrasesLabeled() {
    return typedPhrasesLabeled;
  }

  /**
   * @return the typedPhrasesUnlabeled
   */
  public CounterMap<String> getTypedPhrasesUnlabeled() {
    return typedPhrasesUnlabeled;
  }

  /**
   * @return the relations
   */
  public CounterMap<String> getRelations() {
    return relations;
  }

  /**
   * @return the totTypedPhrasesLabeled
   */
  public int getTotTypedPhrasesLabeled() {
    return totTypedPhrasesLabeled;
  }

  /**
   * @return the totTypedPhrasesUnlabeled
   */
  public int getTotTypedPhrasesUnlabeled() {
    return totTypedPhrasesUnlabeled;
  }

  /**
   * 
   * @return
   */
  public String getName() {
    return this.model.name();
  }

  /**
   * @return the model
   */
  public ModelType getModel() {
    return model;
  }

  /**
   * 
   * @param typedPhrase
   * @return
   */
  /*
   * public double getLabeledPercentage(String typedPhrase){ int totLab = 0;
   * if(this.typedPhrasesLabeled.containsKey(typedPhrase)) totLab =
   * this.typedPhrasesLabeled.get(typedPhrase); int totUnl = 0;
   * if(this.typedPhrasesUnlabeled.containsKey(typedPhrase)) totUnl =
   * this.typedPhrasesUnlabeled.get(typedPhrase); return (double) totLab/(totLab+totUnl); }
   */

  /**
   * 
   * @param subject_type
   * @param phrase_placeholder
   * @param object_type
   * @return
   */
  public abstract Pair<String, Double> predict(String subject_type, String phrase_placeholder,
      String object_type);

  /**
   * @return the percUnl
   */
  public int getPercUnl() {
    return percentageUnlabeled;
  }

  /**
   * 
   * @param prediction
   * @return
   */
  public static boolean isPositivePrediction(String prediction) {
    return prediction != null && !prediction.equals(NegativePredictions.NONE.name())
        && !prediction.equals(NegativePredictions.UNKNOWN.name())
        && !prediction.equals(NegativePredictions.MAJRULE.name())
        && !prediction.equals(NegativePredictions.SPY.name());
  }


}
