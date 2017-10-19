package it.uniroma3.main.kg;

import java.util.Set;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage.Lang;
import it.uniroma3.main.kg.resolver.RedirectResolver;
import it.uniroma3.main.kg.resolver.RelationsResolver;
import it.uniroma3.main.kg.resolver.TypesResolver;

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
   * 
   * @param possibleRedirect
   * @return
   */
  public String getRedirect(String possibleRedirect) {
    return this.redirectResolver.resolveRedirect(possibleRedirect);
  }

  /**
   * @return the redirectResolver
   */
  public RedirectResolver getRedirectResolver() {
    return redirectResolver;
  }

  /**
   * @return the typesResolver
   */
  public TypesResolver getTypesResolver() {
    return typesResolver;
  }

  /**
   * @return the relationResolver
   */
  public RelationsResolver getRelationResolver() {
    return relationResolver;
  }

  /**
   * This method queries the Types Resolver to find the type for the entities. It assigns [none] in
   * case of no type.
   * 
   * @return
   */
  public String getType(String entity) {
    return typesResolver.assignTypes(entity);
  }

  /**
   * This method queries the Types Resolver to find the type for the entities. It assigns [none] in
   * case of no type.
   * 
   * @return
   */
  public String getTypeCategory(String entity) {
    String type = typesResolver.assignTypesSimple(entity);
    if (!type.equals("[none]")){
      String cat = "Person";
      if (this.isChildOf(cat, typesResolver.assignTypesSimple(entity))){
        return cat;
      }
      cat = "Place";
      if (this.isChildOf(cat, typesResolver.assignTypesSimple(entity))){
        return cat;
      }
      cat = "Organisation";
      if (this.isChildOf(cat, typesResolver.assignTypesSimple(entity))){
        return cat;
      }
      cat = "Work";
      if (this.isChildOf(cat, typesResolver.assignTypesSimple(entity))){
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

  public static void main(String[] args) {
    Configuration.init(new String[0]);
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
    Configuration.updateParameter("language", "en");

    DBPedia t = new DBPedia();

    String entity = "Stanford_University";

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


}
