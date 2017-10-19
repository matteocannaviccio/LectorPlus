package it.uniroma3.model.paper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.inout.Compressed;
/**
 * 
 * @author matteo
 *
 */
public class ModelStats {

  /**
   * 
   */
  private static void calculateStats(int i) {
    Configuration.updateParameter("percUnl", "" + i);

    Set<String> relations = new HashSet<String>();
    Set<String> typedphrases = new HashSet<String>();
    int cont = 0;

    try {
      BufferedReader reader = Compressed.getBufferedReaderForCompressedFile(
          Configuration.getProvenanceFile(Configuration.getModelCode()));
      String line;
      while ((line = reader.readLine()) != null) {
        cont++;
        if (cont > Integer.MAX_VALUE)
          break;
        try {

          String[] fields = line.split("\t");
          String relation = fields[2].replaceAll("\\(-1\\)", "");
          relations.add(relation);
          String typedPhrase = fields[7];
          typedphrases.add(typedPhrase);


        } catch (Exception e) {
          continue;
        }
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(Configuration.getModelCode());
    System.out.println("Facts: " + cont);
    System.out.println("Relations: " + relations.size());
    System.out.println("TypedP: " + typedphrases.size());
  }


  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);

      System.out.println("\n\n------------------");
      System.out.println("Stats for language : " + Configuration.getLanguageCode());
      System.out.println("------------------");
      
      /*
      List<Integer> tr = new ArrayList<Integer>();
      tr.add(0);
      tr.add(25);
      tr.add(100);
      for (int i : tr)
        ModelStats.calculateStats(i);
        */
      Configuration.updateParameter("lectorModel", "ModelTextExt");
      ModelStats.calculateStats(0);

    }

  }


}
