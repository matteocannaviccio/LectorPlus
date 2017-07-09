package it.uniroma3.model.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.Model.PhraseType;
import it.uniroma3.model.model.ModelBM25;
import it.uniroma3.model.model.ModelNB;
import it.uniroma3.model.model.ModelNBFilter;
import it.uniroma3.model.model.ModelNBind;
import it.uniroma3.model.model.ModelTextExt;
/**
 * 
 * @author matteo
 *
 */
public class Evaluator {

    private DBCrossValidation dbcrossvaliadation;
    private Model model;
    /**
     * This is a map containing 3 numbers for each relation:
     * [0] = true positive
     * [1] = false positive
     * [2] = all predicted
     */
    private Map<String, int[]> relation2counts = new HashMap<String, int[]>();
    private int totalCounts;

    private Map<String, Double> relation2accuracy;
    private Map<String, Integer> relation2hits;
    private Pair<Double, Double> evals;

    /**
     * 
     * @param model
     * @param db_read
     */
    public Evaluator(String dbcrossvalidationName, DBModel dbmodel){
	this.dbcrossvaliadation = loadOrCreateCrossValidationDB(dbcrossvalidationName, dbmodel, 5);
    }

    private void initializeCounts(){
	relation2counts = new HashMap<String, int[]>();
	totalCounts = 0;
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
    private void setModel(ModelType type, String labeled_table, int minFreq, int topK, double cutoff){
	switch(type){
	case BM25:
	    model = new ModelBM25(this.dbcrossvaliadation, labeled_table, minFreq);
	    break;
	case NB:
	    model = new ModelNB(this.dbcrossvaliadation, labeled_table, minFreq);
	    break;
	case NBind:
	    model = new ModelNBind(this.dbcrossvaliadation, labeled_table, minFreq);
	    break;
	case NBfilter:
	    model = new ModelNBFilter(this.dbcrossvaliadation, labeled_table, minFreq);
	    break;
	case TextExtChallenge:
	    model = new ModelTextExt(this.dbcrossvaliadation, labeled_table, minFreq, topK, cutoff);
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
	return model.predictRelation(t.getSubjectType(), t.getPhrasePlaceholders(), t.getObjectType());
    }

    /**
     * 
     * @param model
     */
    private void runEvaluation(String table_name){

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

		    WikiTriple t = new WikiTriple(wikid, "",phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			updateCounts(t);
		    }
		}
	    }

	}catch(SQLException e){
	    e.printStackTrace();
	}

    }

    /**
     * 
     * @param t
     * @param relation2counts
     */
    private void updateCounts(WikiTriple t){
	Pair<String, Double> pred = processRecord(t);
	String prediction = pred.key;
	totalCounts+=1;

	// controlla se abbiamo recuperato qualcosa
	if (prediction != null && !prediction.equals("NONE")){
	    // abbiamo fatto una predizione
	    Set<String> expected = Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject());

	    if (!relation2counts.containsKey(prediction))
		relation2counts.put(prediction, new int[3]);
	    relation2counts.get(prediction)[2]+=1;
	    if (expected.contains(prediction)){
		//System.out.println("TRUE POSITIVE ("+ String.format ("%.2f", prob)+")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
		relation2counts.get(prediction)[0]+=1;
	    }else{
		//System.out.println("FALSE POSITIVE ("+ String.format ("%.2f", prob) +")\t" + "expected: " + expected + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
		relation2counts.get(prediction)[1]+=1;
	    }
	}
    }


    /**
     * Returns a map containg the accuracy for each relation.
     * 
     * @param relation2counts
     * @return
     */
    private Map<String, Double> calcAccuracyPerRelation(){
	/**
	 * This is a map containg the accuracy for each relation
	 */
	Map<String, Double> relation2accuracy = new HashMap<String, Double>();
	for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()){
	    int tp = relation2counts.get(perRelationCount.getKey())[0];
	    int all_predicted = relation2counts.get(perRelationCount.getKey())[2];
	    double accuracy = (double) tp/(all_predicted);
	    relation2accuracy.put(perRelationCount.getKey(), accuracy);
	}
	return relation2accuracy;
    }

    /**
     * Returns a map containg the hits and tests for each relation.
     * 
     * @param relation2counts
     * @return
     */
    private Map<String, Integer> calcHitsPerRelation(){
	Map<String, Integer> relation2hits = new HashMap<String, Integer>();
	for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()){
	    int all_predicted = relation2counts.get(perRelationCount.getKey())[2];
	    relation2hits.put(perRelationCount.getKey(), all_predicted);
	}
	return relation2hits;
    }

    /**
     * 
     * @param relation2counts
     * @return
     */
    private Pair<Double, Double> calcAccuracyRecallGlobal(Map<String, int[]> relation2counts){
	int tp_global = 0;
	int fp_global = 0;
	for (Map.Entry<String, int[]> perRelationCount : relation2counts.entrySet()){
	    tp_global += relation2counts.get(perRelationCount.getKey())[0];
	    fp_global += relation2counts.get(perRelationCount.getKey())[1];
	}
	double precision = (double) tp_global/(tp_global+fp_global);
	double recall = (double) tp_global/totalCounts;
	return Pair.make(precision, recall);
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
    private void runCrossValidation(int nParts, ModelType modelType, int topK, int minF, double cutoff){
	double avg_acc = 0.0;
	double avg_rec = 0.0;

	for (int it=0; it < nParts; it++){
	    String labeled_table = "cv_labeled_triples_" + it;
	    String evaluation_table = "cv_evaluation_triples_"+ it;
	    initializeCounts();
	    this.setModel(modelType, labeled_table, minF, topK, cutoff);
	    this.runEvaluation(evaluation_table);

	    Pair<Double, Double> avg_measures = calcAccuracyRecallGlobal(relation2counts);
	    avg_acc += avg_measures.key;
	    avg_rec += avg_measures.value;
	}

	relation2accuracy = calcAccuracyPerRelation();
	relation2hits = calcHitsPerRelation();
	evals = Pair.make(avg_acc/nParts, avg_rec/nParts);
    }

    /**
     * @return the relation2counts
     */
    public Map<String, int[]> getRelation2counts() {
	return relation2counts;
    }

    /**
     * @return the relation2accuracy
     */
    public Map<String, Double> getRelation2accuracy() {
	return relation2accuracy;
    }

    /**
     * @return the evals
     */
    public Pair<Double, Double> getEvals() {
	return evals;
    }

    private void run() throws IOException{

	int nParts = 5;
	/*--*/
	double[] cutoff = new double[]{0.1, 0.0};
	int[] topk = new int[]{-1};
	int[] minF = new int[]{1,3,5};
	ModelType[] models = new ModelType[]{ModelType.NB, ModelType.NBfilter, ModelType.NBind, ModelType.TextExtChallenge, ModelType.BM25};
	/*--*/
	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Configuration.getLectorFolder() + "/cross_validation_results.tsv")));

	/**
	 * This will print a dataset with the following columns:
	 * 
	 * ModelType  \t  minF  \t  topK  \t  cutoff  \t  label   \t   accuracy  \t  recall
	 * 
	 */
	bw.write("model\tminF\ttopK\tcutoff\tlabel\taccuracy\trecall\tf1\n");
	bw.flush();
	for(ModelType model : models){
	    for(int tk=0; tk<topk.length; tk++){
		for(int mf=0; mf<minF.length; mf++){
		    if(model.equals(ModelType.TextExtChallenge)){
			for(int cf=0; cf<cutoff.length; cf++){
			    runCrossValidation(nParts, model, topk[tk], minF[mf], cutoff[cf]);
			    System.out.println("Results with model= " + model.name() + ", topk= " + topk[tk] + ", minF= " + minF[mf] + ", cutoff=" + cutoff[cf]);
			    double precision = getEvals().key;
			    double recall = getEvals().value;
			    String label = model.name()+", minF="+minF[mf]+", topK="+topk[tk]+", cutoff="+cutoff[cf];
			    bw.write(model.name() + "\t" + minF[mf] + "\t" + topk[tk] + "\t" + cutoff[cf] + "\t" + label+ "\t"+ precision + "\t" + recall + "\n");
			    bw.flush();
			    File perRelationDetails = new File(Configuration.getLectorFolder() + "/relations/" + model +"_"+topk[tk]+"_"+minF[mf]+"_"+cutoff[cf]+".tsv");
			    perRelationDetails.getParentFile().mkdirs();
			    BufferedWriter bw2 = new BufferedWriter(new FileWriter(perRelationDetails));
			    for (String relation : Ranking.getRanking(this.relation2accuracy).keySet())
				bw2.write(relation + "\t" + this.relation2accuracy.get(relation) + "\t" + this.relation2hits.get(relation) + "\n");
			    bw2.close();

			}
		    }else{
			runCrossValidation(nParts, model, topk[tk], minF[mf], 0.0);
			System.out.println("Results with model= " + model.name() + ", topk= " + topk[tk] + ", minF= " + minF[mf]);
			double precision = getEvals().key;
			double recall = getEvals().value;
			String label = model.name()+", minF="+minF[mf]+", topK="+topk[tk];
			bw.write(model.name() + "\t" + minF[mf] + "\t" + topk[tk] + "\t" + 0.0 + "\t" + label + "\t" + precision + "\t" + recall + "\n");
			bw.flush();
			File perRelationDetails = new File(Configuration.getLectorFolder() + "/relations/" + model +"_"+topk[tk]+"_"+minF[mf]+".tsv");
			perRelationDetails.getParentFile().mkdirs();
			BufferedWriter bw2 = new BufferedWriter(new FileWriter(perRelationDetails));
			for (String relation : Ranking.getRanking(this.relation2accuracy).keySet())
			    bw2.write(relation + "\t" + this.relation2accuracy.get(relation) + "\t" + this.relation2hits.get(relation) + "\n");
			bw2.close();
		    }
		}
	    }

	}
	bw.close();
    }

    /**
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("language", lang);
	    String dbname = Configuration.getLectorFolder() + "/" + "cross.db";
	    Evaluator evaluator = new Evaluator(dbname, Lector.getDbmodel(false));
	    Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		    new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	    System.out.println("\nEvaluation in " + Configuration.getLanguageCode());
	    System.out.println("----------");
	    evaluator.run();
	    Lector.close();
	}

    }

}
