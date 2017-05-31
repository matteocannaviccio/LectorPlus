package it.uniroma3.model.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.triples.WikiTriple.TType;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.Ranking;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.PhraseType;
import it.uniroma3.model.model.ModelBM25;
import it.uniroma3.model.model.ModelLS;
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
    public enum ModelType {LectorScore, BM25, NB};

    /**
     * 
     * @param model
     * @param db_read
     */
    public Evaluator(DBModel dbmodel){
	this.dbcrossvaliadation = createDBcrossvalidation(dbmodel, 5);
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
	    int topK, PhraseType typePhrase){
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
	}
    }

    /**
     * 
     * @param dbmodel
     * @param nParts
     * @return
     */
    private DBCrossValidation createDBcrossvalidation(DBModel dbmodel, int nParts){
	return new DBCrossValidation("cross.db", dbmodel, nParts);
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

		    WikiTriple t = new WikiTriple(wikid, phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			fntn +=1;
			Pair<String, Double> pred = processRecord(t);
			String prediction = pred.key;
			double prob = pred.value;

			// controlla se abbiamo recuperato qualcosa
			if (prediction != null){
			    if (relation.replace("(-1)", "").equals(prediction.replace("(-1)", ""))){
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
	System.out.println("Best Phrases : " + Ranking.getTopKRanking(bestPhrases, 20));
	System.out.println("Bad Phrases : " + Ranking.getTopKRanking(badPhrases, 20));

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
    private Pair<Double, Double> runCrossValidation(int nParts, ModelType modelType, PhraseType phraseType, int topK, int minF){
	// {1,2,3,4,5} --> change if you want to average accross all the 5 parts
	double avg_precision = 0.0;
	double avg_recall = 0.0;
	for (int it=0; it < nParts; it++){
	    String labeled_table = "cv_labeled_triples_" + it;
	    String evaluation_table = "cv_evaluation_triples_"+ it;
	    this.setModel(modelType, labeled_table, minF, topK, phraseType);
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
	Evaluator ev = new Evaluator(new DBModel("model.db"));
	int nParts = 1;
	int[] topk = new int[]{5, 100, -1};
	int[] minF = new int[]{0, 10, 100};

	//for (ModelType model : ModelType.values()){
	//for(int tk : topk){
	//for (int mF : minF){
	ModelType model = ModelType.NB;
	int tk = 100;
	int mF = 10;
	Pair<Double, Double> evaluations = ev.runCrossValidation(nParts, model, PhraseType.TYPED_PHRASES, tk, mF);
	System.out.println("***********************************");
	System.out.println("Results with model= " + model.name() + ", topk= " + tk + " and minF= " + mF);
	System.out.println("-> Precision: " + (double) evaluations.key/nParts);
	System.out.println("-> Recall: " + (double) evaluations.value/nParts);
	//}
	// }
	//}
    }
}
