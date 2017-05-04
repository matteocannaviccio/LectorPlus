package it.uniroma3.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @author matteo
 *
 */
public class QueryDB {

    protected DBlite db;

    /**
     * 
     * @param db
     */
    public QueryDB(DBlite db){
	this.db = db;
	db.createNecessaryIndexes();
    }

    /**
     * Returns the sum of the (integer) values in a map.
     * 
     * @param map
     * @return
     */
    protected int countValues(Map<String, Integer> map){
	int values = 0;
	for (int c : map.values())
	    values+=c;
	return values;
    }

    /**
     * Returns typed relations and their counts,
     * that are present at least N times.
     * 
     * 		------------------------------------------------
     * 		   	    LABELED TYPED RELATIONS
     * 		------------------------------------------------
     * 		[Settlement] ### isPartOf ### [Settlement]	: 675 K
     * 		[Settlement] ### isPartOf ### [City]		: 342 K
     * 		[City] ### country ### [Country]		: 112 K
     * 		...
     * 
     * @param min the minimum number of times
     * @return
     */
    protected Map<String, Integer> getTypedRelationsCount(int min){
	Map<String, Integer> typedRelationCounts = new HashMap<String, Integer>();
	String query = 
		"SELECT relation, type_subject, type_object, COUNT(*) as C1 FROM labeled_triples "
			+ "GROUP BY relation, type_subject, type_object "
			+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    stmt.setInt(1, min);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relationRet = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    int count = Integer.parseInt(rs.getString(4));
		    typedRelationCounts.put(ts + "\t" + relationRet + "\t"  + to, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return typedRelationCounts;
    }
    
    /**
     * Returns phrases, types and relations all together,
     * that are present at least N times with their counts.
     * 
     * 		------------------------------------------------
     * 		   	    LABELED TYPED PHRASES RELATIONS
     * 		------------------------------------------------
     * 		located in ### [Settlement] ### isPartOf ### [Settlement]	: 675 K
     * 		surrounded by ### [Settlement] ### isPartOf ### [City]		: 342 K
     * 		is in ### [City] ### country ### [Country]			: 112 K
     * 		...
     * 
     * @param min the minimum number of times
     * @return
     */
    protected Map<String, Integer> getRelationTypesPhraseCounts(int min){
	Map<String, Integer> typedPhraseRelationCounts = new HashMap<String, Integer>();
	String query = 
		"SELECT phrase, relation, type_subject, type_object, COUNT(*) as C1 FROM labeled_triples "
			+ "GROUP BY phrase,relation,type_subject,type_object "
			+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    stmt.setInt(1, min);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    String relation = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    int count = Integer.parseInt(rs.getString(5));
		    typedPhraseRelationCounts.put(phrase + "###" + ts + "###" + relation + "###"  + to, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return typedPhraseRelationCounts;
    }
    
    

    /**
     * Returns relations and their counts,
     * that are present at least N times.
     * 		---------------------	
     * 		   LABELED RELATIONS
     * 		---------------------
     * 		isPartOf 	: 974 K
     * 		country 	: 926 K
     * 		team 		: 212 K
     * 		...
     * 
     * @param minRelFreq the minimum number of times
     * @return
     */
    protected Map<String, Integer> getCleanRelationCount(int min){
	Map<String, Integer> cacheRelationCounts = new HashMap<String, Integer>();
	String query = 
		"SELECT relation, COUNT(*) as C1 from labeled_triples "
			+ "GROUP BY relation " 
			+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    stmt.setInt(1, min);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    int count = Integer.parseInt(rs.getString(2));
		    cacheRelationCounts.put(relation, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return cacheRelationCounts;
    }

    /**
     * Returns the phrases (from the ones labeled with a relation),
     * that are present at least N times.
     * 
     * 		---------------------	
     * 		   LABELED PHRASES
     * 		---------------------
     * 		was born in 	: 334
     * 		plays for 	: 234
     * 		directed by 	: 212
     * 		...
     * 
     * @param minFrequency the minimum number of times
     * @return
     */
    protected Map<String, Integer> getLabeledPhrasesCount(int min){
	Map<String, Integer> phrasesCount = new HashMap<String, Integer>();
	String query = 
		"SELECT phrase,count(*) as C1 FROM labeled_triples "
			+ "GROUP BY phrase "
			+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    stmt.setInt(1, min);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    int count = Integer.parseInt(rs.getString(2));
		    phrasesCount.put(phrase, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrasesCount;
    }

    /**
     * Returns the "typed" phrases (from the ones labeled with a relation),
     * that are present at least N times.
     * 		---------------------------------------------	
     * 			     LABELED TYPED PHRASES
     * 		---------------------------------------------
     * 		[Person] was born in [Settlement] 	: 334
     * 		[Athlete] plays for [Team] 		: 234
     * 		[Movie] directed by [FilmDirector] 	: 212
     * 		...
     * 
     * @param minFrequency the minimum number of times
     * @return
     */
    protected Map<String, Integer> getLabeledTypedPhrasesCount(int min){
	Map<String, Integer> phrasesTypesCount = new HashMap<String, Integer>();
	String phrasesTypesQuery = 
		"SELECT phrase, type_subject, type_object, count(*) as C1 FROM labeled_triples "
			+ "GROUP BY phrase, type_subject, type_object "
			+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(phrasesTypesQuery)){
	    stmt.setInt(1, min);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    int count = Integer.parseInt(rs.getString(4));
		    phrasesTypesCount.put(ts+"\t"+phrase+"\t"+to, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrasesTypesCount;
    }

    /**
     * Returns the count of unlabeled phrases from the set of phrases in input. 
     * 		----------------------	
     * 		  UN-LABELED PHRASES
     * 		----------------------
     * 		was born in 	: 34224
     * 		plays for 	: 5424
     * 		directed by 	: 3432
     *		...
     * 
     * @param phraseLabeled the set of phrases to consider
     * @return
     */
    protected Map<String, Integer> getUnlabeledPhrasesCount(Set<String> phraseLabeled){
	Map<String, Integer> phrasesCount = new HashMap<String, Integer>();
	String phrasesCountQuery = 
		"SELECT phrase,count(*) as C1 FROM unlabeled_triples "
		//+ "WHERE type=\"JOINABLE\" "
		+ "GROUP BY phrase";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(phrasesCountQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    int count = Integer.parseInt(rs.getString(2));
		    if (phraseLabeled.contains(phrase))
			phrasesCount.put(phrase, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrasesCount;
    }

    /**
     * Returns the count of unlabeled typed phrases from the set of typed phrases in input. 
     *       	---------------------------------------------
     * 		         UN-LABELED TYPED PHRASES
     * 		---------------------------------------------
     * 		[Person] was born in [Settlement] 	: 857
     * 		[Athlete] plays for [Team] 		: 544
     * 		[Movie] directed by [FilmDirector] 	: 344
     * 		...
     * 
     * @param typedPhrasesLabeled the set of typed phrases to consider
     * @return
     */
    protected Map<String, Integer> getUnlabeledTypedPhrasesCount(Set<String> typedPhrasesLabeled){
	Map<String, Integer> phrasesCount = new HashMap<String, Integer>();
	String allRelationTypesLabeledTriplesQuery = 
		"SELECT phrase, type_subject, type_object, count(*) as C1 FROM unlabeled_triples "
			+ "WHERE type=\"JOINABLE\" "
			+ "GROUP BY phrase, type_subject, type_object";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(allRelationTypesLabeledTriplesQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    int count = Integer.parseInt(rs.getString(4));
		    phrase = ts+"\t"+phrase+"\t"+to;
		    if (typedPhrasesLabeled.contains(phrase))
			phrasesCount.put(phrase, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrasesCount;
    }

    /**
     * Returns the counts of phraseLabeled assigned to each typed relation.
     *      	--------------------------------------
     * 		          LABELED RELATIONS
     * 	    	--------------------------------------
     * 	    	birthPlace 	--> was born in : 334
     *      	team		--> plays for : 234
     *      	directed	--> directed by : 212
     * 		...
     * 
     * @param phraseLabeled
     * @return
     */
    protected Map<String, Map<String, Integer>> getRelationPhrasesCount(Set<String> phraseLabeled){
	Map<String, Map<String, Integer>> relation2phrasesCount = new HashMap<String, Map<String, Integer>>();
	String allRelationTypesLabeledTriplesQuery = 
		"SELECT relation, phrase, count(*) as C1 FROM labeled_triples "
			+ "GROUP BY relation, phrase";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(allRelationTypesLabeledTriplesQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase = rs.getString(2);

		    // filter only the phrases we are considering...
		    if (phraseLabeled.contains(phrase)){
			int count = Integer.parseInt(rs.getString(3));
			if (!relation2phrasesCount.containsKey(relation))
			    relation2phrasesCount.put(relation, new HashMap<String, Integer>());
			if (!relation2phrasesCount.get(relation).containsKey(phrase))
			    relation2phrasesCount.get(relation).put(phrase, 0);
			relation2phrasesCount.get(relation).put(phrase, relation2phrasesCount.get(relation).get(phrase) + count);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return relation2phrasesCount;
    }



    /**
     * Returns the counts of typedPhraseLabeled assigned to each relation.
     *      ---------------------------------------------------------------
     * 			        LABELED RELATIONS
     * 	    ---------------------------------------------------------------
     * 	    birthPlace		--> [Person] was born in [Settlement] : 334
     *      team 		--> [Athlete] plays for [Team] : 234
     *      directed		--> [Movie] directed by [FilmDirector] : 212
     * 	    ...
     * 
     * @param typedPhraseLabeled
     * @return
     */
    protected Map<String, Map<String, Integer>> getRelationTypedPhrasesCount(Set<String> typedPhraseLabeled){
	Map<String, Map<String, Integer>> relation2phrasesCount = new HashMap<String, Map<String, Integer>>();
	String allRelationTypesLabeledTriplesQuery = 
		"SELECT relation, phrase, type_subject, type_object, count(*) as C1 FROM labeled_triples "
			+ "GROUP BY relation, phrase, type_subject, type_object";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(allRelationTypesLabeledTriplesQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    int count = Integer.parseInt(rs.getString(5));

		    phrase = ts+"\t"+phrase+"\t"+to;

		    // filter only the phrases we are considering...
		    if (typedPhraseLabeled.contains(phrase)){
			if (!relation2phrasesCount.containsKey(relation))
			    relation2phrasesCount.put(relation, new HashMap<String, Integer>());
			if (!relation2phrasesCount.get(relation).containsKey(phrase))
			    relation2phrasesCount.get(relation).put(phrase, 0);
			relation2phrasesCount.get(relation).put(phrase, relation2phrasesCount.get(relation).get(phrase) + count);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return relation2phrasesCount;
    }

    /**
     * Returns the counts of phraseLabeled assigned to each typed relation.
     *      ---------------------------------------------------------
     * 		               LABELED TYPED RELATIONS
     * 	    ---------------------------------------------------------
     * 	    [Person] birthPlace [Settlement] 	--> was born in : 334
     *      [Athlete] team [Team]		--> plays for : 234
     *      [Movie] directed [FilmDirector] 	--> directed by : 212
     * 	    ...
     * 
     * @param phraseLabeled
     * @return
     */
    protected Map<String, Map<String, Integer>> getTypedRelationPhrasesCount(Set<String> phraseLabeled){
	Map<String, Map<String, Integer>> relation2phrasesCount = new HashMap<String, Map<String, Integer>>();
	String allRelationTypesLabeledTriplesQuery = 
		"SELECT relation, phrase, type_subject, type_object, count(*) as C1 FROM labeled_triples "
			+ "GROUP BY relation, phrase, type_subject, type_object";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(allRelationTypesLabeledTriplesQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    int count = Integer.parseInt(rs.getString(5));

		    relation = ts + "\t" + relation + "\t" + to;

		    // optionally filter only the phrases we are considering ...
		    if(phraseLabeled == null || phraseLabeled.contains(phrase)){
			if (!relation2phrasesCount.containsKey(relation))
			    relation2phrasesCount.put(relation, new HashMap<String, Integer>());
			if (!relation2phrasesCount.get(relation).containsKey(phrase))
			    relation2phrasesCount.get(relation).put(phrase, 0);
			relation2phrasesCount.get(relation).put(phrase, relation2phrasesCount.get(relation).get(phrase) + count);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return relation2phrasesCount;
    }

    /**
     * Fills the map that contains the count for each type of unlabeled fact.
     */
    /*
    private void countUnlabeledFacts(){
	Map<String, Integer> unlabeledCounts = new HashMap<String, Integer>();
	String query = 
		"SELECT DISTINCT type, COUNT(*) as C1 from unlabeled_triples "
		+ "GROUP BY type";
	try (Statement stmt = db.getConnection().createStatement()){	
	    try (ResultSet rs = stmt.executeQuery(query)){
		while(rs.next()){
		    String type = rs.getString(1);
		    int count = Integer.parseInt(rs.getString(2));
		    unlabeledCounts.put(type, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	unlabeledCounts.entrySet().stream().forEach(System.out::println);
    }
    */


}
