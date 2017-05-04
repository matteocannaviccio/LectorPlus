package it.uniroma3.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import it.uniroma3.model.ModelNB.ModelNBType;
import it.uniroma3.triples.WikiTriple;
/**
 * 
 * @author matteo
 *
 */
public class Extractor {

    private DBlite db_read;
    private DBlite db_write;
    private Model model;

    /**
     * 
     * @param model
     * @param db_write
     */
    public Extractor(Model model, DBlite db_read, DBlite db_write){
	this.db_read = db_read;
	this.db_write = db_write;
	this.model = model;
	db_write.createNovelFactsDB();

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
	if (t.getWikiSubject().equals(t.getWikiObject()))
	    return false;
	String relation = model.predictRelation(t);
	if (relation!=null){
	    db_write.insertNovelFact(t, relation);
	    return true;
	}else{
	    return false;
	}
    }


    /**
     * 
     * @param model
     */
    private void runExtraction(){
	int contProcessed = 0;
	String allUnlabeledTriplesQuery = "SELECT * from unlabeled_triples where type=\"JOINABLE\"";
	try (Statement stmt = db_read.getConnection().createStatement()){	
	    try (ResultSet rs = stmt.executeQuery(allUnlabeledTriplesQuery)){
		while(rs.next()){
		    // wikid text, phrase text, subject text, object text, type_subject text, type_object text, type text
		    String wikid = rs.getString(1);
		    String phrase = rs.getString(2);
		    String subject = rs.getString(3);
		    String object = rs.getString(4);
		    String type_subject = rs.getString(5);
		    String type_object = rs.getString(6);
		    WikiTriple t = new WikiTriple(wikid, phrase, subject, object, type_subject, type_object, "JOINABLE");
		    if (processRecord(t))
			contProcessed+=1;
		    if (contProcessed % 500000 == 0 && contProcessed > 0)
			System.out.println("Extracted " + contProcessed + " novel facts.");
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
    }
    
    /**
     * 
     * @return
     */
    public static void main(String[] args){
	/*
	ModelScoreType type = ModelScoreType.TYPED_PHRASES;
	DBlite db_read = new DBlite("lector.db");
	DBlite db_write = new DBlite("facts.db");
	ModelScore model = new ModelScore(db_read, 20, 20, 100, type);
	Extractor extractor = new Extractor(model, db_read, db_write);
	extractor.runExtraction();
	*/
	
	DBlite db_read = new DBlite("lector.db");
	DBlite db_write = new DBlite("facts.db");
	Model model = new ModelNB(db_read, ModelNBType.TYPED_RELATIONS, 0);
	Extractor extractor = new Extractor(model, db_read, db_write);
	extractor.runExtraction();
    }

}
