package it.uniroma3.components;

import java.util.List;
import java.util.stream.Collectors;

import it.uniroma3.configuration.Configuration;

public class TypesResolver {
    
    // indexing
    private static KeyValueIndex indexOriginal = new KeyValueIndex(Configuration.getTypesFileOriginal(), Configuration.getTypesIndexOriginal());
    private static KeyValueIndex indexAirpedia = new KeyValueIndex(Configuration.getTypesFileAirpedia(), Configuration.getTypesIndexAirpedia());

    /**
     * 
     * @param wikid
     * @return
     */
    private static List<String> getTypes(String wikid, KeyValueIndex index){
	return index.retrieveValues(wikid).stream().collect(Collectors.toList());
    }

    public static List<String> assignTypes(String wikid){
	List<String> types = getTypes(wikid, indexOriginal);
	types.addAll(getTypes(wikid, indexAirpedia));
	return types;
    }


    public static void main(String[] args){
	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");

	String entity = "Vicki_Goldberg";

	System.out.println("\nTypes in orginal mapping: ");
	getTypes(entity, indexOriginal).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	getTypes(entity, indexAirpedia).forEach(System.out::println);

    }
}
