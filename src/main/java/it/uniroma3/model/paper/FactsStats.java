package it.uniroma3.model.paper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.Compressed;
/**
 * 
 * @author matteo
 *
 */
public class FactsStats {

  private CounterMap<String> personFactsRelationsCount;
  private CounterMap<String> locationFactsRelationsCount;
  private CounterMap<String> organizationFactsRelationsCount;
  private CounterMap<String> creativeFactsRelationsCount;

  private Set<String> personArticlesWithRelationsCount;
  private Set<String> locationArticlesWithRelationsCount;
  private Set<String> organizationArticlesWithRelationsCount;
  private Set<String> creativeArticlesWithRelationsCount;
  
  private int factsTotal;
  private int factsInSpecificArticles;

  private static DBPedia db;
  private static Pattern entity = Pattern.compile(MarkupParser.WIKID_REGEX);

  /**
   * 
   */
  public FactsStats(){
    personFactsRelationsCount = new CounterMap<String>();
    locationFactsRelationsCount = new CounterMap<String>();
    organizationFactsRelationsCount = new CounterMap<String>();
    creativeFactsRelationsCount = new CounterMap<String>();
    personArticlesWithRelationsCount = new HashSet<String>();
    locationArticlesWithRelationsCount = new HashSet<String>();
    organizationArticlesWithRelationsCount = new HashSet<String>();
    creativeArticlesWithRelationsCount = new HashSet<String>();
    factsTotal = 0;
    factsInSpecificArticles = 0;
  }

  /**
   * 
   * @param url
   * @return
   */
  private static String getTitle(String url){
    return url.replace("https://" + Configuration.getLanguageCode() + ".wikipedia.org/wiki/", "");
  }

  /**
   * 
   */
  private void calculateStats() {
    try {
      BufferedReader reader = Compressed.getBufferedReaderForCompressedFile(
          Configuration.getProvenanceFile(Configuration.getModelCode()));
      String line;
      int cont = 0;
      while ((line = reader.readLine()) != null) {
        cont++;
        if (cont > Integer.MAX_VALUE)
          break;
        factsTotal++;
        try {
          String[] fields = line.split("\t");
          String wikid = fields[0];

          String section = fields[1];
          String sec = "#Elsewhere";
          if (section.equals("#Abstract"))
            sec = "#Abstract";

          String relation = fields[2];
          String kindsubject = fields[3];
          Matcher m1 = entity.matcher(kindsubject);
          m1.find();
          String subtype = m1.group(1);
          String kindobject = fields[5];
          Matcher m2 = entity.matcher(kindobject);
          m2.find();
          String objtype = m2.group(1);
          String typedPhrase = fields[7];
          // String sentence = fields[8];
          if (subtype.contains("PE-") || objtype.contains("PE-")){
            //System.out.println(db.getTypeCategory(getTitle(wikid)) + "\t" + getTitle(wikid) + "\t" + section + "\t" + relation + "\t" + m1.group(1) + "\t" + m2.group(1));
            String category = db.getTypeCategory(getTitle(wikid));

            String ab_rel = relation + "/" + sec;
            if (category.equals("Person")){
              factsInSpecificArticles++;
              personFactsRelationsCount.add(relation);
              personArticlesWithRelationsCount.add(getTitle(wikid));
              if (sec.equals("#Abstract"))
                personFactsRelationsCount.add(ab_rel);
            }if (category.equals("Place")){
              factsInSpecificArticles++;
              locationFactsRelationsCount.add(relation);
              locationArticlesWithRelationsCount.add(getTitle(wikid));
              if (sec.equals("#Abstract"))
                locationFactsRelationsCount.add(ab_rel);
            }if (category.equals("Organisation")){
              factsInSpecificArticles++;
              organizationFactsRelationsCount.add(relation);
              organizationArticlesWithRelationsCount.add(getTitle(wikid));
              if (sec.equals("#Abstract"))
                organizationFactsRelationsCount.add(ab_rel);
            }if (category.equals("Work")){
              factsInSpecificArticles++;
              creativeFactsRelationsCount.add(relation);
              creativeArticlesWithRelationsCount.add(getTitle(wikid));
              if (sec.equals("#Abstract"))
                creativeFactsRelationsCount.add(ab_rel);
            }
          }
        } catch (Exception e) {
          continue;
        }
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param map
   * @return
   */
  private static CounterMap<String> getCountsinAbstract(CounterMap<String> map){
    CounterMap<String> tmp_abstract = new CounterMap<String>();
    for (Map.Entry<String, Integer> entry : map.entrySet()){
      if (entry.getKey().contains("#Abstract")){
        String relation = entry.getKey().split("/")[0];
        tmp_abstract.add(relation, entry.getValue());
      }
    }
    return tmp_abstract;
  }


  /**
   * 
   */
  private void printStats() {
    CounterMap<String> personAbstractCount = getCountsinAbstract(personFactsRelationsCount);
    CounterMap<String> locationAbstractCount = getCountsinAbstract(locationFactsRelationsCount);
    CounterMap<String> organizationAbstractCount = getCountsinAbstract(organizationFactsRelationsCount);
    CounterMap<String> creativeAbstractCount = getCountsinAbstract(creativeFactsRelationsCount);
    
    System.out.println("total n. of facts:  " + factsTotal);
    double perc = ((double) factsInSpecificArticles / factsTotal) * 100;
    System.out.println("total in specific:  " + factsInSpecificArticles + "\t(" + String.format("%.2f", perc) + " %)");

    System.out.println("\nPERSON\t");
    int facts = CounterMap.calculateSumAvoidEntriesWith(personFactsRelationsCount, "#Abstract");
    int articles = personArticlesWithRelationsCount.size();
    double rate_facts_per_article = (double) facts/articles;
    System.out.println("FACTS: " + facts + "\t\t"
        + "in articles: " + articles + "\t\t"
        + "with: " + String.format("%.2f", rate_facts_per_article) + " per article.");    
    System.out.println("------");
    
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(personFactsRelationsCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (personAbstractCount.containsKey(entry.getKey()))
          inAbstract = personAbstractCount.get(entry.getKey());
        double percAB = ((double) inAbstract/entry.getValue())  * 100;
        System.out.printf("\t%-30s %-10s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract, String.format("%.2f", percAB) + " %");
      }
    }

    System.out.println("\nLOCATION\t");
    facts = CounterMap.calculateSumAvoidEntriesWith(locationFactsRelationsCount, "#Abstract");
    articles = locationArticlesWithRelationsCount.size();
    rate_facts_per_article = (double) facts/articles;
    System.out.println("FACTS: " + facts + "\t\t"
        + "in articles: " + articles + "\t\t"
        + "with: " + String.format("%.2f", rate_facts_per_article) + " per article.");    
    System.out.println("------");
    
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(locationFactsRelationsCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (locationAbstractCount.containsKey(entry.getKey()))
          inAbstract = locationAbstractCount.get(entry.getKey());
        double percAB = ((double) inAbstract/entry.getValue())  * 100;
        System.out.printf("\t%-30s %-10s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract, String.format("%.2f", percAB) + " %");
      }
    }

    System.out.println("\nORGANIZATION\t");
    facts = CounterMap.calculateSumAvoidEntriesWith(organizationFactsRelationsCount, "#Abstract");
    articles = organizationArticlesWithRelationsCount.size();
    rate_facts_per_article = (double) facts/articles;
    System.out.println("FACTS: " + facts + "\t\t"
        + "in articles: " + articles + "\t\t"
        + "with: " + String.format("%.2f", rate_facts_per_article) + " per article.");    
    System.out.println("------");
    
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(organizationFactsRelationsCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (organizationAbstractCount.containsKey(entry.getKey()))
          inAbstract = organizationAbstractCount.get(entry.getKey());
        double percAB = ((double) inAbstract/entry.getValue())  * 100;
        System.out.printf("\t%-30s %-10s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract, String.format("%.2f", percAB) + " %");
      }
    }

    System.out.println("\nWORK\t");
    facts = CounterMap.calculateSumAvoidEntriesWith(creativeFactsRelationsCount, "#Abstract");
    articles = creativeArticlesWithRelationsCount.size();
    rate_facts_per_article = (double) facts/articles;
    System.out.println("FACTS: " + facts + "\t\t"
        + "in articles: " + articles + "\t\t"
        + "with: " + String.format("%.2f", rate_facts_per_article) + " per article.");    
    System.out.println("------");
    
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(creativeFactsRelationsCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (creativeAbstractCount.containsKey(entry.getKey()))
          inAbstract = creativeAbstractCount.get(entry.getKey());
        double percAB = ((double) inAbstract/entry.getValue())  * 100;
        System.out.printf("\t%-30s %-10s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract, String.format("%.2f", percAB) + " %");
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
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);
      db = new DBPedia();

      System.out.println("\n\n------------------");
      System.out.println("Stats for language : " + Configuration.getLanguageCode());
      System.out.println("------------------");
      
      FactsStats fs = new FactsStats();
      fs.calculateStats();
      fs.printStats();
    }

  }


}
