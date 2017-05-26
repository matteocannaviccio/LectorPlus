package it.uniroma3.model.evaluation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.db.QueryDB;
/**
 * 
 * @author matteo
 *
 */
public class DBCrossValidation extends DB{
    // print the computation
    protected static boolean verbose = true;

    private QueryDB querydb;
    private String dbModelName;

    /**
     * A DBCrossValidation is built on top of the DBModel.
     * It has a name and a number of cross-validation parts.
     */
    public DBCrossValidation(String dbname, DBModel dbmodel, int nParts){
	super(dbname);
	this.dbModelName = dbmodel.getDbname();
	this.querydb = new QueryDB(dbmodel);
	//createDB(nParts);
    }

    /**
     * 
     */
    protected void createDB(int nParts){
	// first of all, we create the schema
	if(verbose)
	    System.out.println("-> creating schema of a DBCrossValidation with " + nParts + " parts.");
	createSchema(nParts);

	// then we insert all the unlabeled, only once
	if(verbose)
	    System.out.println("-> transferring unlabeled.");
	batchInsertUnlabeledTriple();

	// then, we randomly split the labeled triples in N parts
	if(verbose)
	    System.out.println("-> shuffling & splitting labeled triples");
	List<Pair<WikiTriple, String>> labeled_facts = querydb.selectAllLabeledTriples();
	int size = labeled_facts.size() / nParts;
	List<List<Pair<WikiTriple, String>>> partitions = split(labeled_facts, nParts);
	labeled_facts.clear();

	// and, for each one, we insert the N-1 labeled_triples and the evaluation_triples part
	if(verbose)
	    System.out.println("-> creating cross-validation \t n. parts:" + nParts + "\tof avg. size:" + size);
	for (int n = 0; n<nParts; n++){
	    System.out.println("\t---> cross-validation n.: " + n + "... ");
	    for (int j = 0; j<nParts; j++){
		if (j==n)
		    batchInsertTriples("cv_evaluation_triples_" + n, partitions.get(j));
		else
		    batchInsertTriples("cv_labeled_triples_" + n, partitions.get(j));
	    }
	}
    }

    /**
     * 
     */
    private void createUnlabeled(){
	// first, the unlabeled_triple is unique
	String dropUnlabeled = "DROP TABLE IF EXISTS unlabeled_triples";
	String createUnlabeled = "CREATE TABLE unlabeled_triples("
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
		+ "type_object text)";

	String indexUnlabeledPhrase = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
		+ "ON unlabeled_triples(phrase_placeholder, type_subject, type_object)";

	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropUnlabeled);
	    stmt.executeUpdate(createUnlabeled);
	    stmt.executeUpdate(indexUnlabeledPhrase);
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
     * @param table_name
     */
    private void createLabeled(String table_name){
	String dropLabeled = "DROP TABLE IF EXISTS "+table_name;
	String createLabeled = "CREATE TABLE "+table_name+"("
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
	String indexModelRelationPhrase = "CREATE INDEX IF NOT EXISTS indexmodelrelationphrase "
		+ "ON "+table_name+"(relation, phrase_placeholder)";
	String indexModelPhrase = "CREATE INDEX IF NOT EXISTS indexmodelphrase "
		+ "ON "+table_name+"(phrase_placeholder)";
	String indexModelTypesPhrase = "CREATE INDEX IF NOT EXISTS indexmodeltypesphrase "
		+ "ON "+table_name+"(type_subject, type_object, relation, phrase_placeholder)";

	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.executeUpdate(dropLabeled);
	    stmt.executeUpdate(createLabeled);
	    stmt.executeUpdate(indexModelRelationPhrase);
	    stmt.executeUpdate(indexModelPhrase);
	    stmt.executeUpdate(indexModelTypesPhrase);
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
    private void createSchema(int nParts){
	// first, the unlabeled_triple is unique
	createUnlabeled();
	// second, we create several labeled triples and evaluation triples,
	// one foe each part of the cross-validation
	for (int n = 0; n<nParts; n++){
	    createLabeled("CV_labeled_triples_" + n);
	    createLabeled("CV_evaluation_triples_" + n);
	}
    }

    /**
     * This is the schema of unlabeled_triples.
     * 
     * @param triple
     */
    public void batchInsertUnlabeledTriple(){
	String attach = "ATTACH '" + dbModelName + "' AS modelDB";
	String insertAll = "INSERT INTO unlabeled_triples SELECT * FROM modelDB.unlabeled_triples";
	try (Statement stmt = this.getConnection().createStatement()){
	    stmt.execute(attach);
	    stmt.execute(insertAll);
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
     * @param table_name
     * @param labeled
     */
    private void batchInsertTriples(String table_name, List<Pair<WikiTriple, String>> labeled){
	String insert = "INSERT INTO "+table_name+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
	try{
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement pstmt = this.getConnection().prepareStatement(insert);
	    for (Pair<WikiTriple, String> wikiTriple : labeled){
		pstmt.setString(1, wikiTriple.key.getWikid());
		pstmt.setString(2, wikiTriple.key.getPhraseOriginal());
		pstmt.setString(3, wikiTriple.key.getPhrasePlaceholders());
		pstmt.setString(4, wikiTriple.key.getPre());
		pstmt.setString(5, wikiTriple.key.getPost());
		pstmt.setString(6, wikiTriple.key.getSubject());
		pstmt.setString(7, wikiTriple.key.getWikiSubject());
		pstmt.setString(8, wikiTriple.key.getSubjectType());
		pstmt.setString(9, wikiTriple.key.getObject());
		pstmt.setString(10, wikiTriple.key.getWikiObject());
		pstmt.setString(11, wikiTriple.key.getObjectType());
		pstmt.setString(12, wikiTriple.value);
		pstmt.addBatch();
	    }
	    pstmt.executeBatch();
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
     * Split a list of T objects in SIZE random parts.
     * 
     * @param list
     * @param size
     * @return
     */
    public static <T> List<List<T>> split(List<T> list, int size){
	Collections.shuffle(list);
	List<List<T>> result = new ArrayList<List<T>>(size);
	for (int i = 0; i < size; i++) {
	    result.add(new ArrayList<T>());
	}
	int index = 0;
	for (T t : list) {
	    result.get(index).add(t);
	    index = (index + 1) % size;
	}
	return result;
    }
}

