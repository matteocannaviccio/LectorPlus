package it.uniroma3.model.evaluation;

import java.util.ArrayList;
import java.util.List;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.pipeline.factsextractor.FactsExtractor;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;

/**
 * 
 * @author matteo
 *
 */
public class Extractor {

  public Extractor(){

  }

  /**
   * 
   * @param limit
   */
  private static void run(int limit) {

    List<ModelType> models = new ArrayList<ModelType>();
    models.add(ModelType.NaiveBayes);
    models.add(ModelType.ModelTextExt);

    List<Double> majorities = new ArrayList<Double>();
    majorities.add(0.0);
    majorities.add(0.4);
    majorities.add(0.5);

    List<Integer> percentages = new ArrayList<Integer>();
    percentages.add(0);
    percentages.add(25);
    percentages.add(100);

    Model model = null;
    for (ModelType type : models) {
      if (type.equals(ModelType.ModelTextExt)) {
        model = Model.getNewModel(Lector.getDbmodel(false), "model_triples", 1, -1, type, -0.0);
        FactsExtractor extractor = new FactsExtractor(model);
        extractor.runExtractOnFile(limit);
      } else {
        for (Double majThr : majorities) {
          for (Integer t : percentages) {
            model = Model.getNewModel(Lector.getDbmodel(false), "model_triples", 1, t.intValue(),
                type, majThr.doubleValue());
            FactsExtractor extractor = new FactsExtractor(model);
            extractor.runExtractOnFile(limit);
          }
        }
      }
    }
  }

  /**
   * 
   */
  public static void extract(){
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);

      System.out.println("\n===================================");
      System.out.println("Starting a new LectorPlus EXTRACTION");
      System.out.println("===================================");

      Lector.init("FE");

      System.out.println("\nRunning Extraction in: " + Configuration.getLanguageCode());
      System.out.println("-------------------------");

      Extractor.run(Integer.MAX_VALUE);
      //Extractor.run(100000);
      Lector.close();
    }
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
    Extractor.extract();
  }


}
