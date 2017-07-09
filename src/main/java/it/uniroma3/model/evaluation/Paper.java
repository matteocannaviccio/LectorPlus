package it.uniroma3.model.evaluation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import it.uniroma3.config.Configuration;
import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.bean.WikiTriple.TType;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.DBModel;

public class Paper {
    
    private DB db;
    private String labeled_facts = "labeled_triples";
    private String unlabeled_facts = "unlabeled_triples";

    public Paper(DBModel db){
	this.db = db;
	db.createNecessaryIndexes();
    }
    
    /***************************************** 
     * 
     * 		SELECT ALL labeled_triples
     * 
     ******************************************/
    public List<Pair<WikiTriple, String>> selectAllLabeled(){
	List<Pair<WikiTriple, String>> triples = new LinkedList<Pair<WikiTriple, String>>();
	String query = "SELECT * FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String section = rs.getString(2);
		    String phrase_original = rs.getString(3);
		    String phrase_placeholder = rs.getString(4);
		    String pre = rs.getString(5);
		    String post = rs.getString(6);
		    String subject = rs.getString(7);
		    String type_subject = rs.getString(9);
		    String object = rs.getString(10);
		    String type_object = rs.getString(12);
		    String relation = rs.getString(13);
		    triples.add(Pair.make(new WikiTriple(wikid, section, "", phrase_original,
			    phrase_placeholder, pre, post, subject, object, 
			    type_subject, type_object, TType.JOINABLE.name()), relation));
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return triples;
    }
    
    /***************************************** 
     * 
     * 		SELECT ALL unlabeled_triples
     * 
     ******************************************/
    public List<WikiTriple> selectAllUnabeled(){
	List<WikiTriple> triples = new LinkedList<WikiTriple>();
	String query = "SELECT * FROM " + unlabeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String section = rs.getString(2);
		    String sentence = rs.getString(3);
		    String phrase_original = rs.getString(4);
		    String phrase_placeholder = rs.getString(5);
		    String pre = rs.getString(6);
		    String post = rs.getString(7);
		    String subject = rs.getString(8);
		    String type_subject = rs.getString(10);
		    String object = rs.getString(11);
		    String type_object = rs.getString(13);
		    triples.add(new WikiTriple(wikid, section, sentence, phrase_original,
			    phrase_placeholder, pre, post, subject, object, 
			    type_subject, type_object, TType.JOINABLE.name()));
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return triples;
    }
    
    /***************************************** 
     * 
     * 		SELECT ALL unlabeled_triples
     * 
     ******************************************/
    public List<WikiTriple> selectAllOther(){
	List<WikiTriple> triples = new LinkedList<WikiTriple>();
	String query = "SELECT * FROM other_triples";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String section = rs.getString(2);
		    String sentence = rs.getString(3);
		    String phrase_original = rs.getString(4);
		    String phrase_placeholder = rs.getString(5);
		    String pre = rs.getString(6);
		    String post = rs.getString(7);
		    String subject = rs.getString(8);
		    String type_subject = rs.getString(10);
		    String object = rs.getString(11);
		    String type_object = rs.getString(13);
		    String type = rs.getString(14);
		    triples.add(new WikiTriple(wikid, section, sentence, phrase_original,
			    phrase_placeholder, pre, post, subject, object, 
			    type_subject, type_object, type));
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return triples;
    }
    
    public void run(){
	List<Pair<WikiTriple, String>> allLabeled = selectAllLabeled();
	
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
	    Paper paper = new Paper(new DBModel(Configuration.getDBModel()));

	    System.out.println("\nEvaluation in " + Configuration.getLanguageCode());
	    System.out.println("----------");
	    paper.run();
	}

    }
}
