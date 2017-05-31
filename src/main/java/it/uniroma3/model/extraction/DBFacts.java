package it.uniroma3.model.extraction;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.model.DB;

public class DBFacts extends DB{

    /**
     * 
     * @param dbname
     */
    public DBFacts(String dbname) {
	super(dbname);
	createFactsDB();
    }
    
    /**
     * 
     */
    public void createFactsDB(){
	String dropLabeled = "DROP TABLE IF EXISTS novel_facts";
	String createLabeled = "CREATE TABLE novel_facts("
		+ "wikid text, "
		+ "phrase_original text, "
		+ "phrase_placeholder text, "
		+ "phrase_pre text, "
		+ "phrase_post text, "
		+ "subject text, "
		+ "wiki_subject text, "
		+ "type_subject text, "
		+ "object text, "
		+ "wiki_object text, "
		+ "type_object text, "
		+ "relation text)";
	String indexrelation = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
		+ "ON novel_facts(relation)";
	String indexsbjobj = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
		+ "ON novel_facts(subject, object)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(indexrelation);
	    stmt.executeUpdate(indexsbjobj);
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }
    
    
    /**
     * This is the schema of novel_facts:
     *    
     *     	01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     *     	04- phrase_pre text
     * 		05- phrase_post text
     * 		06- subject text
     * 		07- wiki_subject text
     * 		08- type_subject text
     * 		09- object text
     * 		10- wiki_object text
     * 		11- type_object text
     * 		12- relation text
     * 
     * @param triple
     * @param relation
     */
    public void insertNovelFact(WikiTriple triple, String relation){
	String insert = "INSERT INTO novel_facts VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhraseOriginal());
	    stmt.setString(3, triple.getPhrasePlaceholders());
	    stmt.setString(4, triple.getPre());
	    stmt.setString(5, triple.getPost());
	    stmt.setString(6, triple.getSubject());
	    stmt.setString(7, triple.getWikiSubject());
	    stmt.setString(8, triple.getSubjectType());
	    stmt.setString(9, triple.getObject());
	    stmt.setString(10, triple.getWikiObject());
	    stmt.setString(11, triple.getObjectType());
	    stmt.setString(12, relation);
	    stmt.execute();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }
    
    /**
     * 
     */
    public void closeConnection() {
	this.closeConnection();
    }

}
