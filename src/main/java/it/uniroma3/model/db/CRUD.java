package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.uniroma3.extractor.bean.WikiTriple;
import it.uniroma3.extractor.bean.WikiTriple.TType;
import it.uniroma3.extractor.util.CounterMap;
import it.uniroma3.extractor.util.Pair;
import it.uniroma3.model.DB;
/**
 * This class is an interface between the model and the db. 
 * It performs CRUD operations necessary for the model creation.
 * 
 * @author matteo
 *
 */
public class CRUD {

    private DB db;
    private String labeled_facts = "labeled_triples";
    private String other_facts = "other_triples";

    /**
     * Constructor used to query the DBModel.
     * @param db
     */
    public CRUD(DBModel db){
	//System.out.println("**** Initializing Model DB ****");
	this.db = db;
	db.createNecessaryIndexes();
    }

    /**
     * Constructor used to query the DBCrossValidation.
     * 
     * @param db
     * @param labeled_facts
     * @param unlabeled_facts
     */
    public CRUD(DB db, String labeled_facts){
	//System.out.println("**** Initializing Cross-Validation DB ****");
	this.db = db;
	this.labeled_facts = labeled_facts;
    }

    /**
     * Returns the sum of the (integer) values in a map.
     * 
     * @param map
     * @return
     */
    public int countValues(Map<String, Integer> map){
	int values = 0;
	for (int c : map.values())
	    values+=c;
	return values;
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
		    String phrase_original = rs.getString(2);
		    String phrase_placeholder = rs.getString(3);
		    String pre = rs.getString(4);
		    String post = rs.getString(5);
		    String subject = rs.getString(6);
		    String type_subject = rs.getString(8);
		    String object = rs.getString(9);
		    String type_object = rs.getString(11);
		    String relation = rs.getString(12);
		    triples.add(Pair.make(new WikiTriple(wikid, "", phrase_original,
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
	String query = "SELECT * FROM unlabeled_triples";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String wikid = rs.getString(1);
		    String sentence = rs.getString(2);
		    String phrase_original = rs.getString(3);
		    String phrase_placeholder = rs.getString(4);
		    String pre = rs.getString(5);
		    String post = rs.getString(6);
		    String subject = rs.getString(7);
		    String type_subject = rs.getString(9);
		    String object = rs.getString(10);
		    String type_object = rs.getString(12);
		    triples.add(new WikiTriple(wikid, sentence, phrase_original,
			    phrase_placeholder, pre, post, subject, object, 
			    type_subject, type_object, TType.JOINABLE.name()));
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return triples;
    }


    /**
     * Returns typed relations and their counts.
     * 		------------------------------------------------
     * 		   	    LABELED TYPED RELATIONS
     * 		------------------------------------------------
     * 		[Settlement] ### isPartOf ### [Settlement]	: 675 K
     * 		[Settlement] ### isPartOf ### [City]		: 342 K
     * 		[City] 	### country ### [Country]		: 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getTypedRelationsCount(Set<String> availablePhrases){
	CounterMap<String> countTypedRelation = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, relation, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String relation = rs.getString(2);
		    String type_subject = rs.getString(3);
		    String type_object = rs.getString(4);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countTypedRelation.add(type_subject + "###" + relation + "###"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedRelation;
    }

    /**
     * Returns phrases, types and relations all together with their counts. 
     * 		------------------------------------------------
     * 		   	    LABELED TYPED PHRASES RELATIONS
     * 		------------------------------------------------
     * 		located in ### [Settlement] ### isPartOf ### [Settlement]	: 675 K
     * 		surrounded by ### [Settlement] ### isPartOf ### [City]		: 342 K
     * 		is in ### [City] ### country ### [Country]			: 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getRelationTypesPhraseCounts(Set<String> availablePhrases){
	CounterMap<String> countTypedPhraseRelation = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, relation, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String relation = rs.getString(2);
		    String type_subject = rs.getString(3);
		    String type_object = rs.getString(4);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countTypedPhraseRelation.add(phrase_placeholder + "###" + type_subject + "###" + relation + "###"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedPhraseRelation;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getCleanRelationCount(Set<String> availablePhrases){
	CounterMap<String> countRelation = new CounterMap<String>();
	String query = "SELECT relation, phrase_placeholder FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase_placeholder = rs.getString(2);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countRelation.add(relation);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countRelation;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getCleanRelationCountByTypedPhrases(Set<String> availablePhrases){
	CounterMap<String> countRelation = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, relation FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String relation = rs.getString(2);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countRelation.add(relation);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countRelation;
    }
    
    /**
     * Returns the count of unlabeled phrases from the set of phrases in input. 
     * 		----------------------	
     * 		  LABELED PHRASES
     * 		----------------------
     * 		was born in 	: 34224
     * 		plays for 	: 5424
     * 		directed by 	: 3432
     *		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getLabeledPhrasesCount(Set<String> availablePhrases){
	CounterMap<String> phrasesCount = new CounterMap<String>();
	String phrasesCountQuery = "SELECT phrase_placeholder FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(phrasesCountQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			phrasesCount.add(phrase_placeholder);
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
     * 		         LABELED TYPED PHRASES
     * 		---------------------------------------------
     * 		[Person] was born in [Settlement] 	: 857
     * 		[Athlete] plays for [Team] 		: 544
     * 		[Movie] directed by [FilmDirector] 	: 344
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getLabeledTypedPhrasesCount(Set<String> availablePhrases){
	CounterMap<String> phrasesCount = new CounterMap<String>();
	String query = 
		"SELECT phrase_placeholder, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    String typedPhrase = ts+"\t"+phrase_placeholder+"\t"+to;
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			phrasesCount.add(typedPhrase);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrasesCount;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getUnlabeledPhrasesCount(Set<String> availablePhrases){
	CounterMap<String> countPhrases = new CounterMap<String>();
	String phrasesCountQuery = "SELECT phrase_placeholder FROM unlabeled_triples";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(phrasesCountQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countPhrases.add(phrase_placeholder);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countPhrases;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getUnlabeledTypedPhrasesCount(Set<String> availablePhrases){
	CounterMap<String> phrasesCount = new CounterMap<String>();
	String query = 
		"SELECT phrase_placeholder, type_subject, type_object FROM unlabeled_triples";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    String typedPhrase = ts+"\t"+phrase_placeholder+"\t"+to;
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			phrasesCount.add(typedPhrase);
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getRelationPhrasesCount(Set<String> availablePhrases){
	Map<String, CounterMap<String>> relation2phrasesCount = new HashMap<String, CounterMap<String>>();
	String query = "SELECT relation, phrase_placeholder FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase_placeholder = rs.getString(2);
		    // filter only the phrases we are considering...
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals("")){
			if (!relation2phrasesCount.containsKey(relation))
			    relation2phrasesCount.put(relation, new CounterMap<String>());
			relation2phrasesCount.get(relation).add(phrase_placeholder);
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
     *      	--------------------------------------
     * 		          LABELED RELATIONS
     * 	    	--------------------------------------
     * 	    	birthPlace 	<-- was born in : 334
     *      	team		<-- plays for : 234
     *      	directed	<-- directed by : 212
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getPhrasesRelationsCount(Set<String> availablePhrases){
	Map<String, CounterMap<String>> phrases2relationsCount = new HashMap<String, CounterMap<String>>();
	String query = "SELECT relation, phrase_placeholder FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase_placeholder = rs.getString(2);
		    // filter only the phrases we are considering...
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals("")) {
			if (!phrases2relationsCount.containsKey(phrase_placeholder))
			    phrases2relationsCount.put(phrase_placeholder, new CounterMap<String>());
			phrases2relationsCount.get(phrase_placeholder).add(relation);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrases2relationsCount;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getRelationTypedPhrasesCount(Set<String> availablePhrases){
	Map<String, CounterMap<String>> relation2phrasesCount = new HashMap<String, CounterMap<String>>();
	String query = "SELECT relation, phrase_placeholder, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase_placeholder = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    String typedPhrase = ts+"\t"+phrase_placeholder+"\t"+to;

		    // filter only the phrases we are considering...
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals("")){
			if (!relation2phrasesCount.containsKey(relation))
			    relation2phrasesCount.put(relation, new CounterMap<String>());
			relation2phrasesCount.get(relation).add(typedPhrase);
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
     * 			        LABELED PHRASES
     * 	    ---------------------------------------------------------------
     * 	    birthPlace	<-- [Person] was born in [Settlement] : 334
     *      team 	<-- [Athlete] plays for [Team] : 234
     *      directed	<-- [Movie] directed by [FilmDirector] : 212
     * 	    ...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getTypedPhrasesRelationsCount(Set<String> availablePhrases){
	Map<String, CounterMap<String>> phrases2relationsCount = new HashMap<String, CounterMap<String>>();
	String query = "SELECT relation, phrase_placeholder, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase_placeholder = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    String typedPhrase = ts+"\t"+phrase_placeholder+"\t"+to;

		    // filter only the phrases we are considering...
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals("")){
			if (!phrases2relationsCount.containsKey(typedPhrase))
			    phrases2relationsCount.put(typedPhrase, new CounterMap<String>());
			phrases2relationsCount.get(typedPhrase).add(relation);
		    }
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return phrases2relationsCount;
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
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getTypedRelationPhrasesCount(Set<String> availablePhrases){
	Map<String, CounterMap<String>> relation2phrasesCount = new HashMap<String, CounterMap<String>>();
	String query = "SELECT relation, phrase_placeholder, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String relation = rs.getString(1);
		    String phrase = rs.getString(2);
		    String ts = rs.getString(3);
		    String to = rs.getString(4);
		    String typedRelation = ts + "\t" + relation + "\t" + to;

		    // optionally filter only the phrases we are considering ...
		    if(availablePhrases.contains(phrase) && !phrase.equals("")){
			if (!relation2phrasesCount.containsKey(typedRelation))
			    relation2phrasesCount.put(typedRelation, new CounterMap<String>());
			relation2phrasesCount.get(typedRelation).add(phrase);
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
    public CounterMap<String> getUnlabeledFactsByType(){
	CounterMap<String> unlabeledCounts = new CounterMap<String>();
	String query = "SELECT DISTINCT type, COUNT(*) as C1 from " + this.other_facts
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
	return unlabeledCounts;
    }

    /**********************************************************************************
     * Returns the phrases (from the ones labeled with a relation),
     * that are present at least minFreq times.
     * 		---------------------	
     * 		   AVAILABLE PHRASES
     * 		---------------------
     * 		   was born in 
     * 		   plays for 
     * 	           directed by 
     * 		   ...
     * 
     * @param minFreq the minimum count
     * @return
     */
    public CounterMap<String> getAvailablePhrases(int minFreq){
	CounterMap<String> availablePhrases = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, count(*) as C1 FROM " + this.labeled_facts
		+ " GROUP BY phrase_placeholder "
		+ "HAVING C1>?";
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    stmt.setInt(1, minFreq);
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    int count = rs.getInt(2);
		    if (!phrase_placeholder.equals(""))
			availablePhrases.add(phrase_placeholder, count);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return availablePhrases;
    }

}
