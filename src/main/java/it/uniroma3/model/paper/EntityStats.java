package it.uniroma3.model.paper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.bean.WikiArticle;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.inout.JSONReader;
/**
 * 
 * @author matteo
 *
 */
public class EntityStats {

  private static Map<String, LongAdder> personPEmethodsCount = new ConcurrentHashMap<>();
  private static Map<String, LongAdder> locationPEmethodsCount = new ConcurrentHashMap<>();
  private static Map<String, LongAdder> organizationPEmethodsCount = new ConcurrentHashMap<>();
  private static Map<String, LongAdder> creativePEmethodsCount = new ConcurrentHashMap<>();

  private static Map<String, Example> personPEmethodsExamples = new ConcurrentHashMap<>();
  private static Map<String, Example> locationPEmethodsExamples = new ConcurrentHashMap<>();
  private static Map<String, Example> organizationPEmethodsExamples = new ConcurrentHashMap<>();
  private static Map<String, Example> creativePEmethodsExamples = new ConcurrentHashMap<>();

  private static Map<String, LongAdder> categoryPEmethodsCount = new ConcurrentHashMap<>();

  private static AtomicInteger allArticles = new AtomicInteger(0);
  private static AtomicInteger allCategorized = new AtomicInteger(0);

  private static DBPedia db;
  private static Pattern entity = Pattern.compile(MarkupParser.WIKID_REGEX);

  private static Map<String, Integer> total = new HashMap<String, Integer>();

  /**
   * 
   * @param primary
   * @param sentences
   * @return
   */
  public static void countPrimaryEntities(WikiArticle article){
    String primary = article.getTitle();
    Map<String, List<String>> sentences = article.getSentences();

    allArticles.incrementAndGet();
    String category = db.getTypeCategory(primary);
    categoryPEmethodsCount.computeIfAbsent(category, k -> new LongAdder()).increment();

    if (!category.equals("-")){
      allCategorized.incrementAndGet();
      for (Map.Entry<String, List<String>> sent : sentences.entrySet()){
        String sec = "#Elsewhere";
        if (sent.getKey().equals("#Abstract"))
          sec = "#Abstract";
        String text= StringUtils.join(sent.getValue(), " ");
        Matcher m = entity.matcher(text);
        String method;
        StringBuffer general = new StringBuffer(); 
        while (m.find()) {
          method = sec + "-" + m.group(1);
          String mentionedText = m.group(3);
          //System.out.println(category + "\t" +  primary + "\t" + method);
          //System.out.println(general);
          
          m.appendReplacement(general, Matcher.quoteReplacement(m.group(0)));

          if (category.equals("Person")){
            StringBuffer textModified = new StringBuffer(); 
            personPEmethodsCount.computeIfAbsent(method, k -> new LongAdder()).increment();
            int generalSize = general.length();
            textModified.append(general.toString().substring(0, generalSize-m.group(0).length()));
            textModified.append("<mark>" + Matcher.quoteReplacement(mentionedText) + "</mark>");
            m.appendTail(textModified);
            Example e = new Example(primary, article.getFirstSentence(), sent.getKey(), textModified.toString(), m.group(0), method, mentionedText);
            personPEmethodsExamples.put(m.group(0), e);
          }

          if (category.equals("Place")){
            StringBuffer textModified = new StringBuffer(); 
            locationPEmethodsCount.computeIfAbsent(method, k -> new LongAdder()).increment();
            int generalSize = general.length();
            textModified.append(general.toString().substring(0, generalSize-m.group(0).length()));
            textModified.append("<mark>" + Matcher.quoteReplacement(mentionedText) + "</mark>");
            m.appendTail(textModified);
            Example e = new Example(primary, article.getFirstSentence(), sent.getKey(), textModified.toString(), m.group(0), method, mentionedText);
            locationPEmethodsExamples.put(m.group(0), e);
          }

          if (category.equals("Organisation")){
            StringBuffer textModified = new StringBuffer(); 
            organizationPEmethodsCount.computeIfAbsent(method, k -> new LongAdder()).increment();
            int generalSize = general.toString().length();
            textModified.append(general.toString().substring(0, generalSize-m.group(0).length()));
            textModified.append("<mark>" + Matcher.quoteReplacement(mentionedText) + "</mark>");
            m.appendTail(textModified);
            Example e = new Example(primary, article.getFirstSentence(), sent.getKey(), textModified.toString(), m.group(0), method, mentionedText);
            organizationPEmethodsExamples.put(m.group(0), e);
          }

          if (category.equals("Work")){
            StringBuffer textModified = new StringBuffer(); 
            creativePEmethodsCount.computeIfAbsent(method, k -> new LongAdder()).increment();
            int generalSize = general.length();
            textModified.append(general.toString().substring(0, generalSize-m.group(0).length()));
            textModified.append("<mark>" + Matcher.quoteReplacement(mentionedText) + "</mark>");
            m.appendTail(textModified);
            Example e = new Example(primary, article.getFirstSentence(), sent.getKey(), textModified.toString(), m.group(0), method, mentionedText);
            creativePEmethodsExamples.put(m.group(0), e);
          }
          
        }
        
      }
    }
  }

  /**
   * 
   * @return
   */
  public static Pair<Integer, Integer> printAllCounts(String method, String category){
    int ab = 0;
    int ew = 0;

    if (category.equalsIgnoreCase("PERSON")){
      if (personPEmethodsCount.containsKey("#Abstract-" + method))
        ab = personPEmethodsCount.get("#Abstract-" + method).intValue();
      if (personPEmethodsCount.containsKey("#Elsewhere-" + method))
        ew = personPEmethodsCount.get("#Elsewhere-" + method).intValue();
    }

    if (category.equalsIgnoreCase("PLACE")){
      if (locationPEmethodsCount.containsKey("#Abstract-" + method))
        ab = locationPEmethodsCount.get("#Abstract-" + method).intValue();
      if (locationPEmethodsCount.containsKey("#Elsewhere-" + method))
        ew = locationPEmethodsCount.get("#Elsewhere-" + method).intValue();
    }

    if (category.equalsIgnoreCase("ORGANISATION")){
      if (organizationPEmethodsCount.containsKey("#Abstract-" + method))
        ab = organizationPEmethodsCount.get("#Abstract-" + method).intValue();
      if (organizationPEmethodsCount.containsKey("#Elsewhere-" + method))
        ew = organizationPEmethodsCount.get("#Elsewhere-" + method).intValue();
    }

    if (category.equalsIgnoreCase("WORK")){
      if (creativePEmethodsCount.containsKey("#Abstract-" + method))
        ab = creativePEmethodsCount.get("#Abstract-" + method).intValue();
      if (creativePEmethodsCount.containsKey("#Elsewhere-" + method))
        ew = creativePEmethodsCount.get("#Elsewhere-" + method).intValue();
    }
    return Pair.make(ew + ab, ab);
  } 

  /**
   * 
   * @param category
   * @param key
   * @return
   */
  private static double getPEPercent(String category, Integer key) {
    return  (double) key / total.get(category + "-PE");
  }

  /**
   * 
   * @param category
   * @param key
   * @return
   */
  private static double getSEPercent(String category, Integer key) {
    return  (double) key / total.get(category + "-SE");
  }


  /**
   * 
   * @param category
   * @param key
   * @return
   */
  private static double getPEAbstractPercent(String category, Integer key) {
    return  (double) key / total.get(category + "-PE-Abstract");
  }


  /**
   * 
   * @param category
   * @param key
   * @return
   */
  private static double getSEAbstractPercent(String category, Integer key) {
    return  (double) key / total.get(category + "-SE-Abstract");
  }


  /**
   * 
   */
  public static void calculateStats(){
    JSONReader reader = new JSONReader(Configuration.getAugmentedArticlesFile());
    List<WikiArticle> lines;
    int contChunks = 0;
    int totChunks = 5;
    while (!(lines = reader.nextChunk(1000)).isEmpty() && contChunks < totChunks) {
      contChunks++;
      lines.parallelStream()
      .forEach(s -> countPrimaryEntities(s));
    }
  }

  /**
   * 
   * @param map
   */
  private static int getTotPE(Map<String, LongAdder> map){
    int tot = 0;
    for (Map.Entry<String, LongAdder> entry : map.entrySet()){
      if (entry.getKey().contains("PE-"))
        tot += entry.getValue().intValue();
    }
    return tot;
  }

  /**
   * 
   * @param map
   */
  private static int getTotPEabstract(Map<String, LongAdder> map){
    int tot = 0;
    for (Map.Entry<String, LongAdder> entry : map.entrySet()){
      if (entry.getKey().contains("#Abstract-PE-"))
        tot += entry.getValue().intValue();
    }
    return tot;
  }

  /**
   * 
   * @param map
   */
  private static int getTotSEabstract(Map<String, LongAdder> map){
    int tot = 0;
    for (Map.Entry<String, LongAdder> entry : map.entrySet()){
      if (entry.getKey().contains("#Abstract-SE-"))
        tot += entry.getValue().intValue();
    }
    return tot;
  }

  /**
   * 
   * @param map
   */
  private static int getTotSE(Map<String, LongAdder> map){
    int tot = 0;
    for (Map.Entry<String, LongAdder> entry : map.entrySet()){
      if (entry.getKey().contains("SE-"))
        tot += entry.getValue().intValue();
    }
    return tot;
  }

  /**
   * 
   */
  private static void setTotal(){
    total.put("Person-PE", getTotPE(personPEmethodsCount));
    total.put("Person-PE-Abstract", getTotPEabstract(personPEmethodsCount));
    total.put("Person-SE", getTotSE(personPEmethodsCount));
    total.put("Person-SE-Abstract", getTotSEabstract(personPEmethodsCount));

    total.put("Place-PE", getTotPE(locationPEmethodsCount));
    total.put("Place-PE-Abstract", getTotPEabstract(locationPEmethodsCount));
    total.put("Place-SE", getTotSE(locationPEmethodsCount));
    total.put("Place-SE-Abstract", getTotSEabstract(locationPEmethodsCount));

    total.put("Organisation-PE", getTotPE(organizationPEmethodsCount));
    total.put("Organisation-PE-Abstract", getTotPEabstract(organizationPEmethodsCount));
    total.put("Organisation-SE", getTotSE(organizationPEmethodsCount));
    total.put("Organisation-SE-Abstract", getTotSEabstract(organizationPEmethodsCount));

    total.put("Work-PE", getTotPE(creativePEmethodsCount));
    total.put("Work-PE-Abstract", getTotPEabstract(creativePEmethodsCount));
    total.put("Work-SE", getTotSE(creativePEmethodsCount));
    total.put("Work-SE-Abstract", getTotSEabstract(creativePEmethodsCount));

  }



  /**
   * 
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    Configuration.init(args);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
    Configuration.updateParameter("language", "en");
    db = new DBPedia();
    EntityStats.calculateStats();

    setTotal();

    List<String> pe_methods = new ArrayList<String>(Arrays.asList(
        new String[]{"PE-TITLE", "PE-SUBTITLE", "PE-SEED", "PE-DBPS", "PE-ALIAS", "PE-PRON", "PE-DISAMB"}));

    List<String> se_methods = new ArrayList<String>(Arrays.asList(
        new String[]{"SE-ORG", "SE-AUG", "SE-DBPS"}));

    System.out.println("-------------");
    System.out.printf("%-20s %s\n", "ARTICLES", allArticles.get());
    System.out.printf("%-20s %s\n", "Categorized", allCategorized.get());

    System.out.println();
    for (String cat : new String[]{"Person", "Place", "Organisation", "Work"}){
      System.out.printf("%-20s %s\n", cat.toUpperCase(), categoryPEmethodsCount.get(cat));
      System.out.println("------------------");
      System.out.printf("\t%-20s %-20s %s\n", "PRIMARY", total.get(cat + "-PE"), total.get(cat + "-PE-Abstract"));
      for (String m : pe_methods){
        Pair<Integer, Integer> pair = printAllCounts(m, cat);
        double percentWhole = getPEPercent(cat, pair.key);
        double percentAbsdtract = getPEAbstractPercent(cat, pair.value );
        System.out.printf("\t%-20s%-20s %-20s %-20s %s\n", m, pair.key, pair.value, String.format("%.2f", percentWhole), String.format("%.2f", percentAbsdtract));
      }
      System.out.printf("\t%-20s %s\n", "---", "---");
      System.out.printf("\t%-20s %-20s %s\n", "SECONDARY", total.get(cat + "-SE"), total.get(cat + "-SE-Abstract"));
      for (String s : se_methods){
        Pair<Integer, Integer> pair = printAllCounts(s, cat);
        double percentWhole = getSEPercent(cat, pair.key);
        double percentAbsdtract = getSEAbstractPercent(cat, pair.value );
        System.out.printf("\t%-20s %-20s %-20s %-20s %s\n", s, pair.key , pair.value , String.format("%.2f", percentWhole), String.format("%.2f", percentAbsdtract));
      }
      System.out.println();
    }


    File folderHTMLFiles = new File( "/Users/matteo/Desktop/samples");
    folderHTMLFiles.mkdirs();
    int cont = 0;
    int max = 50;
    
    BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/PERSON.html")));
    BufferedWriter bw1b = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/PERSON.tsv")));
    CounterMap<String> methodscount = new CounterMap<String>();
    for (Map.Entry<String, Example> example : personPEmethodsExamples.entrySet()){
      String met = example.getValue().getMethod();
      methodscount.add(met);
      Example e = example.getValue();
      if ((!e.isAbstract() || !met.equals("SEED")) && e.isAugmented() && e.isPrimary() && (methodscount.get(met) <= 10)){
        bw1.write(e.toString("PERSON", ""+cont) + "\n");
        bw1b.write(cont + "\t" + e.getTitle() + "\t" + e.whereIsIt() + "\t" + e.entdetmet() + "\n");
        cont++;
      }
    }
    bw1.close();
    bw1b.close();
    
    BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/LOCATION.html")));
    BufferedWriter bw2b = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/LOCATION.tsv")));
    methodscount = new CounterMap<String>();
    for (Map.Entry<String, Example> example : locationPEmethodsExamples.entrySet()){
      String met = example.getValue().getMethod();
      methodscount.add(met);
      Example e = example.getValue();
      if ((!e.isAbstract() || !met.equals("SEED")) && e.isAugmented() && e.isPrimary() && (methodscount.get(met) <= 10)){
        bw2.write(e.toString("LOCATION", ""+cont) + "\n");
        bw2b.write(cont + "\t" + e.getTitle() + "\t" + e.whereIsIt() + "\t" + e.entdetmet() + "\n");
        cont++;
      }
    }
    bw2.close();
    bw2b.close();
    
    BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/ORGANIZATION.html")));
    BufferedWriter bw3b = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/ORGANIZATION.tsv")));
    methodscount = new CounterMap<String>();
    for (Map.Entry<String, Example> example : organizationPEmethodsExamples.entrySet()){
      String met = example.getValue().getMethod();
      methodscount.add(met);
      Example e = example.getValue();
      if ((!e.isAbstract() || !met.equals("SEED")) && e.isAugmented() && e.isPrimary() && (methodscount.get(met) <= 10)){
        bw3.write(e.toString("ORGANIZATION", ""+cont) + "\n");
        bw3b.write(cont + "\t" + e.getTitle() + "\t" + e.whereIsIt() + "\t" + e.entdetmet() + "\n");
        cont++;
      }
    }
    bw3.close();
    bw3b.close();
    
    BufferedWriter bw4 = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/CREATIVE.html")));
    BufferedWriter bw4b = new BufferedWriter(new FileWriter(new File(folderHTMLFiles.getAbsolutePath() + "/CREATIVE.tsv")));
    methodscount = new CounterMap<String>();
    for (Map.Entry<String, Example> example : creativePEmethodsExamples.entrySet()){
      String met = example.getValue().getMethod();
      methodscount.add(met);
      Example e = example.getValue();
      if ((!e.isAbstract() || !met.equals("SEED")) && e.isAugmented() && e.isPrimary() && (methodscount.get(met) <= 10)){
        bw4.write(e.toString("CREATIVE", ""+cont) + "\n");
        bw4b.write(cont + "\t" + e.getTitle() + "\t" + e.whereIsIt() + "\t" + e.entdetmet() + "\n");
        cont++;
      }
    }
    bw4.close();
    bw4b.close();
  }


}
