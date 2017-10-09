package it.uniroma3.model.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.Compressed;
import it.uniroma3.model.db.DBModel;

public class Paper {

  private DBModel db;

  /**
   * 
   * @param db
   */
  public Paper(DBModel db) {
    this.db = db;
    db.deriveModelTable();
  }

  /**
   * 
   */
  private void printLabeledInfo() {
    List<Pair<WikiTriple, String>> allLabeled =
        this.db.retrieveLabeledTriples(DBModel.labeled_table);

    // get all the entities by pattern method detection
    CounterMap<String> metEntDet = new CounterMap<String>();
    CounterMap<String> metPair = new CounterMap<String>();
    // get all the triples by relation
    CounterMap<String> docs2counts = new CounterMap<String>();
    CounterMap<String> relation2counts = new CounterMap<String>();
    // get all the triples by phrase
    CounterMap<String> phrase2counts = new CounterMap<String>();
    // get all the triples by typedphrase
    CounterMap<String> typedphrase2counts = new CounterMap<String>();
    // get all the triples by types
    CounterMap<String> types2counts = new CounterMap<String>();

    /**
     * 
     */
    for (Pair<WikiTriple, String> pair : allLabeled) {
      String entityDetectionSubject = extractMethodFromEntity(pair.key.getSubject());
      String entityDetectionObject = extractMethodFromEntity(pair.key.getObject());
      metEntDet.add(entityDetectionSubject);
      metEntDet.add(entityDetectionObject);
      metPair.add(entityDetectionSubject + "/" + entityDetectionObject);
      //
      docs2counts.add(pair.value);
      String relation = pair.value.replace("(-1)", "");
      relation2counts.add(relation);
      //
      phrase2counts.add(pair.key.getPhrasePlaceholders());
      //
      String typedphrase = pair.key.getSubjectType() + " " + pair.key.getPhrasePlaceholders() + " "
          + pair.key.getObjectType();
      typedphrase2counts.add(typedphrase);
      //
      types2counts.add(pair.key.getSubjectType() + "/" + pair.key.getObjectType());
    }



    System.out.printf("\t%-35s %s\n", "Total Labeled: ", allLabeled.size());
    System.out.printf("\t%-35s %s\n", "Total Labeled Phrases: ", phrase2counts.size());
    System.out.printf("\t%-35s %s\n", "Total Labeled Typed-Phrases: ", typedphrase2counts.size());
    System.out.printf("\t%-35s %s\n", "Total Labeled Relations (docs): ", docs2counts.size());
    System.out.printf("\t%-35s %s\n", "Total Labeled Relations (rels): ", relation2counts.size());
    System.out.println();
    System.out.printf("\t%-35s %s\n", "Top-10 Phrases labeled: ",
        Ranking.getTopKRanking(phrase2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Typed-Phrases labeled: ",
        Ranking.getTopKRanking(typedphrase2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Relations labeled: ",
        Ranking.getTopKRanking(relation2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Types labeled: ",
        Ranking.getTopKRanking(types2counts, 10));
    System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ",
        Ranking.getRanking(metEntDet));
    System.out.printf("\t%-35s %s\n", "Count Entity Detection Pairs: ",
        Ranking.getRanking(metPair));


  }

  /**
   * 
   */

  private void printUnlabeledInfo() {
    List<WikiTriple> allUnlabeled = this.db.retrieveUnlabeledTriples(DBModel.unlabeled_table);

    // get all the entities by pattern method detection
    CounterMap<String> metEntDet = new CounterMap<String>();
    CounterMap<String> metPair = new CounterMap<String>();

    // get all the triples by phrase
    CounterMap<String> phrase2counts = new CounterMap<String>();
    // get all the triples by typedphrase
    CounterMap<String> typedphrase2counts = new CounterMap<String>();
    // get all the triples by types
    CounterMap<String> types2counts = new CounterMap<String>();

    for (WikiTriple t : allUnlabeled) {
      String entityDetectionSubject = extractMethodFromEntity(t.getSubject());
      String entityDetectionObject = extractMethodFromEntity(t.getObject());
      metEntDet.add(entityDetectionSubject);
      metEntDet.add(entityDetectionObject);
      metPair.add(entityDetectionSubject + "/" + entityDetectionObject);
      //
      types2counts.add(t.getSubjectType() + "/" + t.getObjectType());
      //
      phrase2counts.add(t.getPhrasePlaceholders());
      //
      String typedphrase =
          t.getSubjectType() + " " + t.getPhrasePlaceholders() + " " + t.getObjectType();
      typedphrase2counts.add(typedphrase);
    }


    System.out.printf("\t%-35s %s\n", "Total Unlabeled: ", allUnlabeled.size());
    System.out.printf("\t%-35s %s\n", "Top-10 Phrases labeled: ",
        Ranking.getTopKRanking(phrase2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Typed-Phrases labeled: ",
        Ranking.getTopKRanking(typedphrase2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Types unlabeled: ",
        Ranking.getTopKRanking(types2counts, 10));
    System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ",
        Ranking.getRanking(metEntDet));
    System.out.printf("\t%-35s %s\n", "Count Entity Detection Pairs: ",
        Ranking.getRanking(metPair));
  }

  /**
   * 
   * @param entity
   * @return
   */
  private String extractMethodFromEntity(String entity) {
    Pattern ENMETDET = Pattern.compile(MarkupParser.WIKID_REGEX);
    Matcher m = ENMETDET.matcher(entity);
    String method = null;
    if (m.find()) {
      method = m.group(1);
    }
    //System.out.println("METHOD : " + entity + "\t" + method);
    return method;
  }

  /**
   * 
   * @param entity
   * @return
   */
  protected String extractNameFromEntity(String entity) {
    Pattern ENMETDET = Pattern.compile(MarkupParser.WIKID_REGEX);
    Matcher m = ENMETDET.matcher(entity);
    String method = null;
    if (m.find()) {
      method = m.group(2);
    }
    return method;
  }

  /**
   * 
   * @param entity
   * @return
   */
  private String extractPESEFromEntity(String entity) {
    Pattern ENMETDET = Pattern.compile(MarkupParser.WIKID_REGEX);
    Matcher m = ENMETDET.matcher(entity);
    String method = null;
    if (m.find()) {
      method = m.group(1).split("-")[0];
    }
    //System.out.println("PESE : " + entity + "\t" + method);
    return method;
  }

  /**
   * 
   */
  private void printExtractedFactsInfo() {
    CounterMap<String> wikidWithFacts = new CounterMap<String>();
    int abstractSection = 0;
    int otherSection = 0;
    CounterMap<String> relation2counts = new CounterMap<String>();
    CounterMap<String> metEntDet = new CounterMap<String>();
    CounterMap<String> kindPairs = new CounterMap<String>();

    // get all the triples by typedphrase
    CounterMap<String> typedphrase2counts = new CounterMap<String>();

    try {
      BufferedReader reader = Compressed.getBufferedReaderForCompressedFile(
          Configuration.getProvenanceFile(Configuration.getModelCode()));
      String line;
      while ((line = reader.readLine()) != null) {
        try {
          String[] fields = line.split("\t");
          String wikid = fields[0];
          String section = fields[1];
          String relation = fields[2];
          String kindsubject = extractPESEFromEntity(fields[3]);
          String kindobject = extractPESEFromEntity(fields[5]);
          String subjectEntityMethod = extractMethodFromEntity(fields[3]);
          String objectEntityMethod = extractMethodFromEntity(fields[5]);
          String typedPhrase = fields[7];
          // String sentence = fields[8];
          if (section.equals("#Abstract"))
            abstractSection += 1;
          else
            otherSection += 1;
          wikidWithFacts.add(wikid);
          relation2counts.add(relation);
          metEntDet.add(subjectEntityMethod);
          metEntDet.add(objectEntityMethod);
          kindPairs.add(kindsubject + "/" + kindobject);
          typedphrase2counts.add(typedPhrase);
        } catch (Exception e) {
          continue;
        }
      }
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.printf("\t%-35s %s\n", "-------", "");
    System.out.printf("\t%-35s %s\n", "Total Facts: ", abstractSection + otherSection);
    System.out.printf("\t%-35s %s\n", "in #Abstract: ", abstractSection);
    System.out.printf("\t%-35s %s\n", "elsewhere: ", otherSection);
    System.out.printf("\t%-35s %s\n", "Different relations: ", relation2counts.size());
    System.out.printf("\t%-35s %s\n", "Different typed-phrases: ", typedphrase2counts.size());
    System.out.printf("\t%-35s %s\n", "Top-10 typed-phrases: ",
        Ranking.getTopKRanking(typedphrase2counts, 10));
    System.out.printf("\t%-35s %s\n", "Top-10 Relations with Facts: ",
        Ranking.getTopKRanking(relation2counts, 10));
    System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ",
        Ranking.getRanking(metEntDet));
    System.out.printf("\t%-35s %s\n", "Kind of Pairs: ", kindPairs);
  }
  
  /**
   * 
   */
  public static void calculateStats(){
    for (String lang : Configuration.getLanguages()) {
      Configuration.updateParameter("language", lang);

      Paper paper = new Paper(Lector.getDbmodel(false));

      System.out.println("Stats for language : " + Configuration.getLanguageCode());
      System.out.println("------------------");
      paper.printLabeledInfo();
      System.out.println("----------");
      paper.printUnlabeledInfo();
      //paper.printExtractedFactsInfo();
      //System.out.println("----------");
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
    Paper.calculateStats();
  }
}
