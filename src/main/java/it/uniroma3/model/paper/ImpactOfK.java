package it.uniroma3.model.paper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.inout.TSVReader;
import it.uniroma3.model.model.Model.ModelType;

public class ImpactOfK {

  /**
   * 
   * @param provenance
   */
  private static void printNuberOfFacts(File provenance, double maj,  int k){
    Set<String> content = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);
    System.out.println(k + "\t" + maj + "\t" + content.size());
  }


  public static void main(String[] args){
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
    //for (String lang : Configuration.getLanguages()) {
    String lang = "en";
    Configuration.updateParameter("language", lang);

    List<ModelType> models = new ArrayList<ModelType>();
    models.add(ModelType.ModelNaiveBayes);
    //models.add(ModelType.ModelTextExt);

    List<Double> majorities = new ArrayList<Double>();
    majorities.add(0.0);
    majorities.add(0.4);
    //majorities.add(0.5);

    List<Integer> percentages = new ArrayList<Integer>();
    percentages.add(0);
    percentages.add(5);
    percentages.add(10);
    percentages.add(15);
    percentages.add(20);
    percentages.add(25);
    percentages.add(30);
    percentages.add(35);
    percentages.add(40);
    percentages.add(45);
    percentages.add(50);
    percentages.add(55);
    percentages.add(60);
    percentages.add(65);
    percentages.add(70);
    percentages.add(75);
    percentages.add(80);
    percentages.add(85);
    percentages.add(90);
    percentages.add(95);
    percentages.add(100);
    
    System.out.println("model\tp\tfacts");

    File provenance;
    for (ModelType type : models) {
      if (type.equals(ModelType.ModelTextExt)) {
        Configuration.updateParameter("lectorModel", type.name());
        provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/" + lang + "_provenance.bz2");
        printNuberOfFacts(provenance, Configuration.getMajThr(), Configuration.getPercUnl());
      } else {
        for (Double majThr : majorities) {
          for (Integer k : percentages) {
            Configuration.updateParameter("lectorModel", type.name());
            Configuration.updateParameter("minF", "1");
            Configuration.updateParameter("percUnl", k.toString());
            Configuration.updateParameter("majorityThreshold", majThr.toString());
            provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/" + lang + "_provenance.bz2");
            printNuberOfFacts(provenance, Configuration.getMajThr(), Configuration.getPercUnl());
          }
        }
      }
    }
    // }
  }
}

