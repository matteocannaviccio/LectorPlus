package it.uniroma3.model.evaluation;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;

public class CreateCrossValidation {

  private static void create() {
    Lector.init("FE");
    System.out.println("\nEvaluation");
    System.out.println("----------");

    String dbname = Configuration.getLectorFolder() + "/" + "cross.db";
    System.out.println(dbname);
    Evaluator evaluator = new Evaluator(dbname, Lector.getDbmodel(false));
    evaluator.getClass();

    System.out.println("Done");
    Lector.close();

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    Configuration.init(args);
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);
      System.out.println("\n===================================");
      System.out.println("Starting a new LectorPlus execution");
      System.out.println("===================================");
      Configuration.printFullyDetails();
      CreateCrossValidation.create();
    }

  }

}
