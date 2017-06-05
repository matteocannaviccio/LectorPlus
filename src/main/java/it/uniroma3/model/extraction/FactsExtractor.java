package it.uniroma3.model.extraction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.triples.WikiTriple.TType;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.extractor.util.reader.NTriplesWriter;
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
public class FactsExtractor {

    private Model model;
    public enum ModelType {BM25, LectorScore, NB};
    private NTriplesWriter writer;

    /**
     * 
     * @param model
     * @param db_write
     */
    public FactsExtractor(){
	System.out.println("\n**** NEW FACTS EXTRACTION ****");
	Lector.getDbfacts(true);
	this.writer = new NTriplesWriter(getOutputStreamBZip2(Configuration.getOutputFactsFile()));
    }

    /**
     * Process the triple to label.
     * It can not have the same entities as subject and object. 
     * Return a true value if we can extract a new facts, false otherwise.
     * 
     * @param t
     * @return
     */
    private boolean processRecord(WikiTriple t){
	Pair<String, Double> prediction = model.predictRelation(t);
	String relation = prediction.key;
	if (relation!=null){
	    writer.statement(t.getWikid(), t.getWikiSubject(), relation, t.getWikiObject(), false);
	    Lector.getDbfacts(false).insertNovelFact(t, relation);
	    return true;
	}else{
	    return false;
	}
    }


    /**
     * 
     * @param model
     */
    public void runExtraction(){
	int contProcessed = 0;
	String allUnlabeledTriplesQuery = "SELECT * FROM unlabeled_triples";
	try (Statement stmt = Lector.getDbmodel(false).getConnection().createStatement()){	
	    try (ResultSet rs = stmt.executeQuery(allUnlabeledTriplesQuery)){
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

		    WikiTriple t = new WikiTriple(wikid, phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());

		    if (!t.getWikiSubject().equals(t.getWikiObject())){
			if (processRecord(t)){
			    contProcessed+=1;
			}
			if (contProcessed % 1000 == 0 && contProcessed > 0)
			    System.out.println("Extracted " + contProcessed + " novel facts.");
		    }
		}
	    }
	   
	    // close the output stream
	    writer.done();
	    
	}catch(SQLException e){
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
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
    public void setModelForEvaluation(ModelType type, String labeled_table, int minFreq, 
	    int topK, PhraseType typePhrase){
	switch(type){
	
	case BM25:
	    model = new ModelBM25(Lector.getDbmodel(false), labeled_table, minFreq, topK, PhraseType.TYPED_PHRASES);
	    break;
	    
	case NB:
	    model = new ModelNB(Lector.getDbmodel(false), labeled_table, minFreq, ModelNBType.CLASSIC);
	    break;
	    
	case LectorScore:
	    model = new ModelLS(Lector.getDbmodel(false), labeled_table, minFreq, topK, 0.5, 0.5, PhraseType.TYPED_PHRASES);
	    break;
	}
    }
    
    /**
     * 
     * @param path
     * @return
     */
    @SuppressWarnings("resource")
    private OutputStream getOutputStreamBZip2(String path) {
	OutputStream out = null;
	try {
	    out = new FileOutputStream(path);
	    out = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out); 
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (CompressorException e) {
	    e.printStackTrace();
	}
	return out;
    }


}
