package it.uniroma3.extractor.kg.tgpatterns;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.util.Pair;

public class ExperimentSchemaCreator {
    
    /**
     * This method extracts Wikipedia Id (i.e. wikid) from the annotated entities.
     * 
     * @param entity
     * @return
     */
    private static String getWikipediaName(String entity){
	String dbpediaEntity = null;
	Pattern ENTITY = Pattern.compile("<[A-Z-]+<([^>]*?)>>");
	Matcher m = ENTITY.matcher(entity);
	if(m.find()){
	    dbpediaEntity = m.group(1);
	}
	return dbpediaEntity;
    }

    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));


	/****************/

	Map<String, Pair<TGPattern, TGPattern>> relation2tgpattern = new HashMap<String, Pair<TGPattern, TGPattern>>();
	Map<String, Pair<Integer, Integer>> relation2counts = new HashMap<String, Pair<Integer, Integer>>();

	BufferedReader br = new BufferedReader(new FileReader(new File(Configuration.getIndexableDBPediaFile())));
	String line;
	int cont = 0;
	while((line = br.readLine()) != null){
	    cont++;
	    if (cont % 100000 == 0)
		System.out.println("First iteration:\t" + cont);

	    try{
		String subject = line.split("\t")[0].split("###")[0];
		String object = line.split("\t")[0].split("###")[1];
		String relation = line.split("\t")[1];


		TGPattern subjectTGP = Lector.getKg().getTGPattern(ExperimentSchemaCreator.getWikipediaName(subject));
		TGPattern objectTGP = Lector.getKg().getTGPattern(ExperimentSchemaCreator.getWikipediaName(object));

		if (subjectTGP != null && objectTGP != null){
		    if(!relation2counts.containsKey(relation)){
			relation2counts.put(relation, Pair.make(0, 0));
		    }
		    relation2counts.put(relation, Pair.make(relation2counts.get(relation).key + 1,relation2counts.get(relation).value));
		    if(!relation2tgpattern.containsKey(relation)){
			relation2tgpattern.put(relation, Pair.make(subjectTGP, objectTGP));
		    }else{
			TGPattern combinedSubject = relation2tgpattern.get(relation).key.combine(subjectTGP);
			TGPattern combinedObject = relation2tgpattern.get(relation).value.combine(objectTGP);
			relation2tgpattern.put(relation, Pair.make(combinedSubject, combinedObject));
		    }
		}else{
		    if(!relation2counts.containsKey(relation)){
			relation2counts.put(relation, Pair.make(0, 0));
		    }
		    relation2counts.put(relation, Pair.make(relation2counts.get(relation).key, relation2counts.get(relation).value + 1));
		}
	    }catch(Exception e){
		System.out.println("Excp happens, skit it!");
		continue;
	    }
	}

	for(Map.Entry<String, Pair<TGPattern, TGPattern>> entry : relation2tgpattern.entrySet()){
	    Pair<TGPattern, TGPattern> normPair = Pair.make(entry.getValue().key.getMainPath(0.5), entry.getValue().value.getMainPath(0.5));
	    relation2tgpattern.put(entry.getKey(), normPair);
	}

	for(Map.Entry<String, Pair<TGPattern, TGPattern>> entry : relation2tgpattern.entrySet()){
	    System.out.println("Relation: \t" + entry.getKey());
	    System.out.println("Positive count: \t" + relation2counts.get(entry.getKey()).key);
	    System.out.println("Negative count: \t" + relation2counts.get(entry.getKey()).value);
	    System.out.println("Subject: \t" + entry.getValue().key.getInstances() + "\t" + entry.getValue().key);
	    System.out.println("Object: \t" + entry.getValue().value.getInstances() + "\t" + entry.getValue().value);
	    System.out.println("*************************************");
	}

	br.close();
    }

}
