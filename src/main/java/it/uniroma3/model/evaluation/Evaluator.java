package it.uniroma3.model.evaluation;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBCrossValidation;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;

/**
 * 
 * @author matteo
 *
 */
public class Evaluator {

  private DBCrossValidation dbcrossvaliadation;
  private Model current_model;

  /**
   * This is a map containing 3 numbers for each relation: [0] = true positive [1] = false positive
   * [2] = all predicted
   */
  private Map<String, int[]> relation2counts = new HashMap<String, int[]>();
  private int totalCounts;
  private Map<String, Double> relation2accuracy;
  private Map<String, Integer> relation2hits;
  private Pair<Double, Double> evals;

  /**
   * 
   * @param model
   * @param db_read
   */
  public Evaluator(String dbcrossvalidationName, DBModel dbmodel) {
    this.dbcrossvaliadation = loadOrCreateCrossValidationDB(dbcrossvalidationName, dbmodel, 5);
  }

  /**
   * 
   */
  private void initializeCounts() {
    relation2counts = new HashMap<String, int[]>();
    totalCounts = 0;
  }

  /**
   * 
   * @param crossDBName
   * @param dbmodel
   * @param nParts
   * @return
   */
  private DBCrossValidation loadOrCreateCrossValidationDB(String crossDBName, DBModel dbmodel,
      int nParts) {
    if (!new File(crossDBName).exists())
      return new DBCrossValidation(crossDBName, nParts, dbmodel);
    else
      return new DBCrossValidation(crossDBName);
  }


  /**
   * Process the triple to label. It can not have the same entities as subject and object. Return a
   * true value if we can extract a new facts, false otherwise.
   * 
   * @param t
   * @return
   */
  private Pair<String, Double> processRecord(String subjectType, String phrase, String objectType) {
    return current_model.predict(subjectType, phrase, objectType);
  }

  /**
   * 
   * @param table_name
   */
  private void runEvaluation(String table_name, int max) {
    int cont = 0;
    String all =
        "SELECT phrase_placeholder, wiki_subject, wiki_object, type_subject, type_object FROM "
            + table_name;
    try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()) {
      try (ResultSet rs = stmt.executeQuery(all)) {
        while (rs.next() && cont < max) {
          cont += 1;
          String phrase = rs.getString(1);
          String wikiSubject = rs.getString(2);
          String wikiObject = rs.getString(3);
          String subjectType = rs.getString(4);
          String objectType = rs.getString(5);

          if (!wikiSubject.equals(wikiObject)) {
            updateCounts(subjectType, phrase, objectType, wikiSubject, wikiObject);
          }
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }

  }



  /**
   * 
   * @param t
   */
  private void updateCounts(String subjectType, String phrase, String objectType,
      String wikiSubject, String wikiObject) {
    Pair<String, Double> pred = processRecord(subjectType, phrase, objectType);
    String prediction = pred.key;
    double prob = pred.value;
    totalCounts += 1;

    // controlla se abbiamo recuperato qualcosa
    if (Model.isPositivePrediction(prediction)) {

      // abbiamo fatto una predizione
      Set<String> expected = Lector.getDBPedia().getRelations(wikiSubject, wikiObject);

      if (!relation2counts.containsKey(prediction))
        relation2counts.put(prediction, new int[3]);
      relation2counts.get(prediction)[2] += 1;

      if (expected.contains(prediction)) {
        // System.out.println("TRUE POSITIVE\t" + "expected: " + expected + " - predicted: " +
        // prediction +"("+prob+") ---> " + subjectType + " " + phrase+ " " + objectType);
        relation2counts.get(prediction)[0] += 1;
      } else {
        // System.out.println("FALSE POSITIVE\t" + "expected: " + expected + " - predicted: " +
        // prediction +"("+prob+") ---> " + subjectType + " " + phrase+ " " + objectType);
        relation2counts.get(prediction)[1] += 1;
      }
    }
    // else
    // System.out.println("FALSE POSITIVE\t" + "expected: " + "-" + " - predicted: " + prediction
    // +"("+prob+") ---> " + subjectType + " " + phrase+ " " + objectType);

  }



  /**
   * 
   * @return
   */
  private Map<String, Double> calcAccuracyPerRelation() {
    /**
     * This is a map containg the accuracy for each relation
     */
    Map<String, Double> relation2accuracy = new HashMap<String, Double>();
    for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()) {
      int tp = relation2counts.get(perRelationCount.getKey())[0];
      int all_predicted = relation2counts.get(perRelationCount.getKey())[2];
      double accuracy = (double) tp / (all_predicted);
      relation2accuracy.put(perRelationCount.getKey(), accuracy);
    }
    return relation2accuracy;
  }

  /**
   * 
   * @return
   */
  private Map<String, Integer> calcHitsPerRelation() {
    Map<String, Integer> relation2hits = new HashMap<String, Integer>();
    for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()) {
      int all_predicted = relation2counts.get(perRelationCount.getKey())[2];
      relation2hits.put(perRelationCount.getKey(), all_predicted);
    }
    return relation2hits;
  }

  /**
   * 
   * @param relation2counts
   * @return
   */
  private Pair<Double, Double> calcAccuracyRecallGlobal(Map<String, int[]> relation2counts) {
    int tp_global = 0;
    int fp_global = 0;
    for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()) {
      tp_global += relation2counts.get(perRelationCount.getKey())[0];
      fp_global += relation2counts.get(perRelationCount.getKey())[1];
    }
    double precision = (double) tp_global / (tp_global + fp_global);
    double recall = (double) tp_global / totalCounts;
    return Pair.make(precision, recall);
  }

  /**
   * 
   * @param nParts
   */
  private void runCrossValidation(ModelType type, int limit, int minF, int percUnl, int nParts) {
    double avg_acc = 0.0;
    double avg_rec = 0.0;

    for (int it = 0; it < nParts; it++) {
      String labeled_table = "CV_evidence_" + it;
      String evaluation_table = "CV_evaluation_triples_" + it;
      initializeCounts();
      this.current_model =
          Model.getNewModel(this.dbcrossvaliadation, labeled_table, minF, percUnl, type, 0.4);
      this.runEvaluation(evaluation_table, limit);
      Pair<Double, Double> avg_measures = calcAccuracyRecallGlobal(relation2counts);
      avg_acc += avg_measures.key;
      avg_rec += avg_measures.value;
    }

    relation2accuracy = calcAccuracyPerRelation();
    relation2hits = calcHitsPerRelation();
    evals = Pair.make(avg_acc / nParts, avg_rec / nParts);
  }

  /**
   * @return the relation2counts
   */
  public Map<String, int[]> getRelation2counts() {
    return relation2counts;
  }

  /**
   * @return the relation2accuracy
   */
  public Map<String, Double> getRelation2accuracy() {
    return relation2accuracy;
  }

  /**
   * 
   * @throws IOException
   */
  private void run(int limit) throws IOException {
    int nParts = 1;

    List<ModelType> models = new ArrayList<ModelType>();
    models.add(ModelType.NaiveBayes);

    Integer[] mF = new Integer[] {1};
    Integer[] tH = new Integer[] {0, 100, 25};

    for (ModelType type : models) {
      for (int m : mF) {
        for (int t : tH) {
          runCrossValidation(type, limit, m, t, nParts);
          double precision = evals.key;
          double recall = evals.value;
          String nameModel = type + " (" + limit + ") - " + tH;
          System.out.printf("\t%-20s %-20s %-20s %s\n", nameModel, String.format("%.2f", precision),
              String.format("%.2f", recall),
              String.format("%.2f", (2 * recall * precision) / (precision + recall)));
        }
      }
    }
  }

  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
      Configuration.updateParameter("language", lang);
      System.out.println("\n======================================");
      System.out.println("Starting a new LectorPlus EVALUATION");
      System.out.println("======================================");

      Lector.init("FE");

      System.out.println("\nRunning Evaluation in: " + Configuration.getLanguageCode());
      System.out.println("-------------------------");
      Evaluator evaluator =
          new Evaluator(Configuration.getDBCrossValidation(), Lector.getDbmodel(false));
      evaluator.run(500);
      Lector.close();
    }

  }

}
