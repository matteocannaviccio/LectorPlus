package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;

import it.uniroma3.extractor.bean.WikiMVL;
import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
/**
 * 
 * @author matteo
 *
 */
public class DBModel extends DB{

    public DBModel(String dbname){
	super(dbname);
	//createNecessaryIndexes();
    }

    public void createModelDB(){
	String dropLabeled = "DROP TABLE IF EXISTS labeled_triples";
	String createLabeled = "CREATE TABLE labeled_triples("
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
	String dropUnlabeled = "DROP TABLE IF EXISTS unlabeled_triples";
	String createUnlabeled = "CREATE TABLE unlabeled_triples("
		+ "wikid text, "
		+ "sentence text, "
		+ "phrase_original text, "
		+ "phrase_placeholder text, "
		+ "phrase_pre text, "
		+ "phrase_post text, "
		+ "subject text, "
		+ "wiki_subject text, "
		+ "type_subject text, "
		+ "object text, "
		+ "wiki_object text, "
		+ "type_object text)";
	String dropOther = "DROP TABLE IF EXISTS other_triples";
	String createOther = "CREATE TABLE other_triples("
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
		+ "type text)";
	String dropMVLCollection = "DROP TABLE IF EXISTS mvl_collection";
	String createMVLCollection = "CREATE TABLE mvl_collection("
		+ "code text, "
		+ "wikid text, "
		+ "section text, "
		+ "list text)";
	String dropNationalitiesCollection = "DROP TABLE IF EXISTS nationality_collection";
	String createNationalitiesCollection = "CREATE TABLE nationality_collection("
		+ "wikid text, "
		+ "sentence text, "
		+ "subject_type text, "
		+ "object text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(dropUnlabeled);
	    stmt.executeUpdate(createUnlabeled);
	    stmt.executeUpdate(dropOther);
	    stmt.executeUpdate(createOther);
	    stmt.executeUpdate(dropMVLCollection);
	    stmt.executeUpdate(createMVLCollection);
	    stmt.executeUpdate(dropNationalitiesCollection);
	    stmt.executeUpdate(createNationalitiesCollection);
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
    public void createNecessaryIndexes(){
	System.out.println("Creating indexes");
	String indexModelRelationPhrase = "CREATE INDEX IF NOT EXISTS indexmodelrelationphrase "
		+ "ON labeled_triples(relation, phrase_placeholder)";
	String indexModelPhrase = "CREATE INDEX IF NOT EXISTS indexmodelphrase "
		+ "ON labeled_triples(phrase_placeholder)";
	String indexModelTypesPhrase = "CREATE INDEX IF NOT EXISTS indexmodeltypesphrase "
		+ "ON labeled_triples(type_subject, type_object, relation, phrase_placeholder)";
	String indexUnlabeledPhrase = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
		+ "ON unlabeled_triples(phrase_placeholder, type_subject, type_object)";
	String indexOther = "CREATE INDEX IF NOT EXISTS indexother "
		+ "ON other_triples(type)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(indexModelRelationPhrase);
	    stmt.executeUpdate(indexModelPhrase);
	    stmt.executeUpdate(indexModelTypesPhrase);
	    stmt.executeUpdate(indexOther);
	    stmt.executeUpdate(indexUnlabeledPhrase);

	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
	System.out.println("Done");

    }

    /**
     * This is the schema of labeled_triples:
     * 
     * 		01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     * 		04- phrase_pre text
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
    public void batchInsertLabeledTriple(Queue<Pair<WikiTriple, String>> labeled_triples){
	String insert = "INSERT INTO labeled_triples VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try {
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.getConnection().prepareStatement(insert);
	    for (Pair<WikiTriple, String> triple : labeled_triples){
		stmt.setString(1, triple.key.getWikid());
		stmt.setString(2, triple.key.getPhraseOriginal());
		stmt.setString(3, triple.key.getPhrasePlaceholders());
		stmt.setString(4, triple.key.getPre());
		stmt.setString(5, triple.key.getPost());
		stmt.setString(6, triple.key.getSubject());
		stmt.setString(7, triple.key.getWikiSubject());
		stmt.setString(8, triple.key.getSubjectType());
		stmt.setString(9, triple.key.getObject());
		stmt.setString(10, triple.key.getWikiObject());
		stmt.setString(11, triple.key.getObjectType());
		stmt.setString(12, triple.value);
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.getConnection().commit();
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
     * This is the schema of unlabeled_triples:
     * 
     * 		01- wikid text
     * 		02- sentence text
     * 		03- phrase_original text
     * 		04- phrase_placeholder text
     *      	05- phrase_pre text
     * 		06- phrase_post text
     * 		07- subject text
     * 		08- wiki_subject text
     * 		09- type_subject text
     * 		10- object text
     * 		11- wiki_object text
     * 		12- type_object text
     * 
     * @param triple
     */
    public void batchInsertUnlabeledTriple(Queue<WikiTriple> unlabeled_triples){
	String insert = "INSERT INTO unlabeled_triples VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try {
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.getConnection().prepareStatement(insert);
	    for (WikiTriple triple : unlabeled_triples){
		stmt.setString(1, triple.getWikid());
		stmt.setString(2, triple.getWholeSentence());
		stmt.setString(3, triple.getPhraseOriginal());
		stmt.setString(4, triple.getPhrasePlaceholders());
		stmt.setString(5, triple.getPre());
		stmt.setString(6, triple.getPost());
		stmt.setString(7, triple.getSubject());
		stmt.setString(8, triple.getWikiSubject());
		stmt.setString(9, triple.getSubjectType());
		stmt.setString(10, triple.getObject());
		stmt.setString(11, triple.getWikiObject());
		stmt.setString(12, triple.getObjectType());
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.getConnection().commit();
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
     * This is the schema of other_triples:
     * 
     * 		01- wikid text
     * 		02- phrase_original text
     * 		03- phrase_placeholder text
     *      	04- phrase_pre text
     * 		05- phrase_post text
     * 		06- subject text
     * 		07- wiki_subject text
     * 		08- type_subject text
     * 		09- object text
     * 		10- wiki_object text
     * 		11- type_object text
     * 		12- type text
     * 
     * @param triple
     */
    public void batchInsertOtherTriple(Queue<WikiTriple> other_triples){
	String insert = "INSERT INTO other_triples VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try {
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.getConnection().prepareStatement(insert);
	    for (WikiTriple triple : other_triples){
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
		stmt.setString(12, triple.getType().name());
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.getConnection().commit();
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
     * @param list
     */
    public void batchInsertMVList(Queue<WikiMVL> lists){
	String insert = "INSERT INTO mvl_collection VALUES(?,?,?,?)";
	try {
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.getConnection().prepareStatement(insert);
	    for (WikiMVL mvl : lists){
		stmt.setString(1, mvl.getCode());
		stmt.setString(2, mvl.getWikid());
		stmt.setString(3, mvl.getSection());
		stmt.setString(4, StringUtils.join(mvl.getListWikid(), ","));
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.getConnection().commit();
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
     * @param list
     */
    public void batchInsertNationalityTriple(Queue<String[]> nationalities){
	String insert = "INSERT INTO nationality_collection VALUES(?,?,?,?)";
	try {
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.getConnection().prepareStatement(insert);
	    for (String[] nat : nationalities){
		stmt.setString(1, nat[0]);
		stmt.setString(2, nat[1]);
		stmt.setString(3, nat[2]);
		stmt.setString(4, nat[3]);
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.getConnection().commit();
	}catch(SQLException e){
	    try {
		this.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

}
