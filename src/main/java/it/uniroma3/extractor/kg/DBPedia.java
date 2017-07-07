package it.uniroma3.extractor.kg;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.kg.resolver.RedirectResolver;
import it.uniroma3.extractor.kg.resolver.RelationsResolver;
import it.uniroma3.extractor.kg.resolver.TypesResolver;
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
    public DBPedia(){
	this.redirectResolver = new RedirectResolver();
	this.typesResolver = new TypesResolver();
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
    public String getRedirect(String possibleRedirect){
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
     * This method queries the Types Resolver to find the type for the entities.
     * It assigns [none] in case of no type.
     * 
     * @return
     */
    public String getType(String entity) {
	return typesResolver.assignTypes(entity);
    }
    
    /**
     * 
     * @param uri
     * @return
     */
    public String getResourceName(String uri){
	return uri.replace(getResourceURI(), "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    public String getPredicateName(String uri){
	return uri.replace(getOntologyURI(), "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isDBPediaResource(String uri){
	return uri.contains(getResourceURI());
    }
    
    /**
     * 
     * @return
     */
    public String getResourceURI(){
	String namespace = null;
	if(Lector.getWikiLang().getLang().equals(Lang.en))
	    namespace = "http://dbpedia.org/resource/";
	else{
	    namespace = "http://" + Lector.getWikiLang().getLang() +".dbpedia.org/resource/";
	}
	return namespace;
    }
    
    /**
     * 
     * @return
     */
    public String getOntologyURI(){
	return "http://dbpedia.org/ontology/";
    }

    /**
     * 
     * @param uri
     * @return
     */
    public static boolean isIntermediateNode(String uri){
	return uri.contains("__");
    }

    /**
     * 
     * @param uri
     * @return
     */
    public boolean isInDBPediaOntology(String uri){
	return uri.contains(getOntologyURI());
    }
    
    /**
     * Returns true if the first argument is a parent type of the second.
     * 
     * @param possibleParent
     * @param possibleChild
     * @return
     */
    public boolean isChildOf(String possibleParent, String possibleChild){
	return typesResolver.isChildOf(possibleParent, possibleChild);
    }
    
    /**
     * 
     * @param args
     */
    
    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));

	DBPedia t = new DBPedia();

	String entity = "Sergei_Zubov";
	
	System.out.println(Lector.getDBPedia().isChildOf("Person", "[Writer]"));
	
	System.out.println("\nTypes in orginal mapping: ");
	t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in DBTax: ");
	t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexDBTax()).forEach(System.out::println);
	
	System.out.println("\nTypes in LHD: ");
	t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexLHD()).forEach(System.out::println);
	
	System.out.println("\nTypes in SDTyped: ");
	t.getTypesResolver().getOntPath(entity, t.getTypesResolver().getIndexSDTyped()).forEach(System.out::println);
    }
        

}
