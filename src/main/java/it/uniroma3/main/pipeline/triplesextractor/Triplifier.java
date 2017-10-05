package it.uniroma3.main.pipeline.triplesextractor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiArticle;
import it.uniroma3.main.bean.WikiMVL;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;
import it.uniroma3.main.pipeline.triplesextractor.placeholders.PlaceholderFilter;
import it.uniroma3.main.util.Pair;

/**
 * This module extracts triples from the articles and write them in DB.
 * 
 * Before to extract the triple we pre-process the sentence and perform some text filtering such as:
 * - applying place-holders - removing parenthesis - removing triples where the object is followed
 * by 's - removing triples with phrases that end with "that"
 * 
 * @author matteo
 *
 */
public class Triplifier {
  private Queue<Pair<WikiTriple, String>> labeled_triples;
  private Queue<WikiTriple> unlabeled_triples;
  private Queue<WikiTriple> other_triples;
  private Queue<WikiMVL> mvlists;
  private PlaceholderFilter placeholderFilter;
  private Queue<String[]> nationalities;

  private AtomicInteger conjuctionCount;
  private AtomicInteger possessiveCount;
  private AtomicInteger largeSentencesCount;
  private AtomicInteger specialCharCount;

  private AtomicInteger allEntities;
  private AtomicInteger allPE;
  private AtomicInteger allSE;

  private AtomicInteger allTriples;
  private AtomicInteger fromWhichLabeled;
  private AtomicInteger fromWhichLabeledExpanded;
  private AtomicInteger fromWhichUnlabeled;

  private Map<String, LongAdder> entitiesCount;
  private Map<String, LongAdder> placeholdersCount;

  /**
   * 
   */
  public Triplifier() {
    Lector.getDbmodel(true);
    labeled_triples = new ConcurrentLinkedQueue<Pair<WikiTriple, String>>();
    unlabeled_triples = new ConcurrentLinkedQueue<WikiTriple>();
    other_triples = new ConcurrentLinkedQueue<WikiTriple>();
    nationalities = new ConcurrentLinkedQueue<String[]>();
    mvlists = new ConcurrentLinkedQueue<WikiMVL>();
    placeholderFilter = PlaceholderFilter.getPlaceholderFilter();

    largeSentencesCount = new AtomicInteger();
    possessiveCount = new AtomicInteger();
    conjuctionCount = new AtomicInteger();
    specialCharCount = new AtomicInteger();
    placeholdersCount = new ConcurrentHashMap<>();

    allEntities = new AtomicInteger();
    allPE = new AtomicInteger();
    allSE = new AtomicInteger();
    entitiesCount = new ConcurrentHashMap<>();

    allTriples = new AtomicInteger();
    fromWhichLabeled = new AtomicInteger();
    fromWhichLabeledExpanded = new AtomicInteger();
    fromWhichUnlabeled = new AtomicInteger();
  }

  /**
   * Given the article, iterate all the sentences and extract the triples. We eliminate the content
   * inside parenthesis and then we dispatch each triple in the right db using its type as a label.
   * 
   * @param article
   */
  public void extractTriples(WikiArticle article) {
    for (Map.Entry<String, List<String>> sentenceCollection : article.getSentences().entrySet()) {
      String section = sentenceCollection.getKey();
      for (String sentence : sentenceCollection.getValue()) {
        sentence = Lector.getTextParser().removeParenthesis(sentence);
        sentence =
            replaceMultiValuedList(sentence, sentenceCollection.getKey(), article.getWikid());
        for (WikiTriple t : createTriples(article, sentence, section)) {
          processTriple(t);
        }
      }
    }
    extractNationality(article);
  }

  /**
   * 
   * @param article
   */
  private void extractNationality(WikiArticle article) {
    if (article.getNationality() != null) {
      String[] nat = new String[4];
      nat[0] = article.getWikid();
      nat[1] = article.getFirstSentence();
      nat[2] = Lector.getDBPedia().getType(article.getWikid());
      nat[3] = article.getNationality();
      nationalities.add(nat);
    }
  }

  /**
   * Dispatch the triple in the DB, with the right label on it.
   * 
   * @param t
   */
  public void processTriple(WikiTriple t) {
    switch (t.getType()) {

      // it is a joinable triple only if both the subject
      // and object are wiki entities with a type
      case JOINABLE:
        Set<String> labels = t.getLabels();
        if (!labels.isEmpty()) {
          fromWhichLabeled.incrementAndGet();
          for (String relation : t.getLabels()) {
            fromWhichLabeledExpanded.incrementAndGet();
            labeled_triples.add(Pair.make(t, relation));
          }
        } else {
          fromWhichUnlabeled.incrementAndGet();
          unlabeled_triples.add(t);
        }
        break;
      case MVL:
      case JOINABLE_NOTYPE_BOTH:
      case JOINABLE_NOTYPE_SBJ:
      case JOINABLE_NOTYPE_OBJ:
      case DROP:
        other_triples.add(t);
        break;

    }
  }

  /**
   * Extract a list of triples (using consecutive entities) from the given sentence.
   * 
   * @param sentence
   * @return
   */
  public List<WikiTriple> createTriples(WikiArticle article, String sentence, String section) {
    List<WikiTriple> triples = new ArrayList<WikiTriple>();

    // find entities
    Pattern ENTITIES = Pattern.compile(MarkupParser.WIKID_REGEX);
    Matcher m = ENTITIES.matcher(sentence);

    // find placeholders
    Pattern PLACEHOLDERS = Pattern.compile("#[A-Z]+?#");

    // initial condition
    boolean foundSubject = false;
    boolean foundObject = false;

    // entities
    String method;

    // triple
    String pre = "";
    String subject = null;
    String object = null;
    String post = "";
    String phrase = "";

    // delimiters
    int subjectStartPos = 0;
    int subjectEndPos = 0;
    int objectStartPos = 0;
    int objectEndPos = 0;

    sentence = sentence.trim().replace("\t", " ").replaceAll("\n", " ");

    while (m.find()) {
      allEntities.incrementAndGet();
      method = m.group(1);
      if (method.contains("PE"))
        allPE.incrementAndGet();
      else
        allSE.incrementAndGet();
      entitiesCount.computeIfAbsent(method, k -> new LongAdder()).increment();

      if (!foundSubject) {
        foundSubject = true;
        subject = m.group(0);
        subjectStartPos = m.start(0);
        subjectEndPos = m.end(0);

      } else if (!foundObject) {
        object = m.group(0);
        objectStartPos = m.start(0);
        objectEndPos = m.end(0);

        pre = getWindow(
            replaceEntities(
                sentence.substring(Math.max(subjectStartPos - 200, 0), subjectStartPos).trim()),
            3, "pre");
        post = getWindow(
            replaceEntities(sentence
                .substring(objectEndPos, Math.min(sentence.length(), objectEndPos + 200)).trim()),
            3, "post");
        phrase = sentence.substring(subjectEndPos, objectStartPos).trim().replace("\t", " ")
            .replaceAll("\n", " ");

        boolean acceptablePhrase = true;
        String phrase_placeholders = placeholderFilter.replace(phrase);
        if (phrase_placeholders.equals("SPECIALCHAR")) {
          specialCharCount.incrementAndGet();
          acceptablePhrase = false;
        }

        if (phrase_placeholders.equals("CONJUNCTION")) {
          conjuctionCount.incrementAndGet();
          acceptablePhrase = false;

        }

        // this is only for english
        if (post.startsWith("'s") && Configuration.getLanguageCode().equals("en")) {
          possessiveCount.incrementAndGet();
          acceptablePhrase = false;

        }

        if (phrase_placeholders.split(" ").length > 15) {
          largeSentencesCount.incrementAndGet();
          acceptablePhrase = false;

        }

        // if everything is correct, add the triple...
        if (!phrase_placeholders.equals("") && acceptablePhrase) {
          allTriples.incrementAndGet();
          Matcher m_pl = PLACEHOLDERS.matcher(phrase_placeholders);
          while (m_pl.find()) {
            String ph = m_pl.group(0);
            placeholdersCount.computeIfAbsent(ph, k -> new LongAdder()).increment();
          }
          WikiTriple t = new WikiTriple(article.getWikid(), section, sentence, pre, subject, phrase,
              phrase_placeholders, object, post);
          triples.add(t);
        }

        // change subject now for the next triple
        subject = object;
        subjectStartPos = objectStartPos;
        subjectEndPos = objectEndPos;
      }
    }

    return triples;
  }


  /**
   * Replace all the MVL lists that are present in the sentence.
   * 
   * @param sentence
   * @return
   */
  private String replaceMultiValuedList(String sentence, String section, String wikid) {
    Matcher m = WikiMVL.getRegexMVL().matcher(sentence);
    while (m.find()) {
      WikiMVL mv = new WikiMVL(m.group(0), section, wikid);
      this.mvlists.add(mv);
      sentence = m.replaceAll(
          Matcher.quoteReplacement("<<MVL><" + mv.getCode() + "><" + mv.getSpanOfText() + ">>"));
    }
    return sentence;
  }

  /**
   * Extract the span of text before (pre) the subject entity or after (post) the object entity. We
   * hope that in the spanNCharacters there would be contained an N number of terms.
   * 
   * @param spanNCharacters window size
   * @param N number of terms
   * @param direction can be "pre" or "post"
   * @return
   */
  private String getWindow(String spanNCharacters, int N, String direction) {
    int elems = N; // number of elements to include in the window
    String[] tokens = spanNCharacters.split(" "); // split by space
    StringBuffer buff = new StringBuffer();
    String window = "";
    if (tokens.length > 0) {
      if (direction.equals("pre")) {
        // pre-window: start from the end
        for (int i = tokens.length - 1; i > 0 && elems > 0; i--) {
          buff.append(tokens[i] + " ");
          elems--;
        }
        window = reverseWords(buff.toString().trim());
      } else if (direction.equals("post")) {
        // post-window: start from the beginning
        for (int i = 0; i < tokens.length && elems > 0; i++) {
          buff.append(tokens[i] + " ");
          elems--;
        }
        window = buff.toString().trim();
      }
    }
    return window;
  }

  /**
   * 
   * @param s
   * @return
   */
  private String reverseWords(String s) {
    if (s == null || s.length() == 0)
      return "";
    // split to words by space
    String[] arr = s.split(" ");
    StringBuilder sb = new StringBuilder();
    for (int i = arr.length - 1; i >= 0; --i) {
      if (!arr[i].equals("")) {
        sb.append(arr[i]).append(" ");
      }
    }
    return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1);
  }

  /**
   * 
   * @param spanNCharacters
   * @return
   */
  private String replaceEntities(String spanNCharacters) {
    Pattern ENTITIES = Pattern.compile("<<.*?>>"); // find entities
    Matcher m = ENTITIES.matcher(spanNCharacters);
    StringBuffer buf = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(buf, "<EN>");
    }
    m.appendTail(buf);
    return buf.toString();
  }

  public void printStats() {
    System.out.println("\nStats of entities");
    System.out.println("----------------------------------");
    System.out.println("\t # all entities: " + withSuffix(allEntities.intValue()));
    System.out.println("\t # primary (PE): " + withSuffix(allPE.intValue()));
    System.out.println("\t # secondary (SE): " + withSuffix(allSE.intValue()));
    System.out.println("");
    for (Map.Entry<String, LongAdder> methods : this.entitiesCount.entrySet()) {
      System.out.println("\t" + methods.getKey() + " : " + methods.getValue().intValue());
    }

    System.out.println("\nStats of phrases and placeholders");
    System.out.println("----------------------------------");
    System.out.println("\t # all triples: " + withSuffix(allTriples.intValue()));
    System.out.println(
        "\t # used: " + withSuffix(fromWhichLabeled.intValue() + fromWhichUnlabeled.intValue()));
    System.out.println("\t # labeled triples: " + withSuffix(fromWhichLabeled.intValue()));
    System.out.println(
        "\t # (labeled expanded triples: " + withSuffix(fromWhichLabeledExpanded.intValue()) + ")");
    System.out.println("\t # unlabeled triples: " + withSuffix(fromWhichUnlabeled.intValue()));
    System.out.println();
    System.out.println(
        "\t # eliminated: " + withSuffix(conjuctionCount.intValue() + specialCharCount.intValue()
            + possessiveCount.intValue() + largeSentencesCount.intValue()));
    System.out.println("\t # triples with conjuctions (and, commas, etc.): "
        + withSuffix(conjuctionCount.intValue()));
    System.out.println("\t # triples with special chars ((, ), ;, :, etc.):  "
        + withSuffix(specialCharCount.intValue()));
    System.out.println("\t # triples with possessive after the object ('s): "
        + withSuffix(possessiveCount.intValue()));
    System.out.println(
        "\t # triples too large (>15 tokens): " + withSuffix(largeSentencesCount.intValue()));
    System.out.println("");
    for (Map.Entry<String, LongAdder> placeholders : this.placeholdersCount.entrySet()) {
      System.out.println("\t" + placeholders.getKey() + " : " + placeholders.getValue().intValue());
    }
  }


  /**
   * 
   */
  public void updateBlock() {
    Lector.getDbmodel(false).batchInsertLabeledTriple(this.labeled_triples);
    this.labeled_triples.clear();

    Lector.getDbmodel(false).batchInsertNationalityTriple(this.nationalities);
    this.nationalities.clear();

    Lector.getDbmodel(false).batchInsertUnlabeledTriple(this.unlabeled_triples);
    this.unlabeled_triples.clear();

    Lector.getDbmodel(false).batchInsertOtherTriple(this.other_triples);
    this.other_triples.clear();

    Lector.getDbmodel(false).batchInsertMVList(this.mvlists);
    this.mvlists.clear();

  }

  /**
   * 
   */
  public void printEveryThing() {
    System.out.println("***** Labeled Triples *****");
    for (Pair<WikiTriple, String> pair : this.labeled_triples) {
      System.out.println(pair.key.toString());
    }

    System.out.println("\n***** Unlabeled Triples *****");
    for (WikiTriple t : this.unlabeled_triples) {
      System.out.println(t.toString());
    }

    System.out.println("\n***** Other Triples *****");
    for (WikiTriple t : this.other_triples) {
      System.out.println(t.toString());
    }
  }

  public static String withSuffix(int count) {
    if (count < 1000)
      return "" + count;
    int exp = (int) (Math.log(count) / Math.log(1000));
    return String.format("%.1f %c", count / Math.pow(1000, exp), "KMB".charAt(exp - 1));
  }
}
