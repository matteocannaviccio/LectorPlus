package it.uniroma3.main.util.inout.ntriples;

import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage.Lang;

/**
 * An hand-made converter used to convert triples between String and NTriples format. TODO: include
 * a real RDF parser.
 * 
 */
public class NTriplesConverter {
  private static String RESOURCE;
  private static String ONTOLOGY = "http://dbpedia.org/ontology/";


  /**
   * Use the right URIs depending on the language.
   * 
   */
  private static void setURIResource() {
    if (!Lector.getWikiLang().getLang().equals(Lang.en))
      RESOURCE = "http://" + Lector.getWikiLang().getLang().name() + ".dbpedia.org/resource/";
    else
      RESOURCE = "http://dbpedia.org/resource/";
  }

  /**
   * 
   * @param subject
   * @param predicate
   * @param object
   * @return
   */
  public static String convertString2NTriple(String subject, String predicate, String object) {
    setURIResource();
    StringBuffer ntriple_line = new StringBuffer();
    subject = RESOURCE + subject;
    predicate = ONTOLOGY + predicate;
    object = RESOURCE + object;
    ntriple_line.append("<" + subject + "> ");
    ntriple_line.append("<" + predicate + "> ");
    ntriple_line.append("<" + object + "> ");
    ntriple_line.append(".\n");
    return ntriple_line.toString();
  }


  /**
   * It reads triples that are used to describe facts between named entities from an .nt or .ttl
   * file, discarding every line that contains a literal as object.
   * 
   * @param line
   * @return
   */
  public static String convertRDFFacts2String(String line) {
    setURIResource();
    line = removeLastDot(line);
    StringBuffer converted_line = new StringBuffer();
    String[] text_line = line.split(">\\s(<|\")");
    String subject;
    String predicate;
    String object;

    if (text_line[0].contains(RESOURCE)) {
      subject = text_line[0].replaceAll(RESOURCE, "");
      subject = strip(subject, "<", ">");
    } else
      return null;

    if (text_line[1].contains(ONTOLOGY)) {
      predicate = text_line[1].replaceAll(ONTOLOGY, "");
      predicate = strip(predicate, "<", ">");

    } else
      return null;

    if (text_line[2].contains(RESOURCE)) {
      object = text_line[2].replaceAll(RESOURCE, "");
      object = strip(object, "<", ">");
    } else
      return null;

    converted_line.append(subject + "\t" + predicate + "\t" + object);
    return converted_line.toString();
  }

  /**
   * It reads triples that are used to describe facts between named entities from an .nt or .ttl
   * file, discarding every line that contains a literal as object.
   * 
   * @param line
   * @return
   */
  public static String convertRDFOntologyLines2String(String line) {
    line = removeLastDot(line);
    StringBuffer converted_line = new StringBuffer();
    String[] text_line = line.split(">\\s(<|\")");
    String subject;
    String object;

    if (text_line[0].contains(ONTOLOGY)) {
      subject = text_line[0].replaceAll(ONTOLOGY, "");
      subject = strip(subject, "<", ">");
    } else
      return null;

    if (!text_line[1].contains("rdf-schema#subClassOf"))
      return null;

    if (text_line[2].contains(ONTOLOGY)) {
      object = text_line[2].replaceAll(ONTOLOGY, "");
      object = strip(object, "<", ">");
    } else
      return null;

    converted_line.append(subject + "\t" + object);
    return converted_line.toString();
  }

  /**
   * 
   * @param line
   * @return
   */
  public static String convertRDFinstancetypes2String(String line) {
    setURIResource();
    StringBuffer converted_line = new StringBuffer();
    line = removeLastDot(line);
    String[] text_line = line.split(">\\s(<|\")");
    String subject;
    String object;

    if (text_line[0].contains(RESOURCE)) {
      subject = text_line[0].replaceAll(RESOURCE, "");
      subject = strip(subject, "<", ">");
    } else
      return null;

    if (text_line[2].contains(ONTOLOGY)) {
      object = text_line[2].replaceAll(ONTOLOGY, "");
      object = strip(object, "<", ">");
    } else
      return null;

    converted_line.append(subject + "\t" + object);
    return converted_line.toString();
  }

  /**
   * 
   * @param line
   * @return
   */
  public static String convertRDFontology2String(String line) {
    setURIResource();
    line = removeLastDot(line);
    String[] text_line = line.split(">\\s(<|\")");
    String subject;
    String object;

    if (text_line[1].contains("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
      StringBuffer converted_line = new StringBuffer();

      if (text_line[0].contains(ONTOLOGY)) {
        subject = text_line[0].replaceAll(RESOURCE, "");
        subject = strip(subject, "<", ">");
      } else
        return null;

      if (text_line[2].contains(ONTOLOGY)) {
        object = text_line[2].replaceAll(ONTOLOGY, "");
        object = strip(object, "<", ">");
      } else
        return null;

      converted_line.append(subject + "\t" + object);
      return converted_line.toString();
    } else
      return null;
  }

  /**
   * 
   * @param text
   * @param stripleft
   * @param stripright
   * @return
   */
  private static String strip(String text, String stripleft, String stripright) {
    return text.replaceAll("^" + stripleft, "").replaceAll(stripright + "$", "");
  }

  /**
   * 
   * @param text
   * @param stripleft
   * @param stripright
   * @return
   */
  private static String removeLastDot(String text) {
    return text.replace(" .", "");
  }

}
