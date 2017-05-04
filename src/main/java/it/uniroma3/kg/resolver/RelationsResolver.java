package it.uniroma3.kg.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.util.KeyValueIndex;
/**
 * 
 * @author matteo
 *
 */
public class RelationsResolver {

    private KeyValueIndex indexKG;

    public RelationsResolver(){
	if (!new File(Configuration.getKGIndex()).exists()){
	    System.out.print("Creating [relations] index ...");
	    long start_time = System.currentTimeMillis();
	    this.indexKG = new KeyValueIndex(Configuration.getIndexableDBPediaNormalizedRelationsFile(), Configuration.getKGIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    this.indexKG = new KeyValueIndex(Configuration.getKGIndex());
    }

    /**
     * 
     * @param wikidSubject
     * @param wikidObject
     * @return
     */
    public Set<String> getRelations(String wikidSubject, String wikidObject) {
	Set<String> relations = new HashSet<String>();
	for (String relation : indexKG.retrieveValues(wikidSubject + "###" + wikidObject))
	    relations.add(relation);
	for (String relation : indexKG.retrieveValues(wikidObject + "###" + wikidSubject)){
	    if(!relations.contains(relation))
		relations.add(relation + "(-1)");
	}
	return relations;
    }

    /**
     * 
     * @param relation
     * @return
     */
    private void getInstances(String relation){
	for (String instance : indexKG.retrieveKeys(relation))
	    System.out.println(instance);
    }

    /**
     * 
     * @param subject
     * @param object
     */
    private void findRelations(String subject, String object){
	System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
	for (String relation : getRelations(subject, object))
	    System.out.println("\t" + relation);
    }


    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	RelationsResolver res = new RelationsResolver();

	String subject = "Belfast_Trojans";
	String object = "Belfast";
	//res.findRelations(subject, object);

	String relation = "routeJunction";
	res.getInstances(relation);

    }

}
