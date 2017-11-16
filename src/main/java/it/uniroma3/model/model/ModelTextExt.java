package it.uniroma3.model.model;

import java.util.HashMap;
import java.util.Map;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBLector;

/**
 * 
 * @author matteo
 *
 */
public class ModelTextExt extends Model {

  /*
   * A model is essentially a mapping between phrases and relations. This mapping is obtained using
   * scoring function applied to each phrase.
   */
  private Map<String, String> patterns = new HashMap<String, String>();
  private double generality_cutoff = 0.1;

  /**
   * 
   */
  protected Map<String, CounterMap<String>> phrases2relations =
      new HashMap<String, CounterMap<String>>();
  protected CounterMap<String> phrasesLabeled = new CounterMap<String>();
  protected CounterMap<String> phrasesUnlabeled = new CounterMap<String>();
  protected int totPhrasesLabeled;
  protected int totPhrasesUnlabeled;

  /**
   * 
   * @param db_model
   * @param evidenceTableName
   * @param minF
   * @param modelType
   */
  public ModelTextExt(DBLector db_model, String evidenceTableName, int minF, ModelType modelType) {
    super(db_model, evidenceTableName, minF, 100, modelType);
    init(evidenceTableName);
    assignPhrasesToRelations();

  }

  /**
   * 
   * @param evidenceTableName
   */
  protected void init(String model_triples) {
    for (Map.Entry<String, CounterMap<String>> tp2r : typedPhrases2relations.entrySet()) {
      String phrase = tp2r.getKey().split("\t")[1];
      if (!phrases2relations.containsKey(phrase))
        phrases2relations.put(phrase, new CounterMap<String>());
      phrases2relations.get(phrase).addAll(tp2r.getValue());
      if (tp2r.getValue().containsKey("NONE"))
        phrasesUnlabeled.add(phrase, tp2r.getValue().get("NONE"));
      phrasesLabeled.add(phrase, tp2r.getValue().calculateSumAvoidThis("NONE"));
    }
    totPhrasesUnlabeled = phrasesUnlabeled.calculateSum();
    totPhrasesLabeled = phrasesLabeled.calculateSum();
  }

  /**
   * 
   * @return
   */
  private Map<String, String> assignPhrasesToRelations() {

    Map<String, Double> phrase2prob = new HashMap<String, Double>();

    // for each relation
    for (Map.Entry<String, Integer> entry : relations.entrySet()) {
      String relation = entry.getKey();

      if (relation.equals("NONE"))
        continue;

      // for each phrase
      for (Map.Entry<String, Integer> labeledTypedPhrase : this.typedPhrasesLabeled.entrySet()) {
        String typed_phrase = labeledTypedPhrase.getKey();

        if (typedPhrases2relations.get(typed_phrase).containsKey(relation)) {

          double tpL = typedPhrasesLabeled.get(typed_phrase);
          double tpR = typedPhrases2relations.get(typed_phrase).get(relation);

          // now split the phrase
          if (typed_phrase.split("\t").length != 3)
            continue;
          String phrase = typed_phrase.split("\t")[1];

          double pL = phrasesLabeled.get(phrase);
          double pR = phrases2relations.get(phrase).get(relation);

          double pU = 0.0;
          if (phrasesUnlabeled.containsKey(phrase))
            pU = phrasesUnlabeled.get(phrase);

          double probLab = tpR / tpL;
          double probSeedLabUnlab = pR / (pL + pU);

          /*
           * add to the model only one relation for each phrase i.e. save the highest probability
           * value associated to each phrase with a relation
           * 
           */
          if (!phrase2prob.containsKey(typed_phrase) || phrase2prob.get(typed_phrase) < probLab) {
            if (probSeedLabUnlab > generality_cutoff && probLab >= 0.1) {
              phrase2prob.put(typed_phrase, probLab);
              patterns.put(typed_phrase, relation);
            }
          }

        }
      }
    }
    return patterns;
  }

  /**
   * 
   * @param relations2phrase_details
   */
  /*
   * private void printDetails(Map<String, Map<String, Double[]>> relations2phrase_details){ try {
   * CSVWriter writer = new CSVWriter(new FileWriter(Configuration.getLectorFolder() +
   * "/lector_textext_score.csv"), ','); // header writer.writeNext(new String[]{ "relation",
   * "phrase", "c(tpR)", "c(tpL)", "c(tpU)", "c(pR)", "c(pL)", "c(pU)", "P(tpR)", "P(tpRU)",
   * "P(pR)", "P(pRU)", "NONE"});
   * 
   * // content for (Map.Entry<String, Map<String, Double[]>> relation :
   * relations2phrase_details.entrySet()){ for (Map.Entry<String, Double[]> phrase :
   * Ranking.getDoubleKRanking(relation.getValue(), 9, -1).entrySet()){ String[] values = new
   * String[13]; //values[0] = Lector.getDBPedia().getOntologyURI() + relation.getKey(); values[0] =
   * "https://dbpedia.org/ontology/" + relation.getKey(); values[1] = phrase.getKey(); values[2] =
   * String.valueOf(phrase.getValue()[0]); // c(tpR) values[3] =
   * String.valueOf(phrase.getValue()[1]); // c(tpL) values[4] =
   * String.valueOf(phrase.getValue()[2]); // c(tpU) values[5] =
   * String.valueOf(phrase.getValue()[3]); // c(pR) values[6] =
   * String.valueOf(phrase.getValue()[4]); // c(pL) values[7] =
   * String.valueOf(phrase.getValue()[5]); // c(pU) values[8] =
   * String.valueOf(phrase.getValue()[6]); // P(tpR) values[9] =
   * String.valueOf(phrase.getValue()[7]); // P(tpRU) values[10] =
   * String.valueOf(phrase.getValue()[8]); // P(pR) values[11] =
   * String.valueOf(phrase.getValue()[9]); // P(pRU) values[12] =
   * String.valueOf(phrase.getValue()[10]); // NONE writer.writeNext(values); } } writer.close(); }
   * catch (IOException e) { e.printStackTrace(); } }
   */



  /**
   * This is the only method that can be used from the clients.
   * 
   * @param phrase
   * @return
   */
  private Pair<String, Double> predictRelation(String subject_type, String phrase_placeholder, String object_type) {
    String typed_phrase = subject_type + "\t" + phrase_placeholder + "\t" + object_type;
    String relation = NegativePredictions.NONE.name();
    if (patterns.containsKey(typed_phrase)) {
      relation = patterns.get(typed_phrase);
    }
    return Pair.make(relation, 1.0);

  }

  /**
   * @return the patterns
   */
  public Map<String, String> getPatterns() {
    return patterns;
  }

  @Override
  public Pair<String, Double> predict(String subject_type, String phrase_placeholder,
      String object_type) {
    return this.predictRelation(subject_type, phrase_placeholder, object_type);
  }


}
