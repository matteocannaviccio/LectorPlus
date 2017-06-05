package it.uniroma3.extractor.kg.resolver;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.kg.normalizer.DBPediaNormalizer;
import it.uniroma3.extractor.util.KeyValueIndex;
import it.uniroma3.extractor.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class RelationsResolver {

    private KeyValueIndex indexKG;
    private Map<String, String> inverse;

    /**
     * 
     */
    public RelationsResolver(){
	inverse = InverseDBPediaRelations.inverse();
	if (!new File(Configuration.getDBPediaIndex()).exists()){
	    System.out.print("\t-> Writing DBPedia index ... ");
	    long start_time = System.currentTimeMillis();
	    List<Pair<String, String>> dbpedia_dump = DBPediaNormalizer.normalizeMappingBasedDBPediaDump(Configuration.getDBPediaDumpFile());
	    this.indexKG = new KeyValueIndex(dbpedia_dump, Configuration.getDBPediaIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    this.indexKG = new KeyValueIndex(Configuration.getDBPediaIndex());
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
		relation = relation + "(-1)";
	    if(inverse.containsKey(relation))
		relation = inverse.get(relation);
	    relations.add(relation);
	}
	return relations;
    }

    /**
     * 
     * @param relation
     * @return
     */
    public void getInstances(String relation){
	for (String instance : indexKG.retrieveKeys(relation))
	    System.out.println(instance);
    }

    /**
     * 
     * @param subject
     * @param object
     */
    public void findRelations(String subject, String object){
	System.out.println("Relations in DBPedia between <" + subject + "> and <" + object + ">:");
	for (String relation : getRelations(subject, object))
	    System.out.println("\t" + relation);
    }


    /**
     * TEST MAIN.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	Lector.initAP(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	
	RelationsResolver res = new RelationsResolver();

	//String subject = "Barbara_Pierce_Bush";
	//String object = "George_W._Bush";
	//res.findRelations(object, subject);

	String relation = "nationality";
	res.getInstances(relation);

    }

}
