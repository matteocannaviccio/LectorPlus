package it.uniroma3.model.paper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.util.Pair;

public class DBPediaStats {

  private static DBPedia db;

  private static void getAll(String category, String relation){
    int totalPairs = 0;
    int totalPairsWithTypes = 0;
    int totalPairsWithTypesAndCategory = 0;
    List<Pair<String, String>> listFacts = db.getAllFacts(relation);
    for (Pair<String, String> entry : listFacts){
      String categorySubject = db.getTypeCategory(entry.key);
      String categoryObject = db.getTypeCategory(entry.value);
      totalPairs++;
      if (!categorySubject.equals("-") && !categoryObject.equals("-")){
        totalPairsWithTypes++;
        if (categorySubject.equals(category))
          totalPairsWithTypesAndCategory++;
      }
    }
    System.out.printf("%-20s %-20s %-20s %s\n", relation, totalPairs, totalPairsWithTypes, totalPairsWithTypesAndCategory);
  }


  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
    Configuration.updateParameter("language", "it");
    db = new DBPedia();
    List<Pair<String, String>> category2relations = new ArrayList<Pair<String, String>>(21);
    category2relations.add(Pair.make("Person", "birthPlace"));
    category2relations.add(Pair.make("Person", "team"));
    category2relations.add(Pair.make("Person", "deathPlace"));
    category2relations.add(Pair.make("Person", "almaMater"));
    category2relations.add(Pair.make("Person", "nationality"));
    category2relations.add(Pair.make("Person", "spouse"));
    
    category2relations.add(Pair.make("Place", "isPartOf"));
    category2relations.add(Pair.make("Place", "location"));
    category2relations.add(Pair.make("Place", "department"));
    category2relations.add(Pair.make("Place", "country"));
    category2relations.add(Pair.make("Place", "district"));
    
    category2relations.add(Pair.make("Organisation", "city"));
    category2relations.add(Pair.make("Organisation", "ground"));
    category2relations.add(Pair.make("Organisation", "hometown"));
    category2relations.add(Pair.make("Organisation", "country"));
    category2relations.add(Pair.make("Organisation", "location"));
    
    category2relations.add(Pair.make("Work", "starring"));
    category2relations.add(Pair.make("Work", "musicalArtist"));
    category2relations.add(Pair.make("Work", "country"));
    category2relations.add(Pair.make("Work", "writer"));
    category2relations.add(Pair.make("Work", "artist"));
    
    
    for (Pair<String, String> entry : category2relations){
      getAll(entry.key, entry.value);
    }
  }

}
