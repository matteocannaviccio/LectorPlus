package it.uniroma3.model.alternatives;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBCrossValidation;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.ModelNaiveBayes;

/**
 * This is the code to use to find the reliability thresholds using the "spy" mechanism.
 * 
 * @author matteo
 *
 */
public class Estimator {

  private DBCrossValidation dbcrossvaliadation; // we exploit the random folds from the cross
                                                // validation db
  private ModelNaiveBayes current_model;

  /**
   * 
   * @param model
   * @param db_read
   */
  public Estimator(String dbcrossvalidationName, DBModel dbmodel) {
    this.dbcrossvaliadation = loadOrCreateCrossValidationDB(dbcrossvalidationName, dbmodel, 5);
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
   * It iterates over every language and for each one it runs the estimator.
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    for (String lang : Configuration.getLanguages()) {
      // Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
      Configuration.updateParameter("language", lang);

      System.out.println("\nRunning Evaluation in: " + Configuration.getLanguageCode());
      System.out.println("-------------------------");

      Lector.init("FE");

      Estimator evaluator =
          new Estimator(Configuration.getDBCrossValidation(), Lector.getDbmodel(false));
      int nParts = 5;
      int limit = Integer.MAX_VALUE;
      evaluator.run(nParts, limit);

      Lector.close();
    }
  }

  /**
   * fold: is the identifier of the relative fold in the cross-validation db (5 folds).
   * 
   * @param nParts
   * @param limit
   */
  private void run(int nParts, int limit) {

    String column_file = Configuration.getDataFolder() + "/per_relations.tsv";
    String threshold_file = Configuration.getDataFolder() + "/trhasholds.tsv";

    try {
      BufferedWriter bw_columns = new BufferedWriter(new FileWriter(new File(column_file)));
      BufferedWriter bw_thresholds = new BufferedWriter(new FileWriter(new File(threshold_file)));

      List<Map<String, Pair<Double, Integer>>> folds =
          new ArrayList<Map<String, Pair<Double, Integer>>>(nParts);

      for (int fold = 0; fold < nParts; fold++) {
        System.out.println("Running iteration: " + (fold + 1));
        String model_triples = "CV_evidence_" + fold;
        String evaluation_table = "CV_evaluation_triples_" + fold;
        this.current_model = null;
        this.current_model = (ModelNaiveBayes) Model.getNewModel(dbcrossvaliadation, model_triples,
            1, 0, ModelType.ModelNaiveBayes, 0.4);
        folds.add(this.runEstimation(evaluation_table, limit));
      }

      /*
       * the relations we use are forced to be in every fold (intersection). we start from all the
       * relations from the model.
       */
      Map<String, Double> thresholds = new HashMap<String, Double>();

      for (String rel : this.current_model.getRelations().keySet()) {

        // skip the unknown relation
        if (rel.equals("NONE"))
          continue;

        int[] amounts = new int[5];
        double[] thrsholds = new double[5];
        for (int j = 0; j < folds.size(); j++) {
          if (folds.get(j).containsKey(rel)) {
            thrsholds[j] = folds.get(j).get(rel).key;
            amounts[j] = folds.get(j).get(rel).value;
          } else {
            thrsholds[j] = 0;
            amounts[j] = 0;
          }
        }

        String zero = String.format("%.2f", thrsholds[0]) + "(" + amounts[0] + ")";
        String one = String.format("%.2f", thrsholds[1]) + "(" + amounts[1] + ")";
        String two = String.format("%.2f", thrsholds[2]) + "(" + amounts[2] + ")";
        String three = String.format("%.2f", thrsholds[3]) + "(" + amounts[3] + ")";
        String four = String.format("%.2f", thrsholds[4]) + "(" + amounts[4] + ")";

        bw_columns
            .write(rel + "\t" + zero + "\t" + one + "\t" + two + "\t" + three + "\t" + four + "\n");
        bw_columns.flush();

        int minThreshold = 400;
        if (amounts[0] > minThreshold && amounts[1] > minThreshold && amounts[2] > minThreshold
            && amounts[3] > minThreshold && amounts[4] > minThreshold) {
          thresholds.put(rel,
              (double) (thrsholds[0] + thrsholds[1] + thrsholds[2] + thrsholds[3] + thrsholds[4])
                  / 5);
        }

        // System.out.printf("\t%-20s %-20s %-20s %-20s %-20s %s\n", rel, zero, one, two, three,
        // four);

        // System.out.println(rel2values.getKey() + "\t" + (rel2values.getValue().key/nParts) + "\t"
        // + rel2values.getValue().value);
      }

      for (String rel : thresholds.keySet()) {
        bw_thresholds.write(rel + "\t" + String.format("%.2f", thresholds.get(rel)) + "\n");
        bw_thresholds.flush();
      }

      bw_columns.close();
      bw_thresholds.close();

    } catch (IOException e) {
      e.printStackTrace();
    }


  }

  /**
   * 
   * @param subjectType
   * @param phrase
   * @param objectType
   * @param wikiSubject
   * @param wikiObject
   * @return
   */
  private Pair<String, Double> getPredictionWithRelativePercLAB(String subjectType, String phrase,
      String objectType, String wikiSubject, String wikiObject) {
    String rel = "-";
    double probREL = 0.0;
    Pair<String, Double> basePred = Pair.make(rel, probREL);

    Pair<String, Double> prediction = this.current_model.predict(subjectType, phrase, objectType);

    if (!prediction.getKey().equals("PF") && !prediction.getKey().equals("LT")
        && !prediction.getKey().equals("UT")) {
      Set<String> expected = Lector.getDBPedia().getRelations(wikiSubject, wikiObject);
      if (expected.contains(prediction.key)) {
        rel = prediction.key;
        String typedPhrase = subjectType + "\t" + phrase + "\t" + objectType;

        /*
         * UNCOMMENT THIS TO USE THE LABELED PERCENTAGE AS PROBABILITY first : labeled percentage
         * second : labeled relation percentage third : labeled relation percentage only positive
         */
        // double probability = current_model.getLabeledPercentage(typedPhrase);
        double probability = current_model.getRelationProbability(typedPhrase, rel);
        // double probability = prediction.value;

        basePred =
            Pair.make(subjectType + "\t" + phrase + "\t" + objectType + "\t" + rel, probability);
        // System.out.println(basePred);
      }
    }
    return basePred;
  }

  /**
   * 
   * @param table_name
   * @param limit
   * @return
   */
  private Map<String, Pair<Double, Integer>> runEstimation(String table_name, int limit) {

    // inizializza le liste per tutte le relazioni
    Map<String, List<Double>> estimations = new HashMap<String, List<Double>>();
    for (String rel : this.current_model.getRelations().keySet()) {
      if (!rel.equals("NONE"))
        estimations.put(rel, new LinkedList<Double>());
    }

    /*
     * inizializza una mappa d'appoggio per la stampa delle frasi: spouse - [(1.0: married), (0.9:
     * wife of, wife), ...]
     */
    Map<String, Map<Double, List<String>>> contents =
        new HashMap<String, Map<Double, List<String>>>();
    for (String rel : this.current_model.getRelations().keySet()) {
      contents.put(rel, new TreeMap<Double, List<String>>());
    }

    // create the list of predictions
    int cont = 0;
    String all =
        "SELECT phrase_placeholder, wiki_subject, wiki_object, type_subject, type_object FROM "
            + table_name;
    try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()) {
      try (ResultSet rs = stmt.executeQuery(all)) {
        while (rs.next()) {
          cont += 1;
          if (cont > limit)
            break;
          String phrase = rs.getString(1);
          String wikiSubject = rs.getString(2);
          String wikiObject = rs.getString(3);
          String subjectType = rs.getString(4);
          String objectType = rs.getString(5);

          if (!wikiSubject.equals(wikiObject)) {
            // here we ask for a prediction of the model to the typed phrase
            // (NaiveBayesOnlyPositive) but
            // we also collect the relative percentage of labeled phrases associated with the typed
            // phrase
            Pair<String, Double> pred = getPredictionWithRelativePercLAB(subjectType, phrase,
                objectType, wikiSubject, wikiObject);

            if (!pred.key.equals("-")) {
              String typedphrase = pred.getKey().split("\t")[0] + " " + pred.getKey().split("\t")[1]
                  + " " + pred.getKey().split("\t")[2];
              String relationPredicted = pred.getKey().split("\t")[3];

              // add the k-value to the list associated with the relation
              estimations.get(relationPredicted).add(pred.getValue());

              // add the k-value and typed phrase to the list associated with the relation
              if (!contents.get(relationPredicted).containsKey(pred.getValue()))
                contents.get(relationPredicted).put(pred.getValue(), new LinkedList<String>());
              contents.get(relationPredicted).get(pred.getValue()).add(typedphrase);

              // System.out.println(pred.getKey() + "\t" + pred.getValue());
            }
          }
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }



    // now order the lists and select 80%
    Map<String, Pair<Double, Integer>> relations2avgLABPercentage =
        new HashMap<String, Pair<Double, Integer>>();

    for (Map.Entry<String, List<Double>> rel2values : estimations.entrySet()) {
      if (!rel2values.getValue().isEmpty()) {
        Collections.sort(rel2values.getValue(), Collections.reverseOrder());
        int size = (int) (rel2values.getValue().size() * 0.8); // size is 80% of the list

        double value = rel2values.getValue().get(size);
        int num = rel2values.getValue().size();
        relations2avgLABPercentage.put(rel2values.getKey(), Pair.make(value, num));
        // System.out.println(rel2values.getKey() + "\t" + rel2values.getValue() + "\t" +
        // (contents.get(rel2values.getKey())));
      }
    }

    return relations2avgLABPercentage;
  }


  /**
   * 
   * @param general
   * @param novel
   * @return
   */
  private Map<String, Pair<Double, Integer>> mergeNovelIntoGeneralEstimations(
      Map<String, Pair<Double, Integer>> general, Map<String, Pair<Double, Integer>> novel) {
    Map<String, Pair<Double, Integer>> thirdEstimation =
        new HashMap<String, Pair<Double, Integer>>();
    for (Map.Entry<String, Pair<Double, Integer>> entry : novel.entrySet()) {
      String relation = entry.getKey();
      Pair<Double, Integer> novel_estimations = entry.getValue();
      if (general.containsKey(relation)) {
        Pair<Double, Integer> general_estimations = general.get(relation);
        thirdEstimation.put(relation, Pair.make(general_estimations.key + novel_estimations.key,
            general_estimations.value + novel_estimations.value));
      }
    }
    return thirdEstimation;
  }

}
