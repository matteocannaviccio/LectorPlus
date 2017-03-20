package it.uniroma3.tools;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.uniroma3.configuration.Configuration;
/**
 * 
 * @author matteo
 *
 */
public class TypesResolver {

    private KeyValueIndex indexOriginal;
    private KeyValueIndex indexAirpedia;

    /**
     * 
     */
    public TypesResolver(){
	System.out.print("Creating TYPES resolver ...");
	long start_time = System.currentTimeMillis();
	this.indexOriginal = new KeyValueIndex(Configuration.getTypesOriginalFile(), Configuration.getTypesIndex());
	long end_time = System.currentTimeMillis();
	System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	System.out.print("Creating AIRPEDIA resolver ...");
	start_time = System.currentTimeMillis();
	this.indexAirpedia = new KeyValueIndex(Configuration.getAirpediaFile(), Configuration.getAirpediaIndex());
	end_time = System.currentTimeMillis();
	System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
    }
    
    /**
     * @return the indexOriginal
     */
    public KeyValueIndex getIndexOriginal() {
        return indexOriginal;
    }

    /**
     * @return the indexAirpedia
     */
    public KeyValueIndex getIndexAirpedia() {
        return indexAirpedia;
    }
    
    /**
     * 
     * @param wikid
     * @return
     */
    private List<String> getTypes(String wikid, KeyValueIndex index){
	return index.retrieveValues(wikid).stream().collect(Collectors.toList());
    }

    /**
     * 
     * @param wikid
     * @return
     */
    public List<String> assignTypes(String wikid){
	List<String> types = getTypes(wikid, indexOriginal);
	types.addAll(getTypes(wikid, indexAirpedia));
	return types;
    }

    public static void main(String[] args){
	Configuration.init("/Users/matteo/Work/Repository/java/lectorplus/config.properties");
	
	TypesResolver t = new TypesResolver();
	
	String entity = "Hard_Headed_Woman";

	System.out.println("\nTypes in orginal mapping: ");
	t.getTypes(entity, t.getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	t.getTypes(entity, t.getIndexAirpedia()).forEach(System.out::println);

    }
}
