package it.uniroma3.main.kg.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.uniroma3.config.Configuration;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.kg.normalizer.CleanDBPediaRelations;
import it.uniroma3.main.kg.normalizer.Normalizer;
import it.uniroma3.main.util.KeyValueIndex;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;
/**
 * 
 * @author matteo
 *
 */
public class RelationsResolver {

    private KeyValueIndex indexKG;
    private CleanDBPediaRelations cleaner;

    /**
     * 
     */
    public RelationsResolver(){
	this.cleaner = new CleanDBPediaRelations();
	this.indexKG = getIndexOrCreate(Configuration.getDBPediaIndex(), Configuration.getDBPediaDumpFile());
    }

    /**
     * Returns a KeyValueIndex given the path. If the exists does not exist it create it and then return.
     * 
     * @param indexPath
     * @param sourcePath
     * @return
     */
    private KeyValueIndex getIndexOrCreate(String indexPath, String sourcePath){
	KeyValueIndex index = null;
	if (!new File(indexPath).exists()){
	    System.out.printf("\t\t%-20s %-20s %s", "--> Read & Index:", "DBpedia", "");

	    long start_time = System.currentTimeMillis();
	    List<Pair<String, String>> dbpedia_dump = Normalizer.normalizeMappingBasedDBPediaDump(Configuration.getDBPediaDumpFile());
	    index = new KeyValueIndex(dbpedia_dump, indexPath);
	    long end_time = System.currentTimeMillis();

	    System.out.printf("%-20s %s\n", "lines: " + index.getIndexedLines(), "indexed in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
	}
	else // we already have the index
	    index = new KeyValueIndex(indexPath);

	return index;
    }

    /**
     * It queries the KG index looking for a pair of entities that match the input subject and object.
     * If none pairs match, it looks for the inverse order (adding -1 to the label of the relation).
     * 
     * @param wikidSubject
     * @param wikidObject
     * @return
     */
    public Set<String> getRelations(String wikidSubject, String wikidObject) {
	Set<String> relations = new HashSet<String>();
	for (String relation : indexKG.retrieveValues(wikidSubject + "###" + wikidObject)){
	    String candidate = cleaner.cleanRelation(relation);
	    if (!candidate.equals("notValid"))
		relations.add(candidate);
	}
	for (String relation : indexKG.retrieveValues(wikidObject + "###" + wikidSubject)){
	    String candidate = cleaner.cleanRelation(relation + "(-1)");
	    if (!relations.contains(relation) && !candidate.equals("notValid"))
		relations.add(candidate);
	}
	return relations;
    }

    /**
     * 
     * @param relation
     * @return
     */
    public void getInstances(String relation){
	int tot = 0;
	for (String instance : indexKG.retrieveKeys(relation))
	    if (tot < 200 ){
		System.out.printf("\t%-50s %s\n", instance.split("###")[0], instance.split("###")[1]);
		tot +=1;
	    }
    }

    /**
     * 
     * @param subject
     * @param object
     */
    void findRelations(String subject, String object){
	System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
	for (String relation : getRelations(subject, object))
	    System.out.println("\t" + cleaner.cleanRelation(relation));
    }

    /**
     * 
     */
    private void getAllRelations(){
	for (Map.Entry<String, Integer> relation : Ranking.getRanking(indexKG.matchAll()).entrySet()){
	    System.out.println(relation.getKey() + "\t" + relation.getValue());
	}
    }


    /**
     * TEST MAIN.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	Configuration.updateParameter("language", "en");
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");	

	RelationsResolver res = new RelationsResolver();

	String subject = "Michelle_Obama";
	String object = "Barack_Obama";

	res.findRelations(object, subject);

	//res.getAllRelations();
	String relation = "board";
	res.getInstances(relation);
	
	

    }

}
