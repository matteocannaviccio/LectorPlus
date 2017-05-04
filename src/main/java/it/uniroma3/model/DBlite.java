package it.uniroma3.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import edu.stanford.nlp.util.StringUtils;
import it.uniroma3.triples.WikiMVL;
import it.uniroma3.triples.WikiTriple;
/**
 * 
 * @author matteo
 *
 */
public class DBlite {

    private Connection connection;

    /**
     * 
     */
    public DBlite(String dbname){
	this.connection = obtainConnection(dbname);
    }

    /**
     * 
     * @return
     */
    public Connection obtainConnection(String dbname){
	String sDriverName = "org.sqlite.JDBC";
	Connection conn = null;
	try {
	    Class.forName(sDriverName);
	    String sJdbc = "jdbc:sqlite";
	    String sDbUrl = sJdbc + ":" + dbname;
	    conn = DriverManager.getConnection(sDbUrl);
	    Statement st = conn.createStatement();        
	    st.execute("PRAGMA synchronous=OFF");
	    st.execute("PRAGMA jorunal_mode=MEMORY");
	} catch (ClassNotFoundException | SQLException e) {
	    e.printStackTrace();
	}
	return conn;
    }


    /**
     * 
     */
    public void createDB(){
	String dropLabeled = "DROP TABLE IF EXISTS labeled_triples";
	String createLabeled = "CREATE TABLE labeled_triples(wikid text, phrase text, subject text, object text, type_subject text, type_object text, relation text)";
	String dropModelStats = "DROP TABLE IF EXISTS model_stats";
	String createModelsStats = "CREATE TABLE model_stats(phrase text, type_subject text, type_object text, relation text, count int, PRIMARY KEY (phrase, type_subject, type_object, relation))";
	String indexModelStats = "CREATE INDEX indexphrase ON model_stats(type_subject, type_object, relation, phrase)";
	String dropUnlabeled = "DROP TABLE IF EXISTS unlabeled_triples";
	String createUnlabeled = "CREATE TABLE unlabeled_triples(wikid text, phrase text, subject text, object text, type_subject text, type_object text, type text)";
	String dropMVLCollection = "DROP TABLE IF EXISTS mvl_collection";
	String createMVLCollection = "CREATE TABLE mvl_collection(code text, section text, wikid text, list text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(dropModelStats);
	    stmt.executeUpdate(createModelsStats);
	    stmt.executeUpdate(indexModelStats);
	    stmt.executeUpdate(dropUnlabeled);
	    stmt.executeUpdate(createUnlabeled);
	    stmt.executeUpdate(dropMVLCollection);
	    stmt.executeUpdate(createMVLCollection);
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     */
    public void createNovelFactsDB(){
	String dropLabeled = "DROP TABLE IF EXISTS novel_facts";
	String createLabeled = "CREATE TABLE novel_facts("
		+ "wikid text, phrase text, subject text, object text, wikisubject text, "
		+ "wikiobject text, type_subject text, type_object text, relation text)";
	String indexrelation = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase ON novel_facts(relation)";
	String indexsbjobj = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase ON novel_facts(subject,object)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(indexrelation);
	    stmt.executeUpdate(indexsbjobj);
	}catch(SQLException e){
	    try {
		connection.rollback();
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
	System.out.print("Writing indexes ... ");
	String indexModelRelationPhrase = "CREATE INDEX IF NOT EXISTS indexmodelrelationphrase ON labeled_triples(relation,phrase)";
	String indexModelPhrase = "CREATE INDEX IF NOT EXISTS indexmodelphrase ON labeled_triples(phrase)";
	String indexModelTypesPhrase = "CREATE INDEX IF NOT EXISTS indexmodeltypesphrase ON labeled_triples(type_subject, type_object, relation, phrase)";
	String indexUnlabeled = "CREATE INDEX IF NOT EXISTS indexunlabeled ON unlabeled_triples(type, phrase, type_subject, type_object)";
	String indexUnlabeledPhrase = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase ON unlabeled_triples(phrase, type_subject, type_object)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(indexModelRelationPhrase);
	    stmt.executeUpdate(indexModelPhrase);
	    stmt.executeUpdate(indexModelTypesPhrase);
	    stmt.executeUpdate(indexUnlabeled);
	    stmt.executeUpdate(indexUnlabeledPhrase);
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
	System.out.println("Done!");

    }

    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public void insertLabeledTriple(WikiTriple triple, String relation){
	String insert = "INSERT INTO labeled_triples VALUES(?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhrase());
	    stmt.setString(3, triple.getSubject());
	    stmt.setString(4, triple.getObject());
	    stmt.setString(5, triple.getSubjectType());
	    stmt.setString(6, triple.getObjectType());
	    stmt.setString(7, relation);
	    stmt.execute();
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public void updateModelStats(String phrase, String subjectType, String objectType, String relation, int count){
	String update = "COALESCE((SELECT count FROM model_stats WHERE phrase=? AND type_subject=? AND type_object=? AND relation=?), 0) + ?";
	String insert = "INSERT OR REPLACE INTO model_stats(phrase,type_subject,type_object,relation,count) VALUES(?,?,?,?," + update + ")";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, phrase);
	    stmt.setString(5, phrase);
	    stmt.setString(2, subjectType);
	    stmt.setString(6, subjectType);
	    stmt.setString(3, objectType);
	    stmt.setString(7, objectType);
	    stmt.setString(4, relation);
	    stmt.setString(8, relation);
	    stmt.setInt(9, count);
	    stmt.execute();
	}catch(SQLException e){
	    e.printStackTrace();
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public void insertUnlabeledTriple(WikiTriple triple){
	String insert = "INSERT INTO unlabeled_triples VALUES(?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhrase());
	    stmt.setString(3, triple.getSubject());
	    stmt.setString(4, triple.getObject());
	    stmt.setString(5, triple.getSubjectType());
	    stmt.setString(6, triple.getObjectType());
	    stmt.setString(7, triple.getType().name());
	    stmt.execute();
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public void insertMVList(WikiMVL list){
	String insert = "INSERT INTO mvl_collection VALUES(?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, list.getCode());
	    stmt.setString(2, list.getWikid());
	    stmt.setString(3, list.getSection());
	    stmt.setString(4, StringUtils.join(list.getListWikid(), ","));
	    stmt.execute();
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }


    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public void insertNovelFact(WikiTriple triple, String relation){
	String insert = "INSERT INTO novel_facts VALUES(?,?,?,?,?,?,?,?,?)";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(insert)){
	    stmt.setString(1, triple.getWikid());
	    stmt.setString(2, triple.getPhrase());
	    stmt.setString(3, triple.getSubject());
	    stmt.setString(4, triple.getObject());
	    stmt.setString(5, triple.getWikiSubject());
	    stmt.setString(6, triple.getWikiObject());
	    stmt.setString(7, triple.getSubjectType());
	    stmt.setString(8, triple.getObjectType());
	    stmt.setString(9, relation);
	    stmt.execute();
	}catch(SQLException e){
	    try {
		connection.rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**************************************************************/


    /**
     * @return the connection
     */
    public Connection getConnection() {
	return connection;
    }

    /**
     * @return the connection
     */
    public void closeConnection() {
	try {
	    connection.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }


}
