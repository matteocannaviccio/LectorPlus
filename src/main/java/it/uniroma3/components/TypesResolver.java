package it.uniroma3.components;

import java.util.Optional;
import java.util.stream.Stream;

import it.uniroma3.configuration.Configuration;

public class TypesResolver {

    /**
     * 
     * @param possibleRedirect
     * @return
     */
    public static Stream<String> getTargetPage(String possibleRedirect, KeyValueIndex index){
	return index.retrieveValues(possibleRedirect).stream();
    }

    public static void main(String[] args){
	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");
	
	// indexing
	KeyValueIndex indexOriginal = new KeyValueIndex(Configuration.getTypesFileOriginal(), Configuration.getTypesIndexOriginal());
	KeyValueIndex indexAirpedia = new KeyValueIndex(Configuration.getTypesFileAirpedia(), Configuration.getTypesIndexAirpedia());
	String entity = "A.C._ChievoVerona";
	
	System.out.println("\nTypes in orginal mapping: ");
	getTargetPage(entity, indexOriginal).forEach(System.out::println);
	
	System.out.println("\nTypes in Airpedia: ");
	getTargetPage(entity, indexAirpedia).forEach(System.out::println);

    }
}
