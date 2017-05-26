package it.uniroma3.model.evaluation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.triples.WikiTriple.TType;
import it.uniroma3.extractor.util.Pair;
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
    public enum ModelType {BM25, LectorScore, NB};

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
	    model = new ModelBM25(this.dbcrossvaliadation, labeled_table, minFreq, topK, PhraseType.TYPED_PHRASES);
	    break;
	case NB:
	    model = new ModelNB(this.dbcrossvaliadation, labeled_table, minFreq, ModelNBType.CLASSIC);
	    break;
	case LectorScore:
	    model = new ModelLS(this.dbcrossvaliadation, labeled_table, minFreq, topK, 0.5, 0.5, PhraseType.TYPED_PHRASES);
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
	DBCrossValidation dbc = new DBCrossValidation("cross.db", dbmodel, nParts);
	return dbc;
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

	//CounterMap<String> bestPhrases = new CounterMap<>();
	String all = "SELECT * FROM " + table_name;

	try (Statement stmt = this.dbcrossvaliadation.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(all)){
		while(rs.next() && fntn < 500000){
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
			    if (relation.equals(prediction)){
				//System.out.println("TRUE POSITIVE ("+ String.format ("%.2f", prob)+")\t" + "expected: " + relation + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				tp +=1;
			    }else{
				//System.out.println("FALSE POSITIVE ("+ String.format ("%.2f", prob) +")\t" + "expected: " + relation + " - predicted: " + prediction +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
				fp +=1;
			    }
			// altrimenti, se non abbiamo recuperato niente
			}else{ 
			    //predictions.add(Pair.make(relation, "-"));
			    //System.out.println("FALSE NEGATIVE\t" + "expected: " + relation + " - predicted: " + predict +" ---> " + subject_type + " " + phrase_original+ " " + object_type);
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
     * @param args
     */
    public static void main(String[] args){
	Evaluator ev = new Evaluator(new DBModel("model.db"));

	// {1,2,3,4,5} --> change if you want to average accross all the 5 parts
	int its = 1;
	double avg_precision = 0.0;
	double avg_recall = 0.0;
	
	for (int it=0; it < its; it++){
	    String labeled_table = "cv_labeled_triples_" + it;
	    String evaluation_table = "cv_evaluation_triples_"+ it;

	    ev.setModel(ModelType.NB, labeled_table, 0, -1, PhraseType.TYPED_PHRASES);
	    Pair<Double, Double> precRec =  ev.runEvaluation(evaluation_table);

	    avg_precision += precRec.key;
	    avg_recall += precRec.value;
	}
	
	System.out.println("-> Precision: " + (double) avg_precision/its);
	System.out.println("-> Recall: " + (double) avg_recall/its);
    }
}
