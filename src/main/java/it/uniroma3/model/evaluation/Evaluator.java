package it.uniroma3.model.evaluation;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.bean.WikiTriple.TType;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
import it.uniroma3.model.model.ModelBM25;
import it.uniroma3.model.model.ModelLS;
import it.uniroma3.model.model.ModelLSTextExt;
import it.uniroma3.model.model.ModelNB;
import it.uniroma3.model.model.ModelNB.ModelNBType;
/**
 * 
 * @author matteo
 *
 */
public class Evaluator {

    private DBCrossValidation dbcrossvaliadation;
    private Model model;
    private CounterMap<String> cases = new CounterMap<String>();

    /**
     * 
     * @param model
     * @param db_read
     */
    public Evaluator(String dbcrossvalidationName, DBModel dbmodel){
	this.dbcrossvaliadation = loadOrCreateCrossValidationDB(dbcrossvalidationName, dbmodel, 5);
    }

    /**
     * 
     * @param crossDBName
     * @param dbmodel
     * @param nParts
     * @return
     */
    private DBCrossValidation loadOrCreateCrossValidationDB(String crossDBName, DBModel dbmodel, int nParts){
	if (!new File(crossDBName).exists())
	    return new DBCrossValidation(crossDBName, dbmodel, nParts);
	else
	    return new DBCrossValidation(crossDBName);
    }

    /**
     * 
     * @param type
     * @param labeled_table
     * @param minFreq
     * @param topK
     * @param typePhrase
     * @return
     */
    private void setModel(ModelType type, String labeled_table, int minFreq, int topK, double cutoff, PhraseType typePhrase){
	switch(type){
	case BM25:
	    model = new ModelBM25(this.dbcrossvaliadation, labeled_table, minFreq, topK, typePhrase);
	    break;
	case NB:
	    model = new ModelNB(this.dbcrossvaliadation, labeled_table, minFreq, ModelNBType.CLASSIC);
	    break;
	case LectorScore:
	    model = new ModelLS(this.dbcrossvaliadation, labeled_table, minFreq, topK, typePhrase);
	    break;
	case TextExtChallenge:
	    model = new ModelLSTextExt(this.dbcrossvaliadation, labeled_table, minFreq, topK, cutoff, typePhrase);
	    break;
	}
    }

    /**
     * 
     * @param expectedRelation
     * @return
     */
    private boolean isPredictableRelation(String expectedRelation){
	return model.canPredict(expectedRelation);
    }

    /**
     * Process the triple to label.
     * It can not have the same entities as subject and object. 
     * Return a true value if we can extract a new facts, false otherwise.
     * 
     * @param t
     * @return
     */
    private Pair<String, Double> processRecord(WikiTriple t){
	return model.predictRelation(t);
    }

    /**
     * 
     * @param model
     */
    private Pair<Double, Double> runEvaluation(String table_name){
	int tp = 0;
	int fp = 0;
	int fntn = 0;

	String all = "SELECT * FROM " + table_name;

	try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(all)){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String phrase_original = rs.getString(2);
		    String phrase_placeholder = rs.getString(3);
		    String pre = rs.getString(4);
		    String post = rs.getString(5);
		    String subject = rs.getString(6);
		    String subject_type = rs.getString(8);
		    String object = rs.getString(9);
		    String object_type = rs.getString(11);
		    String relation = rs.getString(12);

		    WikiTriple t = new WikiTriple(wikid, "",phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			fntn +=1;
			Pair<String, Double> pred = processRecord(t);
			String prediction = pred.key;
			double prob = pred.value;

			// controlla se abbiamo recuperato qualcosa
			if (prediction != null){
			    Set<String> expected = Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject());

			    if (expected.contains(prediction)){
				//System.out.println("TRUE POSITIVE ("+ String.format ("%.2f", prob)+")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				tp +=1;
			    }else{
				//System.out.println("FALSE POSITIVE ("+ String.format ("%.2f", prob) +")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				fp +=1;
			    }
			    // altrimenti, se non abbiamo recuperato niente
			}else{ 
			    //predictions.add(Pair.make(relation, "-"));
			    //System.out.println("FALSE NEGATIVE\t" + "expected: " + expected + " - predicted: - ---> " + subject_type + " " + phrase_original+ " " + object_type);
			}
		    }
		}
	    }

	}catch(SQLException e){
	    e.printStackTrace();
	}

	double precision = (double) tp/(tp+fp);
	double recall = (double) tp/fntn;

	return Pair.make(precision, recall);
    }

    /**
     * 
     * @param model
     */
    private Map<String, Double> runEvaluationPerRelation(String table_name){
	Map<String, int[]> relation2counts = new HashMap<String, int[]>();
	Map<String, Double> relation2precision = new HashMap<String, Double>();
	String all = "SELECT * FROM " + table_name;

	try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(all)){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String phrase_original = rs.getString(2);
		    String phrase_placeholder = rs.getString(3);
		    String pre = rs.getString(4);
		    String post = rs.getString(5);
		    String subject = rs.getString(6);
		    String subject_type = rs.getString(8);
		    String object = rs.getString(9);
		    String object_type = rs.getString(11);
		    String relation = rs.getString(12);

		    WikiTriple t = new WikiTriple(wikid, "",phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			Pair<String, Double> pred = processRecord(t);
			String prediction = pred.key;
			double prob = pred.value;
			Set<String> expected = Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject());
			// controlla se abbiamo recuperato qualcosa
			if (prediction != null){
			    cases.add(prediction);
			    
			    if (!relation2counts.containsKey(prediction))
				relation2counts.put(prediction, new int[2]);

			    if (expected.contains(prediction)){
				//System.out.println("TRUE POSITIVE ("+ String.format ("%.2f", prob)+")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				relation2counts.get(prediction)[0] += 1;
			    }else{
				//System.out.println("FALSE POSITIVE ("+ String.format ("%.2f", prob) +")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				relation2counts.get(prediction)[1] += 1;
			    }
			    // altrimenti, se non abbiamo recuperato niente
			}else{
			    //predictions.add(Pair.make(relation, "-"));
			    //System.out.println("FALSE NEGATIVE\t" + "expected: " + expected + " - predicted: - ---> " + subject_type + " " + phrase_original+ " " + object_type);
			}
		    }
		}
	    }

	}catch(SQLException e){
	    e.printStackTrace();
	}

	for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()){
	    int tp = relation2counts.get(perRelationCount.getKey())[0];
	    int fp = relation2counts.get(perRelationCount.getKey())[1];
	    
	    double precision = (double) tp/(tp+fp);
	    relation2precision.put(perRelationCount.getKey(), precision);
	}

	return relation2precision;
    }

    /**
     * 
     * @param nParts
     * @param modelType
     * @param phraseType
     * @param topK
     * @param minF
     * @return
     */
    private Pair<Double, Double> runCrossValidation(int nParts, ModelType modelType, PhraseType phraseType, int topK, int minF, double cutoff){
	double avg_precision = 0.0;
	double avg_recall = 0.0;
	for (int it=0; it < nParts; it++){
	    String labeled_table = "cv_labeled_triples_" + it;
	    String evaluation_table = "cv_evaluation_triples_"+ it;
	    this.setModel(modelType, labeled_table, minF, topK, cutoff, phraseType);
	    Pair<Double, Double> precision_recall =  this.runEvaluation(evaluation_table);
	    avg_precision += precision_recall.key;
	    avg_recall += precision_recall.value;
	}
	return Pair.make(avg_precision, avg_recall);
    }

    /**
     * 
     * @param nParts
     * @param modelType
     * @param phraseType
     * @param topK
     * @param minF
     * @return
     */
    private Map<String, Double> runCrossValidationPerRelation(int nParts, ModelType modelType, PhraseType phraseType, int topK, int minF, double cutoff){
	Map<String, Double> globalPrecisionRecall = new HashMap<String, Double>();
	for (int it=0; it < nParts; it++){
	    String labeled_table = "cv_labeled_triples_" + it;
	    String evaluation_table = "cv_evaluation_triples_"+ it;
	    this.setModel(modelType, labeled_table, minF, topK, cutoff, phraseType);
	    Map<String, Double> relation_precision =  this.runEvaluationPerRelation(evaluation_table);
	    for (Map.Entry<String, Double> rel : relation_precision.entrySet()){
		if (!globalPrecisionRecall.containsKey(rel.getKey()))
		    globalPrecisionRecall.put(rel.getKey(), 0.0);
		globalPrecisionRecall.put(rel.getKey(), rel.getValue() + globalPrecisionRecall.get(rel.getKey()));
	    }
	}
	return globalPrecisionRecall;
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	/*************************************************/


	System.out.println("\nEvaluation");
	System.out.println("----------");

	String dbname = Configuration.getLectorFolder() + "/" + "cross.db";
	Evaluator evaluator = new Evaluator(dbname, Lector.getDbmodel(false));

	int nParts = 1;
	double[] cutoff = new double[]{0.1, 0.0};
	int[] topk = new int[]{-1};
	int[] minF = new int[]{5};

	ModelType model = ModelType.TextExtChallenge;

	/*
	for(int tk=0; tk<topk.length; tk++){
	    for(int mf=0; mf<minF.length; mf++){
		for(int cf=0; cf<cutoff.length; cf++){
		    Pair<Double, Double> evaluations = ev.runCrossValidation(nParts, model, PhraseType.TYPED_PHRASES, topk[tk], minF[mf], cutoff[cf]);
		    System.out.println("\nResults with model= " + model.name() + ", topk= " + topk[tk] + ", minF= " + minF[mf] + ", cutoff=" + cutoff[cf]);
		    double precision = (double) evaluations.key/nParts;
		    double recall = (double) evaluations.value/nParts;
		    double f1 = 2.0 * ((precision*recall)/(precision+recall));
		    System.out.println("-> Precision: " + precision);
		    System.out.println("-> Recall: " + recall);
		    System.out.println("-> F1: " + f1);
		}
	    }
	}
	 */

	for(int tk=0; tk<topk.length; tk++){
	    for(int mf=0; mf<minF.length; mf++){
		for(int cf=0; cf<cutoff.length; cf++){
		    Map<String, Double> evaluations = evaluator.runCrossValidationPerRelation(nParts, model, PhraseType.TYPED_PHRASES, topk[tk], minF[mf], cutoff[cf]);
		    System.out.println("\nResults with model= " + model.name() + ", topk= " + topk[tk] + ", minF= " + minF[mf] + ", cutoff=" + cutoff[cf]);
		    for (Map.Entry<String, Double> entry: evaluations.entrySet()){
			System.out.printf("\t%-40s %-20s %s\n", "Relation: " + entry.getKey(), "examples: " + evaluator.cases.get(entry.getKey()), "prec: " + (double) entry.getValue()/nParts);

		    }
		}
	    }
	}

    }
}
