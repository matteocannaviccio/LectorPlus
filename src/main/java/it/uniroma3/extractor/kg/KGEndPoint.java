package it.uniroma3.extractor.kg;

import java.util.Set;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class KGEndPoint {
    
    private RedirectResolver redirectResolver;
    private TypesResolver typesResolver;
    private RelationsResolver relationResolver;
    
    /**
     * 
     */
    public KGEndPoint(){
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
     * @param args
     */
    
    public static void main(String[] args){
	Configuration.init(args);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));

	KGEndPoint t = new KGEndPoint();

	String entity = "Sergei_Zubov";

	System.out.println("\nTypes in orginal mapping: ");
	t.getTypesResolver().getTGpattern(entity, t.getTypesResolver().getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	t.getTypesResolver().getTGpattern(entity, t.getTypesResolver().getIndexAirpedia()).forEach(System.out::println);
	
	System.out.println("\nTypes in DBTax: ");
	t.getTypesResolver().getTGpattern(entity, t.getTypesResolver().getIndexDBTax()).forEach(System.out::println);
	
	System.out.println("\nTypes in LHD: ");
	t.getTypesResolver().getTGpattern(entity, t.getTypesResolver().getIndexLHD()).forEach(System.out::println);
	
	System.out.println("\nTypes in SDTyped: ");
	t.getTypesResolver().getTGpattern(entity, t.getTypesResolver().getIndexSDTyped()).forEach(System.out::println);
    }
        

}
