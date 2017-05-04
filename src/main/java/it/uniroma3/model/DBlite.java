package it.uniroma3.util.index;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import edu.stanford.nlp.util.StringUtils;
import it.uniroma3.triples.WikiMVL;
import it.uniroma3.triples.WikiTriple;
import it.uniroma3.triples.WikiTriple.TType;
import it.uniroma3.util.Pair;
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
    public DBlite(){
	this.connection = obtainConnection();
    }

    /**
     * 
     * @return
     */
    public Connection obtainConnection(){
	String sDriverName = "org.sqlite.JDBC";
	Connection conn = null;
	try {
	    Class.forName(sDriverName);
	    String sTempDb = "lector.db";
	    String sJdbc = "jdbc:sqlite";
	    String sDbUrl = sJdbc + ":" + sTempDb;
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
	createLabeledTriples();
	createUnlabeledTriples();
	createMVLCollection();
	createMVLFactsCollection();
	createModelStats();
    }
    
    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public List<Pair<WikiTriple, String>> selectLabeledTriple(){
	List<Pair<WikiTriple, String>> result = new LinkedList<Pair<WikiTriple, String>>();
        String select = "SELECT * from labeled_triples";
	try (Statement stmt = this.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(select)){
		while(rs.next()){
		    String wikid = rs.getString("wikid");
                    String phrase = rs.getString("phrase");
                    String subject = rs.getString("subject");
                    String subjectType = rs.getString("type_subject");
                    String object = rs.getString("object");
                    String objectType = rs.getString("type_object");
                    String relation = rs.getString("relation");
                    WikiTriple t = new WikiTriple(wikid, phrase, subject, object, subjectType, objectType, TType.JOINABLE.name());
                    result.add(Pair.make(t, relation));
                }
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return result;
    }
    
    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public List<WikiTriple> selectUnlabeledTripleWithCondition(){
	List<WikiTriple> result = new LinkedList<WikiTriple>();
        String select = "SELECT * from unlabeled_triples WHERE type=?";
	try (PreparedStatement stmt = this.getConnection().prepareStatement(select)){
	    stmt.setString(1, "PARTIALNER");
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String wikid = rs.getString("wikid");
                    String phrase = rs.getString("phrase");
                    String subject = rs.getString("subject");
                    String object = rs.getString("object");
                    String subjectType = rs.getString("type_subject");
                    String objectType = rs.getString("type_object");
                    String articleType = rs.getString("type");
                    WikiTriple t = new WikiTriple(wikid, phrase, subject, object, subjectType, objectType, articleType);
                    result.add(t);
                }
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return result;
    }
    
    /**
     * 
     * @param phrase
     * @param subject_type
     * @param object_type
     * @param relation
     */
    public List<WikiTriple> selectUnlabeledTriple(){
	List<WikiTriple> result = new LinkedList<WikiTriple>();
        String select = "SELECT * from unlabeled_triples";
	try (Statement stmt = this.getConnection().createStatement()){
	    try (ResultSet rs = stmt.executeQuery(select)){
		while(rs.next()){
		    String wikid = rs.getString("wikid");
                    String phrase = rs.getString("phrase");
                    String subject = rs.getString("subject");
                    String object = rs.getString("object");
                    String subjectType = rs.getString("type_subject");
                    String objectType = rs.getString("type_object");
                    String articleType = rs.getString("type");
                    WikiTriple t = new WikiTriple(wikid, phrase, subject, object, subjectType, objectType, articleType);
                    result.add(t);
                }
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return result;
    }
    
    /**************************************************************/


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
    
    /**************************************************************/

    /**
     * 
     * @param table_name
     */
    private void createLabeledTriples(){
	String drop = "DROP TABLE IF EXISTS labeled_triples";
	String create = "CREATE TABLE labeled_triples(wikid text, phrase text, subject text, object text, type_subject text, type_object text, relation text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(drop);
	    stmt.executeUpdate(create);
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
     * @param table_name
     */
    private void createModelStats(){
	String drop = "DROP TABLE IF EXISTS model_stats";
	String create = "CREATE TABLE model_stats(phrase text, type_subject text, type_object text, relation text, count int, PRIMARY KEY (phrase, type_subject, type_object, relation))";
	String index1 = "CREATE INDEX indexphrase ON model_stats(phrase)";
	String index2 = "CREATE INDEX indextypesubject ON model_stats(type_subject)";
	String index3 = "CREATE INDEX indextypeobject ON model_stats(type_object)";
	String index4 = "CREATE INDEX indexrelation ON model_stats(relation)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(drop);
	    stmt.executeUpdate(create);
	    stmt.executeUpdate(index1);
	    stmt.executeUpdate(index2);
	    stmt.executeUpdate(index3);
	    stmt.executeUpdate(index4);
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
     * @param table_name
     */
    private void createUnlabeledTriples(){
	String drop = "DROP TABLE IF EXISTS unlabeled_triples";
	String create = "CREATE TABLE unlabeled_triples(wikid text, phrase text, subject text, object text, type_subject text, type_object text, type text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(drop);
	    stmt.executeUpdate(create);
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
     * @param table_name
     */
    private void createMVLCollection(){
	String drop = "DROP TABLE IF EXISTS mvl_collection";
	String create = "CREATE TABLE mvl_collection(code text, section text, wikid text, list text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(drop);
	    stmt.executeUpdate(create);
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
     * @param table_name
     */
    private void createMVLFactsCollection(){
	String drop = "DROP TABLE IF EXISTS mvl_triples";
	String create = "CREATE TABLE mvl_triples(wikid text, subject text, phrase text, object text)";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(drop);
	    stmt.executeUpdate(create);
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
