package it.uniroma3.model.paper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.google.common.collect.Sets;
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
  //private static Map<String, Set<String>> facts_deltas = new HashMap<String, Set<String>>();
  private final static int SIZE_SAMPLE = 30;

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    Configuration.init(args);
    //Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data_small");
    Configuration.updateParameter("language", "en");
    Configuration.updateParameter("majorityThreshold", "0.0");

    List<Integer> percentages = new ArrayList<Integer>();
    percentages.add(100);
    percentages.add(95);
    percentages.add(90);
    percentages.add(85);
    percentages.add(80);
    percentages.add(75);
    percentages.add(70);
    percentages.add(65);
    percentages.add(60);
    percentages.add(55);
    percentages.add(50);
    percentages.add(45);
    percentages.add(40);
    percentages.add(35);
    percentages.add(30);
    percentages.add(25);
    percentages.add(20);
    percentages.add(15);
    percentages.add(10);
    percentages.add(5);
    percentages.add(0);


    // calculate the first bucket
    Configuration.updateParameter("percUnl", percentages.get(0).toString());
    System.out.print(Configuration.getPercUnl());
    //File provenance = new File("/Users/matteo/Desktop/extractor/en" + "/" + Configuration.getModelCode() + "/en_provenance.bz2");
    File provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/en_provenance.bz2");
    Set<String> small = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);
    System.out.print("\t" + small.size());
    List<String> sample = getSampleFromSet(small, SIZE_SAMPLE);
    int code = printFolderAndValidationSample(sample, 1);

    // calculate the bucket for each delta...
    percentages = percentages.subList(1, percentages.size());
    for (Integer k : percentages) {
      Configuration.updateParameter("percUnl", k.toString());
      System.out.print(Configuration.getPercUnl());
      //provenance = new File("/Users/matteo/Desktop/extractor/en" + "/" + Configuration.getModelCode() + "/en_provenance.bz2");
      provenance = new File(Configuration.getOutputFolder() + "/" + Configuration.getModelCode() + "/en_provenance.bz2");
      Set<String> content = TSVReader.getFirstKLines2Set(provenance.getAbsolutePath(), -1);

      Set<String> diff = Sets.difference(content, small);
      small = content;
      sample = getSampleFromSet(diff, SIZE_SAMPLE);
      System.out.print("\t" +diff.size());
      code = printFolderAndValidationSample(sample, code);
    }
    System.out.println("  Done");

    /*
     * pick the size of each random samples from them based on the whole size of the section
     */
    /*
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
     */
  }

  /**
   * 
   * @param sample
   */
  private static int printFolderAndValidationSample(List<String> sample, int start){
    System.out.println("\t" + sample.size() + "\t" + sample.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
    File folder = new File(getValidationFolder("" +Configuration.getPercUnl()));
    printFactsFile(folder, sample);
    printRelationsFile(folder, sample);
    printQuestionsFile(folder, sample);
    int count = printFinalFile(new File(getValidationFolder("general")), sample, "" +Configuration.getPercUnl(), start); 
    return count;
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
        m.appendReplacement(cleanText, "<mark>" + Matcher.quoteReplacement(wikid) + "</mark>");
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
    String folderPath = Configuration.getDataFolder() + "/" + "validation" + "/" + Configuration.getLanguageCode() + "/" + folderName;
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
