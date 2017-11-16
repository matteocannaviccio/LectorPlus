package it.uniroma3.model.paper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.TSVReader;

/**
 * 
 * @author matteo
 *
 */
public class FactsChecker {

  private static Pattern WIKILINK = Pattern.compile(MarkupParser.WIKID_REGEX);

  private static Map<String, List<String>> facts_content = new HashMap<String, List<String>>();
  private static Map<String, Integer> facts_sizes = new HashMap<String, Integer>();

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    final int SIZE_SAMPLE = 1000;

    Configuration.init(args);
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_complete");
      Configuration.updateParameter("language", lang);

      Configuration.updateParameter("minF", "1");
      Configuration.updateParameter("majorityThreshold", "0.4");

      System.out.println("---------");
      System.out.println("Language: " + lang);
      System.out.println("---------");

      Map<String, Set<String>> facts_content = new HashMap<String, Set<String>>();
      Map<String, Integer> facts_sizes = new HashMap<String, Integer>();

      // read all the provenance files
      System.out.print("Reading external set ... ");
      Configuration.updateParameter("percUnl", "0");
      File provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/" + lang + "_provenance.bz2");
      Set<String> content = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);
      facts_content.put(Configuration.getModelCode(), content);
      facts_sizes.put(Configuration.getModelCode(), content.size());
      System.out.println("Done");

      System.out.print("Reading medium set ...");
      Configuration.updateParameter("percUnl", "25");
      provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/" + lang + "_provenance.bz2");
      content = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);
      facts_content.put(Configuration.getModelCode(), content);
      facts_sizes.put(Configuration.getModelCode(), content.size());
      System.out.println("Done");

      System.out.print("Reading internal set ... ");
      Configuration.updateParameter("percUnl", "100");
      provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/" + lang + "_provenance.bz2");
      content = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);
      facts_content.put(Configuration.getModelCode(), content);
      facts_sizes.put(Configuration.getModelCode(), content.size());
      System.out.println("Done");

      System.out.print("Ordering and adjusting ...");
      // order and pick the three sets
      Iterator<Entry<String, Integer>> iterator =
          Ranking.getRanking(facts_sizes).entrySet().iterator();
      Set<String> externalSet = facts_content.get(iterator.next().getKey());
      Set<String> mediumSet = facts_content.get(iterator.next().getKey());
      Set<String> smallSet = facts_content.get(iterator.next().getKey());
      externalSet.removeAll(mediumSet);
      mediumSet.removeAll(smallSet);
      System.out.println("Done");


      // System.out.println(externalSet.size() + "\t" + externalSet.stream().map(s ->
      // s.split("\t")[7]).collect(Collectors.toList()));
      // System.out.println(mediumSet.size() + "\t" + mediumSet.stream().map(s ->
      // s.split("\t")[7]).collect(Collectors.toList()));
      // System.out.println(smallSet.size() + "\t" + smallSet.stream().map(s ->
      // s.split("\t")[7]).collect(Collectors.toList()));

      /*
       * pick the size of each random samples from them based on the whole size of the section
       */
      int total_size = externalSet.size() + mediumSet.size() + smallSet.size();
      double external_part = (double) externalSet.size() / total_size;
      double medium_part = (double) mediumSet.size() / total_size;
      double small_part = (double) smallSet.size() / total_size;
      int fraction_external = (int) Math.floor(SIZE_SAMPLE * external_part);
      int fraction_medium = (int) Math.floor(SIZE_SAMPLE * medium_part);
      int fraction_small = (int) Math.floor(SIZE_SAMPLE * small_part);
      int total = fraction_external + fraction_medium + fraction_small;
      while ((SIZE_SAMPLE - total) > 0) {
        fraction_small += 1;
        total = fraction_external + fraction_medium + fraction_small;
      }

      System.out.println(fraction_external);
      System.out.println(fraction_medium);
      System.out.println(fraction_small);

      List<String> sample_external = getSampleFromSet(externalSet, fraction_external);
      List<String> sample_medium = getSampleFromSet(mediumSet, fraction_medium);
      List<String> sample_small = getSampleFromSet(smallSet, fraction_small);

      File folder_external = new File(getValidationFolder("external"));
      printFactsFile(folder_external, sample_external);
      printRelationsFile(folder_external, sample_external);
      printQuestionsFile(folder_external, sample_external);
      int count = printFinalFile(new File(getValidationFolder("general")), sample_external, "external", 1);

      File folder_medium = new File(getValidationFolder("medium"));
      printFactsFile(folder_medium, sample_medium);
      printRelationsFile(folder_medium, sample_medium);
      printQuestionsFile(folder_medium, sample_medium);
      count = printFinalFile(new File(getValidationFolder("general")), sample_medium, "model", count);

      File folder_strict = new File(getValidationFolder("strict"));
      printFactsFile(folder_strict, sample_small);
      printRelationsFile(folder_strict, sample_small);
      printQuestionsFile(folder_strict, sample_small);
      count = printFinalFile(new File(getValidationFolder("general")), sample_small, "internal", count);

      System.out.println(total);

    }
  }

  /**
   * 
   * @param folder
   */
  private static void printFactsFile(File folder, List<String> facts) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/facts.tsv"));
      for (String fact : facts) {
        writer.write(fact);
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param folder
   */
  private static void printQuestionsFile(File folder, List<String> facts) {

    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/questions.tsv"));
      for (String fact : facts) {
        String relation = fact.split("\t")[2];
        String subject = fact.split("\t")[4];
        String object = fact.split("\t")[6];
        String sentence = fact.split("\t")[8];
        String question = Questions.getQuestion(subject, relation, object);
        writer.write(relation + "\t" + subject + "\t" + object + "\t" + question + "\t" + sentence);
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param originalText
   * @param ann_sub
   * @param ann_obj
   * @return
   */
  public static String removeAllWikilinksBeyondThePair(String originalText, String ann_sub, String ann_obj) {
    Matcher m = WIKILINK.matcher(originalText);
    StringBuffer cleanText = new StringBuffer();
    while (m.find()) {
      String whole = m.group(0);
      String wikid = m.group(2);
      String renderedName = m.group(3);
      if (whole.equals(ann_sub) || whole.equals(ann_obj))
        m.appendReplacement(cleanText, Matcher.quoteReplacement(wikid));
      else
        m.appendReplacement(cleanText, Matcher.quoteReplacement(renderedName));
    }
    m.appendTail(cleanText);
    return cleanText.toString();
  }

  /**
   * 
   * @param originalText
   * @param ann_sub
   * @param ann_obj
   * @return
   */
  public static String getPart(String ann_sub, int group) {
    Matcher m = WIKILINK.matcher(ann_sub);
    m.find();
    return m.group(group);
  }

  /**
   * 
   * @param folder
   */
  private static int printFinalFile(File folder, List<String> facts, String set, int initialCount) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/validation_set.tsv", true));
      for (String fact : facts) {
        String code = String.format("%04d", initialCount);

        String url = fact.split("\t")[0];
        String article = url.split("/")[url.split("/").length-1];
        String section = fact.split("\t")[1];
        String relation = fact.split("\t")[2];

        String annotated_subject = fact.split("\t")[3];
        String ed_sub = getPart(annotated_subject, 1);
        String wikid_sub = getPart(annotated_subject, 2);
        String text_sub = getPart(annotated_subject, 3);

        String annotated_object = fact.split("\t")[5];
        String ed_obj = getPart(annotated_object, 1);
        String wikid_obj = getPart(annotated_object, 2);
        String text_obj = getPart(annotated_object, 3);

        String typed_phrase = fact.split("\t")[7];
        String sentence = removeAllWikilinksBeyondThePair(fact.split("\t")[8], annotated_subject, annotated_object);

        String question = Questions.getQuestion(wikid_sub, relation, wikid_obj);

        writer.write(
            code + "\t" + 
                set + "\t" + 
                url + "\t" + 
                article + "\t" + 
                section + "\t" + 
                relation + "\t" + 
                ed_sub + "\t" + 
                ed_obj + "\t" + 
                wikid_sub + "\t" + 
                wikid_obj + "\t" + 
                text_sub + "\t" + 
                text_obj + "\t" + 
                typed_phrase + "\t" +
                question + "\t" +
                sentence);
        writer.write("\n");
        initialCount++;
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return initialCount;
  }

  /**
   * 
   * @param folder
   */
  private static void printRelationsFile(File folder, List<String> facts) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/relations.tsv"));
      CounterMap<String> relation = new CounterMap<String>();
      for (String fact : facts) {
        relation.add(fact.split("\t")[2]);
      }
      for (String rel : Ranking.getRanking(relation).keySet()) {
        writer.write(rel + "\t" + relation.get(rel));
        writer.write("\n");
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param folderName
   * @return
   */
  private static String getValidationFolder(String folderName) {
    String folderPath = Configuration.getDataFolder() + "/" + "validation" + "/"
        + Configuration.getLanguageCode() + "/" + folderName;
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }


  /**
   * 
   * @param input
   * @param size
   * @return
   */
  public static List<String> getSampleFromSet(Set<String> input, int size) {
    List<String> list = new ArrayList<String>(input);
    Collections.shuffle(list);
    return list.subList(0, size);
  }



}
