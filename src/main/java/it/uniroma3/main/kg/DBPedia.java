package it.uniroma3.main.kg;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage.Lang;
import it.uniroma3.main.kg.resolver.RedirectResolver;
import it.uniroma3.main.kg.resolver.RelationsResolver;
import it.uniroma3.main.kg.resolver.TypesResolver;
import it.uniroma3.main.util.Pair;

/**
 * 
 * @author matteo
 *
 */
public class DBPedia {
  private RedirectResolver redirectResolver;
  private TypesResolver typesResolver;
  private RelationsResolver relationResolver;

  /**
   * 
   */
  public DBPedia() {
    this.redirectResolver = new RedirectResolver();
    this.typesResolver = new TypesResolver(Lang.valueOf(Configuration.getLanguageCode()));
    this.relationResolver = new RelationsResolver();
  }

  /**
   * This method queries the KG to find possible relations between the entities.
   * 
   * @return
   */
  public Set<String> getRelations(String entitySubject, String entityObject) {
    return relationResolver.getRelations(entitySubject, entityObject);
  }

  /**
   * Return some facts in DBpedia for the given relation.
   * 
   * @param relation
   * @param max
   * @return
   */
  public String getSomeFacts(String relation, int max) {
    return relationResolver.getInstances(relation, max);
  }
  
  /**
   * Return all the facts in DBpedia for the given relation.
   * It goes through the string method, orrible, but not extremely necessary.
   * 
   * @param relation
   * @return
   */
  public List<Pair<String, String>> getAllFacts(String relation) {
    List<Pair<String, String>> list = new LinkedList<Pair<String, String>>();
    String pairs = relationResolver.getInstances(relation, -1);
    for (String line : pairs.split("\n"))
      list.add(Pair.make(line.split("\t")[0], line.split("\t")[1]));
    return list;
  }
  
  /**
   * 
   * @param possibleRedirect
   * @return
   */
  public String getRedirect(String possibleRedirect) {
    return this.redirectResolver.resolveRedirect(possibleRedirect);
  }

  /**
   * This method queries the Types Resolver to find the type for the entities.
   * In this case, it uses all the indexes that we have for types. 
   * It assigns [none] in case of no type.
   * 
   * @return
   */
  public String getTypeDeep(String entity) {
    return typesResolver.assignTypesDeep(entity);
  }
  
  /**
   * This method queries the Types Resolver to find the type for the entities. 
   * In this case, it uses only one or two indexes for types. 
   * It assigns [none] in case of no type.
   * 
   * @return
   */
  public String getTypeSimple(String entity) {
    return typesResolver.assignTypesSimple(entity);
  }

  /**
   * This method queries the Types Resolver to find the type and extract its category.
   * 
   * @param entity
   * @return
   */
  public String getTypeCategory(String entity) {
    String type = getTypeSimple(entity);
    if (!type.equals("[none]")){
      String cat = "Person";
      if (this.isChildOf(cat, getTypeSimple(entity))){
        return cat;
      }
      cat = "Place";
      if (this.isChildOf(cat, getTypeSimple(entity))){
        return cat;
      }
      cat = "Organisation";
      if (this.isChildOf(cat, getTypeSimple(entity))){
        return cat;
      }
      cat = "Work";
      if (this.isChildOf(cat, getTypeSimple(entity))){
        return cat;
      }
    }
    return "-";

  }
  
  /**
   * This method queries the ontology and return the category (more coarse grained) of the type.
   * 
   * @param type
   * @return
   */
  public String matchTypeCategory(String type) {
    if (!type.equals("[none]")){
      String cat = "Person";
      if (this.isChildOf(cat, type)){
        return cat;
      }
      cat = "Place";
      if (this.isChildOf(cat, type)){
        return cat;
      }
      cat = "Organisation";
      if (this.isChildOf(cat, type)){
        return cat;
      }
      cat = "Work";
      if (this.isChildOf(cat, type)){
        return cat;
      }
    }
    return "-";

  }

  /**
   * 
   * @param uri
   * @return
   */
  public String getResourceName(String uri) {
    return uri.replace(getResourceURI(), "");
  }

  /**
   * 
   * @param uri
   * @return
   */
  public String getPredicateName(String uri) {
    return uri.replace(getOntologyURI(), "");
  }

  /**
   * 
   * @param uri
   * @return
   */
  public boolean isDBPediaResource(String uri) {
    return uri.contains(getResourceURI());
  }

  /**
   * 
   * @return
   */
  public String getResourceURI() {
    String namespace = null;
    if (Lector.getWikiLang().getLang().equals(Lang.en))
      namespace = "http://dbpedia.org/resource/";
    else {
      namespace = "http://" + Lector.getWikiLang().getLang() + ".dbpedia.org/resource/";
    }
    return namespace;
  }

  /**
   * 
   * @return
   */
  public String getOntologyURI() {
    return "http://dbpedia.org/ontology/";
  }

  /**
   * 
   * @param uri
   * @return
   */
  public static boolean isIntermediateNode(String uri) {
    return uri.contains("__");
  }

  /**
   * 
   * @param uri
   * @return
   */
  public boolean isInDBPediaOntology(String uri) {
    return uri.contains(getOntologyURI());
  }

  /**
   * Returns true if the first argument is a parent type of the second.
   * 
   * @param possibleParent
   * @param possibleChild
   * @return
   */
  public boolean isChildOf(String possibleParent, String possibleChild) {
    return typesResolver.isChildOf(possibleParent, possibleChild);
  }

  /**
   * 
   * @param args
   */

  /*
  public static void main(String[] args) {
    Configuration.init(new String[0]);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
    Configuration.updateParameter("language", "en");

    DBPedia t = new DBPedia();

    String entity = "Turkish_literature";

    //System.out.println(t.isChildOf("Person", "[Writer]"));

    System.out.println("\nTypes in orginal mapping: ");
    t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexOriginal())
    .forEach(System.out::println);

    System.out.println("\nTypes in DBTax: ");
    t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexDBTax())
    .forEach(System.out::println);

    System.out.println("\nTypes in LHD: ");
    t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexLHD())
    .forEach(System.out::println);

    System.out.println("\nTypes in SDTyped: ");
    t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexSDTyped())
    .forEach(System.out::println);
  }
  
  */


}
