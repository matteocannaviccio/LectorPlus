package it.uniroma3.main.kg.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.kg.normalizer.CleanDBPediaRelations;
import it.uniroma3.main.kg.normalizer.Normalizer;
import it.uniroma3.main.util.KeyValueIndex;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;

/**
 * 
 * @author matteo
 *
 */
public class RelationsResolver {

  private static KeyValueIndex indexKG;
  private CleanDBPediaRelations cleaner;

  /**
   * 
   */
  public RelationsResolver() {
    this.cleaner = new CleanDBPediaRelations();
    this.indexKG =
        getIndexOrCreate(Configuration.getDBPediaIndex(), Configuration.getDBPediaDumpFile());
  }

  /**
   * Returns a KeyValueIndex given the path. If the exists does not exist it create it and then
   * return.
   * 
   * @param indexPath
   * @param sourcePath
   * @return
   */
  private KeyValueIndex getIndexOrCreate(String indexPath, String sourcePath) {
    KeyValueIndex index = null;
    if (!new File(indexPath).exists()) {
      System.out.printf("\t\t%-20s %-20s %s", "--> Read & Index:", "DBpedia", "");

      long start_time = System.currentTimeMillis();
      List<Pair<String, String>> dbpedia_dump =
          Normalizer.normalizeMappingBasedDBPediaDump(Configuration.getDBPediaDumpFile());
      index = new KeyValueIndex(dbpedia_dump, indexPath);
      long end_time = System.currentTimeMillis();

      System.out.printf("%-20s %s\n", "lines: " + index.getIndexedLines(),
          "indexed in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
    } else // we already have the index
      index = new KeyValueIndex(indexPath);

    return index;
  }

  /**
   * It queries the KG index looking for a pair of entities that match the input subject and object.
   * If none pairs match, it looks for the inverse order (adding -1 to the label of the relation).
   * 
   * @param wikidSubject
   * @param wikidObject
   * @return
   */
  public Set<String> getRelations(String wikidSubject, String wikidObject) {
    Set<String> relations = new HashSet<String>();
    for (String relation : indexKG.retrieveValues(wikidSubject + "###" + wikidObject)) {
      String candidate = cleaner.cleanRelation(relation);
      if (!candidate.equals("notValid"))
        relations.add(candidate);
    }
    for (String relation : indexKG.retrieveValues(wikidObject + "###" + wikidSubject)) {
      String candidate = cleaner.cleanRelation(relation + "(-1)");
      if (!relations.contains(relation) && !candidate.equals("notValid"))
        relations.add(candidate);
    }
    return relations;
  }

  /**
   * 
   * @param relation
   * @return
   */
  public String getInstances(String relation, int max) {
    StringBuffer sb = new StringBuffer();
    int tot = 0;
    if(max==-1)
      max = Integer.MAX_VALUE;
    for (String instance : indexKG.retrieveKeys(relation))
      if (tot < max) {
        sb.append(instance.split("###")[0] + "\t" + instance.split("###")[1] + "\n");
        tot += 1;
      }
    return sb.toString();
  }

  /**
   * 
   * @param subject
   * @param object
   */
  void findRelations(String subject, String object) {
    System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
    for (String relation : getRelations(subject, object))
      System.out.println("\t" + cleaner.cleanRelation(relation));
  }

  /**
   * 
   */
  private void getAllRelations() {
    for (Map.Entry<String, Integer> relation : Ranking.getRanking(indexKG.matchAll()).entrySet()) {
      System.out.println(relation.getKey() + "\t" + relation.getValue());
    }
  }
  

  /**
   * @return the indexKG
   */
  public static KeyValueIndex getIndexKG() {
    return indexKG;
  }


  /**
   * TEST MAIN.
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    Configuration.updateParameter("language", "en");
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");

    RelationsResolver res = new RelationsResolver();
    /*
    try {
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
          new FileOutputStream("/Users/matteo/Desktop/dbpedia_mapping-based-objects.tsv"),
          "UTF-8"));
      indexKG.matchAllPairs().stream().forEach(s -> {
        try {
          String result = java.net.URLDecoder.decode(s, "UTF-8");
          bw.write(result + "\n");
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
      bw.close();
    } catch (IOException e) {

    }
     */

    String subject = "Michelle_Obama";
    String object = "Barack_Obama";

    //res.findRelations(object, subject);

    // res.getAllRelations();
    String relation = "manufacturer";
    System.out.println(res.getInstances(relation, 200));



  }

}
