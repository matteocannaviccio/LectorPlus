package it.uniroma3.model.evaluation;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;

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

    /**
     * 
     * @param model
     * @param db_read
     */
    public Evaluator(String dbcrossvalidationName, DBModel dbmodel){
	this.dbcrossvaliadation = createDBcrossvalidation(dbcrossvalidationName, dbmodel, 5);
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
    private void setModel(ModelType type, String labeled_table, int minFreq, 
	    int topK, double cutoff, PhraseType typePhrase){
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
     * @param crossDBName
     * @param dbmodel
     * @param nParts
     * @return
     */
    private DBCrossValidation createDBcrossvalidation(String crossDBName, DBModel dbmodel, int nParts){
	if (!new File(crossDBName).exists())
	    return new DBCrossValidation(crossDBName, dbmodel, nParts);
	else
	    return new DBCrossValidation(crossDBName);
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

	CounterMap<String> bestPhrases = new CounterMap<String>();
	CounterMap<String> badPhrases = new CounterMap<String>();

	String all = "SELECT * FROM " + table_name;

	try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(all)){
		while(rs.next() && fntn < 100000){
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
			    if (Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject()).contains(prediction)){
				//System.out.println("TRUE POSITIVE ("+ String.format ("%.2f", prob)+")\t" + "expected: " + relation + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				bestPhrases.add(subject_type + " " + phrase_placeholder+ " " + object_type);
				tp +=1;
			    }else{
				//System.out.println("FALSE POSITIVE ("+ String.format ("%.2f", prob) +")\t" + "expected: " + relation + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				fp +=1;
				badPhrases.add(subject_type + " " + phrase_placeholder+ " " + object_type);
			    }
			    // altrimenti, se non abbiamo recuperato niente
			}else{ 
			    //predictions.add(Pair.make(relation, "-"));
			    //System.out.println("FALSE NEGATIVE\t" + "expected: " + relation + " - predicted: - ---> " + subject_type + " " + phrase_original+ " " + object_type);
			}
		    }
		}
	    }

	}catch(SQLException e){
	    e.printStackTrace();
	}
	double precision = (double) tp/(tp+fp);
	double recall = (double) tp/fntn;
	//System.out.println("Best Phrases : " + Ranking.getTopKRanking(bestPhrases, 20));
	//System.out.println("Bad Phrases : " + Ranking.getTopKRanking(badPhrases, 20));

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
     * @param args
     */
    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "it");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	/*************************************************/


	System.out.println("\nEvaluation");
	System.out.println("----------");

	String dbname = Configuration.getLectorFolder() + "/" + "cross.db";
	Evaluator ev = new Evaluator(dbname, Lector.getDbmodel(false));

	int nParts = 2;
	double[] cutoff = new double[]{0.1, 0.0};
	int[] topk = new int[]{-1};
	int[] minF = new int[]{5};

	ModelType model = ModelType.TextExtChallenge;

	for(int tk=0; tk<topk.length; tk++){
	    for(int mf=0; mf<minF.length; mf++){
		for(int cf=0; cf<cutoff.length; cf++){
		    Pair<Double, Double> evaluations = ev.runCrossValidation(nParts, model, PhraseType.TYPED_PHRASES, topk[tk], minF[mf], cutoff[cf]);
		    System.out.println("\nResults with model= " + model.name() + ", topk= " + topk[tk] + ", minF= " + minF[mf] + ", cutoff=" + cutoff[cf]);
		    System.out.println("-> Precision: " + (double) evaluations.key/nParts);
		    System.out.println("-> Recall: " + (double) evaluations.value/nParts);
		}
	    }
	}

    }
}
