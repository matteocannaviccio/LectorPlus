package it.uniroma3.main.kg.resolver;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage.Lang;
import it.uniroma3.main.kg.normalizer.Normalizer;
import it.uniroma3.main.kg.ontology.OntPath;
import it.uniroma3.main.kg.ontology.Ontology;
import it.uniroma3.main.util.KeyValueIndex;
import it.uniroma3.main.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class TypesResolver {

    /*
     * for the other languages we keep the english types dictionary
     * as a reserve in case we can not find a type in the right
     * language dictionary.
     * TODO: we should add the inter-languages mapping here (...)
     */
    private static KeyValueIndex indexOriginal_ref;
    private static KeyValueIndex indexAirpedia_ref;

    private static KeyValueIndex indexOriginal;
    private static KeyValueIndex indexAirpedia;

    private static KeyValueIndex indexSDTyped;
    private static KeyValueIndex indexLHD;
    private static KeyValueIndex indexDBTax;

    private static Ontology ontology;

    /**
     * 
     */
    public TypesResolver(Lang language){
	// we need the ontology to find subTypes and solve situations regarind parent-child types
	ontology = new Ontology();

	/*
	 * we use different dictionary of types based on the language. ie. for English we have three more dictionaries.
	 * the "ref" index are always the english ones.
	 */
	switch(language){
	case en:
	    indexOriginal = getIndexOrCreate(Configuration.getTypesIndex(), Configuration.getSourceMainInstanceTypes());
	    indexAirpedia = getIndexOrCreate(Configuration.getAirpediaIndex(), Configuration.getSourceAirpediaTypes());
	    indexSDTyped = getIndexOrCreate(Configuration.getSDTypesIndex(), Configuration.getSourceSDTypedInstanceTypes());
	    indexLHD = getIndexOrCreate(Configuration.getLHDTypesIndex(), Configuration.getSourceLHDInstanceTypes());
	    indexDBTax = getIndexOrCreate(Configuration.getDBTaxTypesIndex(), Configuration.getSourceDBTaxInstanceTypes());
	    indexOriginal_ref = indexOriginal;
	    indexAirpedia_ref = indexAirpedia;
	    break;

	case de:
	    indexOriginal = getIndexOrCreate(Configuration.getTypesIndex(), Configuration.getSourceMainInstanceTypes());
	    indexAirpedia = getIndexOrCreate(Configuration.getAirpediaIndex(), Configuration.getSourceAirpediaTypes());
	    indexSDTyped = getIndexOrCreate(Configuration.getSDTypesIndex(), Configuration.getSourceSDTypedInstanceTypes());
	    indexLHD = getIndexOrCreate(Configuration.getLHDTypesIndex(), Configuration.getSourceLHDInstanceTypes());
	    indexOriginal_ref = getIndex(Configuration.getTypesIndex_Ref());
	    indexAirpedia_ref = getIndex(Configuration.getAirpediaIndex_Ref());
	    break;

	default:
	    indexOriginal = getIndexOrCreate(Configuration.getTypesIndex(), Configuration.getSourceMainInstanceTypes());
	    indexAirpedia = getIndexOrCreate(Configuration.getAirpediaIndex(), Configuration.getSourceAirpediaTypes());
	    indexOriginal_ref = getIndex(Configuration.getTypesIndex_Ref());
	    indexAirpedia_ref = getIndex(Configuration.getAirpediaIndex_Ref());
	    break;
	}
    }

    /**
     * Returns a KeyValueIndex given the path. If the exists does not exist returns null.
     * 
     * @param indexPath
     * @return
     */
    private KeyValueIndex getIndex(String indexPath){
	KeyValueIndex index = null;
	if (new File(indexPath).exists())
	    index = new KeyValueIndex(indexPath);
	return index; 
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
	    System.out.printf("\t\t%-20s %-20s %s", "--> Read & Index:", new File(indexPath).getName(), "");

	    long start_time = System.currentTimeMillis();
	    List<Pair<String, String>> keyvalues = Normalizer.normalizeInstanceTypesDataset(sourcePath);
	    index = new KeyValueIndex(keyvalues, indexPath);
	    long end_time = System.currentTimeMillis();
	    
	    System.out.printf("%-20s %s\n", "lines: " + index.getIndexedLines(), "indexed in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
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
     * Returns all the OntPaths that we can find for the entity in the given index.
     * 
     * @param entity
     * @param specificIndex
     * @return
     */
    public List<OntPath> getOntPath(String entity, KeyValueIndex index) {
	List<OntPath> patt = new LinkedList<OntPath>();
	for (String t : getTypes(entity, index)){
	    OntPath tgp = ontology.getOntPath(t);
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
    public OntPath getOntPath(String dbpediaEntity) {
	return ontology.getOntPath(assignTypes(dbpediaEntity));
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
     * We use the standard instance types dictionary. In case it does not contains 
     * a type, for english and germany we can use two further dictionaries.
     *   
     * @param wikid
     * @return
     */
    public String assignTypes(String wikid){
	String type = selectDeepest(getTypes(wikid, indexOriginal));

	if (type.equals("[none]"))
	    type = selectDeepest(getTypes(wikid, indexAirpedia));

	if (type.equals("[none]"))
	    if (Lector.getWikiLang().getLang().equals("en") || Lector.getWikiLang().getLang().equals("de")){
		if (type.equals("[none]"))
		    type = selectDeepest(getTypes(wikid, indexSDTyped));
		if (type.equals("[none]"))
		    type = selectDeepest(getTypes(wikid, indexLHD));
	    }

	// if there is not a type, try with the reference dictionary (english)
	if (type.equals("[none]") && indexOriginal_ref != null)
	    type = selectDeepest(getTypes(wikid, indexOriginal_ref));

	if (type.equals("[none]") && indexAirpedia_ref != null)
	    type = selectDeepest(getTypes(wikid, indexAirpedia_ref));

	return type;
    }



    /**
     * Returns true if the first argument is a parent type of the second.
     * 
     * @param possibleParent
     * @param possibleChild
     * @return
     */
    public boolean isChildOf(String possibleParent, String possibleChild){
	return ontology.isChildOf(possibleParent, possibleChild);
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(args);
	Configuration.updateParameter("language", "en");
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");

	TypesResolver t = new TypesResolver(Lang.valueOf(Configuration.getLanguageCode()));

	String entity = "Thomas_Hyde";
	System.out.println("USED --> " + t.assignTypes(entity));

	System.out.println("\nTypes in orginal mapping: ");
	t.getTypes(entity, t.getIndexOriginal()).forEach(System.out::println);

	System.out.println("\nTypes in Airpedia: ");
	t.getTypes(entity, t.getIndexAirpedia()).forEach(System.out::println);

    }

}
