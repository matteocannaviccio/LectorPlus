package it.uniroma3.extractor.kg;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.kg.normalizer.TGPattern;
import it.uniroma3.extractor.kg.normalizer.TypesNormalizer;
import it.uniroma3.extractor.util.KeyValueIndex;
import it.uniroma3.extractor.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class TypesResolver {

    private static KeyValueIndex indexOriginal;
    private static KeyValueIndex indexAirpedia;
    private static KeyValueIndex indexSDTyped;
    private static KeyValueIndex indexLHD;
    private static KeyValueIndex indexDBTax;

    private static Ontology ontology;

    /**
     * 
     */
    public TypesResolver(){
	// we need the ontology to find subTypes
	ontology = new Ontology();
	
	// we use different dictionary of types based on the language. ie. for English we have three more dictionaries.
	switch(Lector.getWikiLang().getLang()){
	case en:
	    indexOriginal = getIndexOrCreate(Configuration.getTypesIndex(), Configuration.getSourceMainInstanceTypes());
	    indexAirpedia = getIndexOrCreate(Configuration.getAirpediaIndex(), Configuration.getSourceAirpediaInstanceTypes());
	    indexSDTyped = getIndexOrCreate(Configuration.getSDTypesIndex(), Configuration.getSourceSDTypedInstanceTypes());
	    indexLHD = getIndexOrCreate(Configuration.getLHDTypesIndex(), Configuration.getSourceLHDInstanceTypes());
	    indexDBTax = getIndexOrCreate(Configuration.getDBTaxTypesIndex(), Configuration.getSourceDBTaxInstanceTypes());
	    
	default:
	    indexOriginal = getIndexOrCreate(Configuration.getTypesIndex(), Configuration.getSourceMainInstanceTypes());
	    indexAirpedia = getIndexOrCreate(Configuration.getAirpediaIndex(), Configuration.getSourceAirpediaInstanceTypes());
	}
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
	    System.out.print("Creating " + new File(indexPath).getName() + " index ...");
	    long start_time = System.currentTimeMillis();
	    
	    List<Pair<String, String>> keyvalues = TypesNormalizer.normalizeTypesDataset(sourcePath);
	    index = new KeyValueIndex(keyvalues, indexPath);
	    
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    index = new KeyValueIndex(indexPath);
	return index;
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
     * @return the indexSDTyped
     */
    public KeyValueIndex getIndexSDTyped() {
	return indexSDTyped;
    }

    /**
     * @return the indexLHD
     */
    public KeyValueIndex getIndexLHD() {
	return indexLHD;
    }

    /**
     * @return the indexDBTax
     */
    public KeyValueIndex getIndexDBTax() {
	return indexDBTax;
    }

    /**
     * Returns all the types that we can find for the entity in the given index.
     * 
     * @param wikid
     * @return
     */
    public List<String> getTypes(String wikid, KeyValueIndex index){
	List<String> types = index.retrieveValues(wikid).stream().collect(Collectors.toList());
	if (types.isEmpty())
	    types.add("none");
	return types;
    }

    /**
     * 
     * @param entity
     * @param specificIndex
     * @return
     */
    public List<TGPattern> getTGpattern(String entity, KeyValueIndex specificIndex) {
	List<TGPattern> patt = new LinkedList<TGPattern>();
	for (String t : getTypes(entity, specificIndex)){
	    TGPattern tgp = ontology.getTGPattern(t);
	    if (tgp != null)
		patt.add(tgp);
	}
	return patt;
    }

    /**
     * 
     * 
     * @param dbpediaEntity
     * @return
     */
    public TGPattern getTGpattern(String dbpediaEntity) {
	return ontology.getTGPattern(assignTypes(dbpediaEntity));
    }

    /**
     * 
     * @param types
     * @return
     */
    private String selectDeepest(List<String> types){
	String deepType = types.stream().max(Comparator.comparingInt(s -> ontology.depthNode(s))).get();
	return "[" + deepType + "]";
    }


    /**
     * Here we express the rules that are used to assign a type to an input entity.
     * Not all the dictionaries of types that we used have the same reliability!
     * 
     * @param wikid
     * @return
     */
    public String assignTypes(String wikid){
	String type = selectDeepest(getTypes(wikid, indexOriginal));
	if (Lector.getWikiLang().getLang().equals("en")){
	    if (type.equals("[none]"))
		type = selectDeepest(getTypes(wikid, indexSDTyped));
	}
	if (type.equals("[none]"))
	    type = selectDeepest(getTypes(wikid, indexAirpedia));

	if (Lector.getWikiLang().getLang().equals("en")){
	    if (type.equals("[none]"))
		type = selectDeepest(getTypes(wikid, indexLHD));
	}
	return type;
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	
	TypesResolver t = new TypesResolver();

	String entity = "Barack_Obama";
	System.out.println("USED --> " + t.assignTypes(entity));

	System.out.println("\nTypes in orginal mapping: ");
	t.getTypes(entity, t.getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	t.getTypes(entity, t.getIndexAirpedia()).forEach(System.out::println);

	System.out.println("\nTypes in DBTax: ");
	t.getTypes(entity, t.getIndexDBTax()).forEach(System.out::println);

	System.out.println("\nTypes in LHD: ");
	t.getTypes(entity, t.getIndexLHD()).forEach(System.out::println);

	System.out.println("\nTypes in SDTyped: ");
	t.getTypes(entity, t.getIndexSDTyped()).forEach(System.out::println);
    }

}
