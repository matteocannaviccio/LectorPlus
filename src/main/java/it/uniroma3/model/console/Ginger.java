package it.uniroma3.model.console;

import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.ModelNaiveBayes;

public class Ginger {


    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");

	/*
	System.out.println("\t-> Init OnlyPositive Model ...");
	ModelNaiveBayes modelOnlyPositive = (ModelNaiveBayes) Model.getNewModel(new DBModel(Configuration.getDBModel()), "model_triples", 1, 0, ModelType.NaiveBayes, false);

	System.out.println("\t-> Init WithNone Model ...");
	ModelNaiveBayes modelWithNone = (ModelNaiveBayes) Model.getNewModel(new DBModel(Configuration.getDBModel()), "model_triples", 1, 100, ModelType.NaiveBayes, false);

	for (String typedPhrase : modelWithNone.getTypedPhrasesLabeled().keySet()){
	    String subject_type = typedPhrase.split("\t")[0];
	    String phrase_placeholder = typedPhrase.split("\t")[1];
	    String object_type = typedPhrase.split("\t")[2];

	    Pair<String, Double> predictionOnlyPositive = modelOnlyPositive.predict(subject_type, phrase_placeholder, object_type);
	    Pair<String, Double> predictionWithNone = modelWithNone.predict(subject_type, phrase_placeholder, object_type);
	    

	    if ((predictionOnlyPositive.value >= 0.85) && (predictionWithNone.key.equals("NONE")) && (predictionWithNone.value >= 0.85)){
		System.out.println(typedPhrase.replace("\t", " ") 
			+ "\t" + modelOnlyPositive.getTypedPhrases2relations().get(typedPhrase).get(predictionOnlyPositive.key)
			+ "\t" + modelWithNone.getTypedPhrases2relations().get(typedPhrase).get("NONE")
			+ "\t" + predictionOnlyPositive
			+ "\t" + predictionWithNone);
	    }
	}
	*/
	
	ModelNaiveBayes model = (ModelNaiveBayes) Model.getNewModel(new DBModel(Configuration.getDBModel()), "model_triples", 1, 25, ModelType.NaiveBayes, 0.4);
	for (String typedPhrase : model.getTypedPhrasesLabeled().keySet()){
	    String subject_type = typedPhrase.split("\t")[0];
	    String phrase_placeholder = typedPhrase.split("\t")[1];
	    String object_type = typedPhrase.split("\t")[2];
	    Pair<String, Double> prediction = model.predict(subject_type, phrase_placeholder, object_type);
	
	   // System.out.println(typedPhrase.replace("\t", " ") + "\t" + prediction.key + "\t" + prediction.value);
	}
	
    }
}
