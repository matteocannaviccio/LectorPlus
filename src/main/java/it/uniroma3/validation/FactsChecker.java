package it.uniroma3.validation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.TSVReader;
/**
 * 
 * @author matteo
 *
 */
public class FactsChecker {

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	final int SIZE_SAMPLE = 5000;

	Configuration.init(args);
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	    Configuration.updateParameter("language", lang);

	    System.out.println("---------");
	    System.out.println("Language: " + lang);
	    System.out.println("---------");

	    // read all the provenance files
	    Map<String, List<String>> facts_content = new HashMap<String, List<String>>();
	    Map<String, Integer> facts_sizes = new HashMap<String, Integer>();
	    File model_folder = new File(Configuration.getOutputFolder());
	    for (File facts_folder : model_folder.listFiles()){
		if (facts_folder.getName().equals(".DS_Store"))
		    continue;
		File provenance = new File(facts_folder.getAbsolutePath()+ "/" + lang + "_provenance.bz2");
		List<String> content =  TSVReader.getLines2Set(provenance.getAbsolutePath());
		facts_content.put(facts_folder.getName(), content);
		facts_sizes.put(facts_folder.getName(), content.size());
	    }
	    System.out.println();

	    // order and pick the three sets
	    Iterator<Entry<String, Integer>> iterator = Ranking.getRanking(facts_sizes).entrySet().iterator();
	    List<String> externalSet = facts_content.get(iterator.next().getKey());
	    List<String> mediumSet = facts_content.get(iterator.next().getKey());
	    List<String> smallSet = facts_content.get(iterator.next().getKey());
	    externalSet.removeAll(mediumSet);
	    mediumSet.removeAll(smallSet);

	    //System.out.println(externalSet.size() + "\t" + externalSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
	    //System.out.println(mediumSet.size() + "\t" + mediumSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));
	    //System.out.println(smallSet.size() + "\t" + smallSet.stream().map(s -> s.split("\t")[7]).collect(Collectors.toList()));

	    /*
	     * pick the size of each random samples from them
	     * based on the whole size of the section
	     */
	    int total_size = externalSet.size() + mediumSet.size() + smallSet.size();
	    double external_part = (double) externalSet.size()/total_size;
	    double medium_part = (double) mediumSet.size()/total_size;
	    double small_part = (double) smallSet.size()/total_size;
	    int fraction_external = (int) Math.floor(SIZE_SAMPLE * external_part);
	    int fraction_medium = (int) Math.floor(SIZE_SAMPLE * medium_part);
	    int fraction_small = (int) Math.floor(SIZE_SAMPLE * small_part);
	    int total = fraction_external + fraction_medium + fraction_small;
	    while ((SIZE_SAMPLE - total) > 0){
		fraction_small +=1;
		total = fraction_external + fraction_medium + fraction_small;
	    }

	    System.out.println(fraction_external);
	    System.out.println(fraction_medium);
	    System.out.println(fraction_small);

	    List<String> sample_external = getSampleFromList(externalSet, fraction_external);
	    List<String> sample_medium = getSampleFromList(mediumSet, fraction_medium);
	    List<String> sample_small = getSampleFromList(smallSet, fraction_small);

	    File folder_external = new File(getValidationFolder("external"));
	    printFactsFile(folder_external, sample_external);
	    printRelationsFile(folder_external, sample_external);
	    printQuestionsFile(folder_external, sample_external);

	    File folder_medium = new File(getValidationFolder("medium"));
	    printFactsFile(folder_medium, sample_medium);
	    printRelationsFile(folder_medium, sample_medium);
	    printQuestionsFile(folder_medium, sample_medium);

	    File folder_strict = new File(getValidationFolder("strict"));
	    printFactsFile(folder_strict, sample_small);
	    printRelationsFile(folder_strict, sample_small);
	    printQuestionsFile(folder_strict, sample_small);

	    System.out.println(total);

	}
    }

    /**
     * 
     * @param folder
     */
    private static void printFactsFile(File folder, List<String> facts){
	try {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/facts.tsv"));
	    for (String fact : facts){
		writer.write(fact);
		writer.write("\n");
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param folder
     */
    private static void printQuestionsFile(File folder, List<String> facts){
	try {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/questions.tsv"));
	    for (String fact : facts){
		String relation = fact.split("\t")[2];
		String subject = fact.split("\t")[4];
		String object = fact.split("\t")[6];
		String sentence = fact.split("\t")[8];
		String question;
		if (relation.contains("(-1)"))
		    if (relation.equals("isPartOf(-1)"))
			question = "is " + subject + " part of " + object + "?";
		    else
			question = "is " + subject + " the/a " + relation.replaceAll("\\(-1\\)", "") + " of " + object + "?";
		else
		    if (relation.equals("isPartOf"))
			question = "is " + object + " part of " + subject + "?";
		    else
			question = "is " + object + " the/a " + relation.replaceAll("\\(-1\\)", "") + " of " + subject + "?";
		writer.write(relation + "\t" + subject + "\t" + object + "\t" + question + "\t" + sentence);
		writer.write("\n");
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param folder
     */
    private static void printRelationsFile(File folder, List<String> facts){
	try {
	    BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/relations.tsv"));
	    CounterMap<String> relation = new CounterMap<String>();
	    for (String fact : facts){
		relation.add(fact.split("\t")[2]);
	    }
	    for (String rel : Ranking.getRanking(relation).keySet()){
		writer.write(rel + "\t" + relation.get(rel));
		writer.write("\n");
	    }
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    private static String getValidationFolder(String folderName){
	String folderPath = Configuration.getDataFolder() + "/" + "validation" + "/" + Configuration.getLanguageCode() + "/" + folderName ;
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }


    /**
     * 
     * @param input
     * @param size
     * @return
     */
    public static List<String> getSampleFromList(List<String> input, int size){
	Collections.shuffle(input);
	return input.subList(0, size);
    }



}
