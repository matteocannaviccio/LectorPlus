package it.uniroma3.model.evaluation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.model.console.ModelIndexer;
import it.uniroma3.model.console.ModelIndexer.IndexType;

public class RelationsSimilarity {

  /**
   * 
   * @param first_relation
   * @param second_relation
   * @return
   */
  public static Double calculateJSD(CounterMap<String> first_relation,
      CounterMap<String> second_relation) {

    Map<String, Double> first_normalized = first_relation.normalize();
    Map<String, Double> second_normalized = second_relation.normalize();
    Map<String, Double> avg = avg(first_normalized, second_normalized);

    double kl_first = calculateKLD(first_normalized, avg);
    double kl_second = calculateKLD(second_normalized, avg);
    double result = (kl_first + kl_second) / 2;
    return result;
  }

  /**
   * 
   * @param first_normalized
   * @param second_normalized
   * @return
   */
  private static Map<String, Double> avg(Map<String, Double> first_normalized,
      Map<String, Double> second_normalized) {
    Map<String, Double> avg = new HashMap<>();
    Set<String> mySet = new HashSet<String>();
    mySet.addAll(first_normalized.keySet());
    mySet.addAll(second_normalized.keySet());

    for (String tp : mySet) {
      double first = 0.0;
      if (first_normalized.containsKey(tp))
        first = first_normalized.get(tp);
      double second = 0.0;
      if (second_normalized.containsKey(tp))
        second = second_normalized.get(tp);
      avg.put(tp, (first + second) / 2);
    }
    return avg;
  }


  /**
   * 
   * @param first_relation
   * @param second_relation
   * @return
   */
  public static Double calculateKLD(Map<String, Double> first_normalized,
      Map<String, Double> second_normalized) {
    double result = 0.0;
    boolean intersect = false;
    for (String token : first_normalized.keySet()) {
      double prob_first = first_normalized.get(token);
      double prob_second = 0.0;
      if (second_normalized.containsKey(token)) {
        intersect = true;
        prob_second = second_normalized.get(token);
        result += prob_first * (Math.log(prob_first / prob_second));
      }
    }

    if (!intersect)
      result = -1.0;

    return result;
  }



  public static void main(String[] args) {
    Configuration.init(new String[0]);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
    Configuration.updateParameter("language", "en");

    final int PERCENTAGE = 100;
    ModelIndexer model_indexes = new ModelIndexer(
        Configuration.getLectorFolder() + "/model_indexer_" + PERCENTAGE + "/", PERCENTAGE);
    Map<String, CounterMap<String>> rels =
        model_indexes.matchAllComposite(IndexType.relations2typedphrases);

    for (String relA : rels.keySet()) {
      Map<String, Double> probs = new HashMap<String, Double>();

      for (String rel : rels.keySet()) {
        double js = RelationsSimilarity.calculateJSD(rels.get(relA), rels.get(rel));
        if (js > 0.0)
          probs.put(rel, js);
      }
      System.out.println(relA + "\t" + Ranking.getInverseTopKRanking(probs, 3));
    }
  }

}
