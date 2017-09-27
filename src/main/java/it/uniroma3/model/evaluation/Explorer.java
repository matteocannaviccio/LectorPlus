package it.uniroma3.model.evaluation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.uniroma3.main.util.CounterMap;
import it.uniroma3.model.db.DB;

public class Explorer {

    private DB model;
    private DB explorer;
    private Set<String> relations = new HashSet<String>();

    public Explorer(DB model){
	this.model = model;
	this.explorer = new DB("/Users/matteo/Desktop/explorer.db", false);
    }

    /**
     * 
     * @param modelTableName
     */
    public void createDB(){
	String drop1 = "DROP TABLE IF EXISTS phrases";
	String create1 = "CREATE TABLE phrases("
		+ "phrase text, "
		+ "type_subject text, "
		+ "type_object text, "
		+ "relation text, "
		+ "occurrences int)";
	String createIndex1 = "CREATE UNIQUE INDEX IF NOT EXISTS indexPhrases "
		+ "ON phrases(phrase, type_subject, type_object, relation)";

	String drop2 = "DROP TABLE IF EXISTS intersection";
	String create2 = "CREATE TABLE intersection("
		+ "phrase_left text, "
		+ "phrase_right text, "
		+ "type_subject text, "
		+ "type_object text, "
		+ "relation text, "
		+ "occurrences int)";
	String createIndex2 = "CREATE UNIQUE INDEX IF NOT EXISTS indexIntersection "
		+ "ON intersection(phrase_left, phrase_right, type_subject, type_object, relation)";

	try (Statement stmt = this.explorer.getConnection().createStatement()){
	    stmt.executeUpdate(drop1);
	    stmt.executeUpdate(create1);
	    stmt.executeUpdate(createIndex1);
	    stmt.executeUpdate(drop2);
	    stmt.executeUpdate(create2);
	    stmt.executeUpdate(createIndex2);

	}catch(SQLException e){
	    try {
		this.explorer.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * Returns phrases, types, relations and their counts from the Model Table. 
     * 
     * 		[Settlement] located in [Settlement] --- isPartOf --- 675 K
     * 		[Settlement] surrounded by [City] --- isPartOf --- 342 K
     * 
     * if includeNone is True:
     * 
     * 		[City] is in [Country] --- NONE --- 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> retrieveEvidence(String tableName, boolean includeNone){
	Map<String, CounterMap<String>> evidence = new HashMap<String, CounterMap<String>>();
	int c = 0;
	String query = "SELECT phrase_placeholder, relation, type_subject, type_object FROM " + tableName;
	try (PreparedStatement stmt = this.model.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    c += 1;
		    if ((c % 1000000) == 0)
			System.out.println(c);
		    String phrase_placeholder = rs.getString(1);
		    String relation = rs.getString(2);
		    String type_subject = rs.getString(3);
		    String type_object = rs.getString(4);
		    String typedPhrase = type_subject + "\t" + phrase_placeholder + "\t" + type_object;
		    if (!relation.equals("NONE"))
			relations.add(relation);
		    if (!phrase_placeholder.equals("") && (includeNone || !relation.equals("NONE"))){
			if (!evidence.containsKey(typedPhrase))
			    evidence.put(typedPhrase, new CounterMap<String>());
			evidence.get(typedPhrase).add(relation);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return evidence;
    }

    /**
     * 
     * @param list
     */
    public void batchInsertPhraseRelationCount(Map<String, CounterMap<String>> evidence){
	int max = evidence.size();
	int relCount = 0;
	String insert = "INSERT INTO phrases VALUES(?,?,?,?,?)";
	try {
	    this.explorer.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.explorer.getConnection().prepareStatement(insert);
	    for (Map.Entry<String, CounterMap<String>> entry : evidence.entrySet()){
		relCount +=1;
		System.out.println(relCount +"/"+ max);
		for (Map.Entry<String, Integer> relation : entry.getValue().entrySet()){
		    String type_subject = entry.getKey().split("\t")[0];
		    String phrase = entry.getKey().split("\t")[1];
		    String type_object = entry.getKey().split("\t")[2];
		    stmt.setString(1, phrase);
		    stmt.setString(2, type_subject);
		    stmt.setString(3, type_object);
		    stmt.setString(4, relation.getKey());
		    stmt.setInt(5, relation.getValue());
		    if (relation.getValue() > 5){
			stmt.addBatch();
		    }
		}
	    }	    
	    stmt.executeBatch();
	    this.explorer.getConnection().commit();
	}catch(SQLException e){
	    try {
		this.explorer.getConnection().rollback();
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
    public void batchInsertIntersectionCount(List<String> intersections){
	String insert = "INSERT INTO intersection VALUES(?,?,?,?,?,?)";
	try {
	    this.explorer.getConnection().setAutoCommit(false);
	    PreparedStatement stmt = this.explorer.getConnection().prepareStatement(insert);
	    for (String entry : intersections){
		String typedPhrase1 = entry.split("\\|\\|\\|")[0];
		String typedPhrase2 = entry.split("\\|\\|\\|")[1];
		
		String type_subject1 = typedPhrase1.split("\t")[0];
		String phrase1 = typedPhrase1.split("\t")[1];
		String type_object1 = typedPhrase1.split("\t")[2];
		//String type_subject2 = typedPhrase2.split("\t")[0];
		String phrase2 = typedPhrase2.split("\t")[1];
		//String type_object2 = typedPhrase2.split("\t")[2];
		
		String relation = entry.split("\\|\\|\\|")[2];
		int occ = Integer.parseInt(entry.split("\\|\\|\\|")[3]);
		stmt.setString(1, phrase1);
		stmt.setString(2, phrase2);
		stmt.setString(3, type_subject1);
		stmt.setString(4, type_object1);
		stmt.setString(5, relation);
		stmt.setInt(6, occ);
		stmt.addBatch();
	    }	    
	    stmt.executeBatch();
	    this.explorer.getConnection().commit();
	}catch(SQLException e){
	    try {
		this.explorer.getConnection().rollback();
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	    e.printStackTrace();
	}
    }

    /**
     * Returns phrases, types, relations and their counts from the Model Table. 
     * 
     * 		[Settlement] located in [Settlement] --- isPartOf --- 675 K
     * 		[Settlement] surrounded by [City] --- isPartOf --- 342 K
     * 
     * if includeNone is True:
     * 
     * 		[City] is in [Country] --- NONE --- 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public int getEntitiesInCommon(String typedphrase1, String typedphrase2, String relation){
	String subjectType1 = typedphrase1.split("\t")[0];
	String phrase1 = typedphrase1.split("\t")[1];
	String objectType1 = typedphrase1.split("\t")[2];
	String subjectType2 = typedphrase2.split("\t")[0];
	String phrase2 = typedphrase2.split("\t")[1];
	String objectType2 = typedphrase2.split("\t")[2];
	int occurrences = 0;

	if (subjectType1.equals(subjectType2) && objectType1.equals(objectType2)){
	    String query = 
		    "SELECT count(*) FROM ("
			    + "SELECT count(*) as C1 FROM("
			    + "SELECT wiki_subject, wiki_object, phrase_placeholder from labeled_triples "
			    + "WHERE (type_subject = ? AND type_object = ?) AND (phrase_placeholder = ? OR phrase_placeholder = ?) AND relation = ? "
			    + "GROUP BY wiki_subject, wiki_object, phrase_placeholder) "
			    + "GROUP BY wiki_subject, wiki_object "
			    + "HAVING C1 > 1)";

	    try (PreparedStatement stmt = this.model.getConnection().prepareStatement(query)){
		stmt.setString(1, subjectType1);
		stmt.setString(2, objectType1);
		stmt.setString(3, phrase1);
		stmt.setString(4, phrase2);
		stmt.setString(5, relation);

		try (ResultSet rs = stmt.executeQuery()){
		    while(rs.next()){
			occurrences = rs.getInt(1);
		    }
		}

	    }catch(SQLException e){
		e.printStackTrace();
	    }
	}

	return occurrences;
    }

    /**
     * 
     * @return
     */
    private Map<String, List<String>> getRelationToPhrases(){
	Map<String, List<String>> relation2phrases = new HashMap<String, List<String>>();
	String query = "SELECT phrase, type_subject, type_object, relation FROM phrases";
	try (PreparedStatement stmt = this.explorer.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    String type_subject = rs.getString(2);
		    String type_object = rs.getString(3);
		    String typed_phrase = type_subject + "\t" + phrase + "\t" + type_object;
		    String relation = rs.getString(4);
		    if (!relation.equals("NONE")){
			if (!relation2phrases.containsKey(relation))
			    relation2phrases.put(relation, new LinkedList<String>());
			relation2phrases.get(relation).add(typed_phrase);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return relation2phrases;
    }

    /**
     * 
     * @return
     */
    private Map<String, List<String>> getRelationToPairOfPhrases(){
	Map<String, List<String>> rel2listofphrases = new HashMap<String, List<String>>();
	Map<String, List<String>> rel2phrases = getRelationToPhrases();
	int max = rel2phrases.size();
	int relCount = 0;
	for (Map.Entry<String, List<String>> r2p : rel2phrases.entrySet()){
	    String relation = r2p.getKey();
	    relCount +=1;
	    System.out.println(relCount +"/"+ max);
	    if (!rel2listofphrases.containsKey(relation))
		rel2listofphrases.put(relation, new LinkedList<String>());
	    List<String> tmp = new ArrayList<String>(r2p.getValue());
	    for (String typedphrase1 : r2p.getValue()){
		tmp.remove(typedphrase1);
		for (String typedphrase2 : tmp){
		    String subjectType1 = typedphrase1.split("\t")[0];
		    String phrase1 = typedphrase1.split("\t")[1];
		    String objectType1 = typedphrase1.split("\t")[2];
		    String subjectType2 = typedphrase2.split("\t")[0];
		    String phrase2 = typedphrase2.split("\t")[1];
		    String objectType2 = typedphrase2.split("\t")[2]; 
		    if (!phrase1.equals(phrase2) && subjectType1.equals(subjectType2) && objectType1.equals(objectType2)){
			rel2listofphrases.get(relation).add(typedphrase1 + "|||" + typedphrase2);
		    }
		}


	    }
	}
	return rel2listofphrases;
    }

    /**
     * 
     * @param rel2pairOfPhrases
     * @return
     */
    private List<String> getAllEntriesInt(Map<String, List<String>> rel2pairOfPhrases) {
	List<String> output = new LinkedList<String>();
	int maxSize = 5000;

	int max = rel2pairOfPhrases.size();
	int relCount = 0;
	for (Map.Entry<String, List<String>> entry : rel2pairOfPhrases.entrySet()){
	    relCount +=1;
	    System.out.println(relCount +"/"+ max + "  relation: " + entry.getKey() + "   pairs of phrases: " + entry.getValue().size());
	    String relation = entry.getKey();
	    for (String pair : entry.getValue()){
		String typedPhrase1 = pair.split("\\|\\|\\|")[0];
		String typedPhrase2 = pair.split("\\|\\|\\|")[1];
		int en_com = getEntitiesInCommon(typedPhrase1, typedPhrase2, relation);
		output.add(typedPhrase1 + "|||" + typedPhrase2 + "|||" + relation + "|||" + en_com);
	    }
	    if (output.size() > maxSize){
		batchInsertIntersectionCount(output);
		output.clear();
	    }
	}
	return output;
    }




    /**
     * 
     * @param args
     */
    public static void main(String[] args){
	DB db = new DB("/Users/matteo/Desktop/data/lector/en/en_model.db", false);
	Explorer exp = new Explorer(db);
	exp.createDB();
	
	// insert phrases
	//phrases.putAll(exp.retrieveEvidence("unlabeled_triples", true));
	exp.batchInsertPhraseRelationCount(exp.retrieveEvidence("labeled_triples", true));
	
	// insert instersections
	Map<String, List<String>> rel2pairOfPhrases = exp.getRelationToPairOfPhrases();
	exp.getAllEntriesInt(rel2pairOfPhrases);
    }


}
