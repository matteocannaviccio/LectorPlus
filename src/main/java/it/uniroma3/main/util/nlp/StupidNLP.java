package it.uniroma3.main.util.nlp;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import it.uniroma3.main.pipeline.articleparser.MarkupParser;

public class StupidNLP {

  /**
   * 
   * @param sentence
   * @return
   */
  public static List<String> splitInSentence(String sentence) {
    // save a temmp map for the entities
    Map<String, String> tmpEntitiesMapping = new HashMap<String, String>();

    // replace them
    Pattern ENTITIES = Pattern.compile(MarkupParser.WIKID_REGEX);
    Matcher m = ENTITIES.matcher(sentence);
    StringBuffer generalText = new StringBuffer();
    int i = 0;
    while (m.find()) {
      i++;
      String key = "@WIKILINK" + i + "@";
      tmpEntitiesMapping.put(key, m.group(0));
      m.appendReplacement(generalText, Matcher.quoteReplacement(key));
    }
    m.appendTail(generalText);
    sentence = generalText.toString();

    // split in sentences
    List<String> parts = split(sentence);

    // get the entities back
    List<String> splittedSentences = new ArrayList<String>(parts.size());
    Pattern PLACEHOLDERS = Pattern.compile("@WIKILINK[0-9]+@");
    for (String setn : parts) {
      StringBuffer cleanSent = new StringBuffer();
      m = PLACEHOLDERS.matcher(setn);
      while (m.find()) {
        m.appendReplacement(cleanSent,
            Matcher.quoteReplacement(tmpEntitiesMapping.get(m.group(0))));
      }
      m.appendTail(cleanSent);
      splittedSentences.add(cleanSent.toString());
    }
    return splittedSentences;

  }

  /**
   * 
   * @param document
   * @return
   */
  private static List<String> split(String document) {
    List<String> sentenceList = new ArrayList<String>();
    BreakIterator bi = BreakIterator.getSentenceInstance(Locale.US);
    bi.setText(document);
    int start = bi.first();
    int end = bi.next();
    int tempStart = start;
    while (end != BreakIterator.DONE) {
      String sentence = document.substring(start, end);
      if (!hasAbbreviation(sentence)) {
        sentence = document.substring(tempStart, end);
        tempStart = end;
        sentenceList.add(sentence);
      }
      start = end;
      end = bi.next();
    }
    return sentenceList;
  }

  /**
   * 
   * @param sentence
   * @return
   */
  private static boolean hasAbbreviation(String sentence) {
    if (sentence == null || sentence.isEmpty())
      return false;
    if (Pattern.matches("\\sDr.\\s||\\sProf.\\s||\\sMr.\\s||\\sMrs.\\s||\\sMs.\\s||"
        + "\\sJr.\\s||\\sPh.D.\\s||\\sSr.\\s||\\sfeat.\\s||\\sInc.\\s", sentence))
      return true;
    return false;
  }


  public static void main(String[] args) {
    String sentence =
        "Weitere kommerzielle Erfolge hatte sie mit den Filmen <<SE-ORG><Mr._&_Mrs._Smith_(2005)><''Mr. & Mrs. Smith''>> (2005), <<SE-ORG><Wanted_(2008)><''Wanted''>> (2008), <<SE-ORG><Salt_(Film)><''Salt''>> (2010) und <<SE-ORG><Maleficent_–_Die_dunkle_Fee><Maleficent – Die dunkle Fee>> (2014). ";
    List<String> parts = splitInSentence(sentence);
    int cont = 0;
    for (String setn : parts) {
      cont++;
      System.out.println(cont + ")" + setn);
    }
  }

}
