package it.uniroma3.model.evaluation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.util.Pair;
import it.uniroma3.model.DB;
import it.uniroma3.model.db.CRUD;
import it.uniroma3.model.db.DBModel;
/**
 * 
 * @author matteo
 *
 */
public class DBCrossValidation extends DB{
    // print the computation
    protected static boolean verbose = true;
    private String dbModelName;
    private Map<String, List<Pair<WikiTriple, String>>> all_labeled_facts;

    /**
     * A DBCrossValidation is built on top of the DBModel.
     * This constructor is used when a DBCrossValidation already exists.
     */
    public DBCrossValidation(String dbname){
	super(dbname);
	all_labeled_facts = new HashMap<String, List<Pair<WikiTriple, String>>>();
    }

    /**
     * A DBCrossValidation is built on top of the DBModel.
     * It has a name and a number of cross-validation parts.
     * This constructor is used when a DBCrossValidation has to be created.
     */
    public DBCrossValidation(String dbname, DBModel dbmodel, int nParts){
	super(dbname);
	this.dbModelName = dbmodel.getDbname();
	createDB(nParts, new CRUD(dbmodel));
    }

    /**
     * 
     */
    protected void createDB(int nParts, CRUD querydb){
	System.out.print("\t-> creating DBCrossValidation with " + nParts + " parts, ");

	// first of all, we create the schema
	createSchema(nParts);

	// then we insert all the unlabeled, only once
	batchInsertUnlabeledTriple();

	all_labeled_facts = querydb.selectAllLabeledPairs();
	List<String> triples = new ArrayList<String>(all_labeled_facts.keySet());
	int size = all_labeled_facts.size() / nParts;
	List<List<String>> partitions = split(triples, nParts);

	System.out.println("of avg. size: " + size);

	for (int n = 0; n<nParts; n++){
	    for (int j = 0; j<nParts; j++){
		if (j==n)
		    batchInsertTriples("cv_evaluation_triples_" + n, partitions.get(j), false);
		else
		    batchInsertTriples("cv_labeled_triples_" + n, partitions.get(j), true);
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
		+ "section text, "
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
		+ "section text, "
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
    private void batchInsertTriples(String table_name, List<String> pairs, boolean insertDuplicatePairs){
	String insert = "INSERT INTO "+table_name+" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	try{
	    this.getConnection().setAutoCommit(false);
	    PreparedStatement pstmt = this.getConnection().prepareStatement(insert);
	    for (String pair : pairs){
		if (insertDuplicatePairs){
		    for (Pair<WikiTriple, String> labeled_triple :  all_labeled_facts.get(pair)){
			pstmt.setString(1, labeled_triple.key.getWikid());
			pstmt.setString(2, labeled_triple.key.getSection());
			pstmt.setString(3, labeled_triple.key.getPhraseOriginal());
			pstmt.setString(4, labeled_triple.key.getPhrasePlaceholders());
			pstmt.setString(5, labeled_triple.key.getPre());
			pstmt.setString(6, labeled_triple.key.getPost());
			pstmt.setString(7, labeled_triple.key.getSubject());
			pstmt.setString(8, labeled_triple.key.getWikiSubject());
			pstmt.setString(9, labeled_triple.key.getSubjectType());
			pstmt.setString(10, labeled_triple.key.getObject());
			pstmt.setString(11, labeled_triple.key.getWikiObject());
			pstmt.setString(12, labeled_triple.key.getObjectType());
			pstmt.setString(13, labeled_triple.value);
			pstmt.addBatch();
		    }
		}else{
		    /*
		     * in this case (evaluation triples) we do not want to insert all the possible
		     * pairs triple - label because at query time we check the correctness of the
		     * prediction using DBpedia and not the relation in the triple (12, labeled_triple.value)
		     */
		    Pair<WikiTriple, String> labeled_triple = all_labeled_facts.get(pair).get(0);
		    pstmt.setString(1, labeled_triple.key.getWikid());
		    pstmt.setString(2, labeled_triple.key.getSection());
		    pstmt.setString(3, labeled_triple.key.getPhraseOriginal());
		    pstmt.setString(4, labeled_triple.key.getPhrasePlaceholders());
		    pstmt.setString(5, labeled_triple.key.getPre());
		    pstmt.setString(6, labeled_triple.key.getPost());
		    pstmt.setString(7, labeled_triple.key.getSubject());
		    pstmt.setString(8, labeled_triple.key.getWikiSubject());
		    pstmt.setString(9, labeled_triple.key.getSubjectType());
		    pstmt.setString(10, labeled_triple.key.getObject());
		    pstmt.setString(11, labeled_triple.key.getWikiObject());
		    pstmt.setString(12, labeled_triple.key.getObjectType());
		    pstmt.setString(13, labeled_triple.value);
		    pstmt.addBatch();
		}
	    }
	    pstmt.executeBatch();
	    this.getConnection().commit();
	    this.getConnection().setAutoCommit(true);
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

