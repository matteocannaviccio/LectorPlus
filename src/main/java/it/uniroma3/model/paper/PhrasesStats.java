package it.uniroma3.model.paper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.Compressed;

public class PhrasesStats {

  private static CounterMap<String> personFactsPhrasesCount = new CounterMap<String>();
  private static CounterMap<String> locationFactsPhrasesCount = new CounterMap<String>();
  private static CounterMap<String> organizationFactsPhrasesCount = new CounterMap<String>();
  private static CounterMap<String> creativeFactsPhrasesCount = new CounterMap<String>();
  private static DBPedia db;

  private static Pattern entity = Pattern.compile(MarkupParser.WIKID_REGEX);

  /**
   * 
   * @param url
   * @return
   */
  private static String getTitle(String url){
    return url.replace("https://en.wikipedia.org/wiki/", "");
  }

  /**
   * 
   */
  private static void calculateStats() {
    try {
      BufferedReader reader = Compressed.getBufferedReaderForCompressedFile(
          Configuration.getProvenanceFile(Configuration.getModelCode()));
      String line;
      while ((line = reader.readLine()) != null) {
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
          if (subtype.contains("PE-")){
            //System.out.println(db.getTypeCategory(getTitle(wikid)) + "\t" + getTitle(wikid) + "\t" + section + "\t" + relation + "\t" + m1.group(1) + "\t" + m2.group(1));
            String category = db.getTypeCategory(getTitle(wikid));

            String ab_tp = typedPhrase + "/" + sec;
            if (category.equals("Person")){
              personFactsPhrasesCount.add(typedPhrase);
              if (sec.equals("#Abstract"))
                personFactsPhrasesCount.add(ab_tp);
            }if (category.equals("Place")){
              locationFactsPhrasesCount.add(typedPhrase);
              if (sec.equals("#Abstract"))
                locationFactsPhrasesCount.add(ab_tp);
            }if (category.equals("Organisation")){
              organizationFactsPhrasesCount.add(typedPhrase);
              if (sec.equals("#Abstract"))
                organizationFactsPhrasesCount.add(ab_tp);
            }if (category.equals("Work")){
              creativeFactsPhrasesCount.add(typedPhrase);
              if (sec.equals("#Abstract"))
                creativeFactsPhrasesCount.add(ab_tp);
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
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
    Configuration.updateParameter("language", "en");
    db = new DBPedia();
    PhrasesStats.calculateStats();

    CounterMap<String> personAbstractCount = getCountsinAbstract(personFactsPhrasesCount);
    CounterMap<String> locationAbstractCount = getCountsinAbstract(locationFactsPhrasesCount);
    CounterMap<String> organizationAbstractCount = getCountsinAbstract(organizationFactsPhrasesCount);
    CounterMap<String> creativeAbstractCount = getCountsinAbstract(creativeFactsPhrasesCount);

    System.out.println("\nPERSON");
    System.out.println("------");
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(personFactsPhrasesCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (personAbstractCount.containsKey(entry.getKey()))
          inAbstract = personAbstractCount.get(entry.getKey());
        System.out.printf("\t%-50s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract);
      }
    }

    System.out.println("\nPLACES");
    System.out.println("------");
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(locationFactsPhrasesCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (locationAbstractCount.containsKey(entry.getKey()))
          inAbstract = locationAbstractCount.get(entry.getKey());
        System.out.printf("\t%-50s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract);
      }
    }

    System.out.println("\nORGANISATION");
    System.out.println("------");
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(organizationFactsPhrasesCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (organizationAbstractCount.containsKey(entry.getKey()))
          inAbstract = organizationAbstractCount.get(entry.getKey());
        System.out.printf("\t%-50s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract);
      }
    }

    System.out.println("\nWORK");
    System.out.println("------");
    for (Map.Entry<String, Integer> entry : Ranking.getRanking(creativeFactsPhrasesCount).entrySet()){
      if (!entry.getKey().contains("#Abstract")){
        int inAbstract = 0;
        if (creativeAbstractCount.containsKey(entry.getKey()))
          inAbstract = creativeAbstractCount.get(entry.getKey());
        System.out.printf("\t%-50s %-10s %s\n", entry.getKey(), entry.getValue(), inAbstract);
      }
    }

  }

}
