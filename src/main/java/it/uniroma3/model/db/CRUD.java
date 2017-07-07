package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private String unlabeled_facts = "unlabeled_triples";

    /**
     * Constructor used to query the DBModel.
     * @param db
     */
    public CRUD(DBModel db){
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
	this.db = db;
	this.labeled_facts = labeled_facts;
    }

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************

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

    /*********************************************** 
     * 
     *       SELECT ALL labeled PAIRS of Entities
     * 
     ***********************************************/
    public Map<String, List<Pair<WikiTriple, String>>> selectAllLabeledPairs(){
	Map<String, List<Pair<WikiTriple, String>>> triples = new HashMap<String, List<Pair<WikiTriple, String>>>();
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
		    String wiki_subject = rs.getString(7);
		    String type_subject = rs.getString(8);
		    String object = rs.getString(9);
		    String wiki_object = rs.getString(10);
		    String type_object = rs.getString(11);
		    String relation = rs.getString(12);

		    String key = wiki_subject + "\t" + phrase_placeholder + "\t" + wiki_object;
		    WikiTriple t = new WikiTriple(wikid, "", phrase_original,
			    phrase_placeholder, pre, post, subject, object, 
			    type_subject, type_object, TType.JOINABLE.name());

		    if (triples.containsKey(key)){
			triples.get(key).add(Pair.make(t, relation));
		    }else{
			List<Pair<WikiTriple, String>> list = new LinkedList<Pair<WikiTriple, String>>();
			list.add(Pair.make(t, relation));
			triples.put(key, list);
		    }
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

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getTR_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> tr_lt_count = getTR_LT_count(availablePhrases);
	CounterMap<String> tr_ut_count;
	if (includeNone){
	    tr_ut_count = getTR_UT_count(availablePhrases);
	    tr_lt_count.addAll(tr_ut_count);
	}
	return tr_lt_count;
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
    private CounterMap<String> getTR_LT_count(Set<String> availablePhrases){
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
			countTypedRelation.add(type_subject + "\t" + relation + "\t"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedRelation;
    }

    /**
     * Returns typed unlabeled and their counts.
     * 		------------------------------------------------
     * 		   	    UNLABELED TYPED RELATIONS
     * 		------------------------------------------------
     * 		[Settlement] ### NONE ### [Settlement]	: 675 K
     * 		[Settlement] ### NONE ### [City]	: 342 K
     * 		[City] 	### NONE ### [Country]		: 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    private CounterMap<String> getTR_UT_count(Set<String> availablePhrases){
	CounterMap<String> countTypedRelation = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, type_subject, type_object FROM " + this.unlabeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String type_subject = rs.getString(2);
		    String type_object = rs.getString(3);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countTypedRelation.add(type_subject + "\t" + "NONE" + "\t"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedRelation;
    }

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getPTR_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> ptr_lt_count = getPTR_LT_counts(availablePhrases);
	CounterMap<String> ptr_ut_count;
	if (includeNone){
	    ptr_ut_count = getPTR_UT_counts(availablePhrases);
	    ptr_lt_count.addAll(ptr_ut_count);
	}
	return ptr_lt_count;
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
    private CounterMap<String> getPTR_LT_counts(Set<String> availablePhrases){
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
			countTypedPhraseRelation.add(phrase_placeholder + "\t" + type_subject + "\t" + relation + "\t"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedPhraseRelation;
    }

    /**
     * Returns phrases, types and relations all together with their counts. 
     * 		------------------------------------------------
     * 		   	    LABELED TYPED PHRASES RELATIONS
     * 		------------------------------------------------
     * 		located in ### [Settlement] ### NONE ### [Settlement]	: 675 K
     * 		surrounded by ### [Settlement] ### NONE ### [City]	: 342 K
     * 		is in ### [City] ### NONE ### [Country]			: 112 K
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    private CounterMap<String> getPTR_UT_counts(Set<String> availablePhrases){
	CounterMap<String> countTypedPhraseRelation = new CounterMap<String>();
	String query = "SELECT phrase_placeholder, type_subject, type_object FROM " + this.unlabeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String type_subject = rs.getString(2);
		    String type_object = rs.getString(3);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			countTypedPhraseRelation.add(phrase_placeholder + "\t" + type_subject + "\t" + "NONE" + "\t"  + type_object);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return countTypedPhraseRelation;
    }

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getR_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> r_lt_count = getR_LT_counts(availablePhrases);
	if (includeNone){
	    r_lt_count.add("NONE", getR_UT_counts(availablePhrases));
	}
	return r_lt_count;
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
    private CounterMap<String> getR_LT_counts(Set<String> availablePhrases){
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
    private int getR_UT_counts(Set<String> availablePhrases){
	int ut_count = 0;
	String query = "SELECT phrase_placeholder FROM " + this.unlabeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			ut_count +=1;
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return ut_count;
    }

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getPT_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> pt_lt_count = getPT_LT_counts(availablePhrases);
	CounterMap<String> pt_ut_count;
	if (includeNone){
	    pt_ut_count = getPT_UT_counts(availablePhrases);
	    pt_lt_count.addAll(pt_ut_count);
	}
	return pt_lt_count;
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
    public CounterMap<String> getPT_LT_counts(Set<String> availablePhrases){
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
     * Returns the count of unlabeled typed phrases from the set of typed phrases in input. 
     *       	---------------------------------------------
     * 		         UNLABELED TYPED PHRASES
     * 		---------------------------------------------
     * 		[Person] was born in [Settlement] 	: 857
     * 		[Athlete] plays for [Team] 		: 544
     * 		[Movie] directed by [FilmDirector] 	: 344
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public CounterMap<String> getPT_UT_counts(Set<String> availablePhrases){
	CounterMap<String> phrasesCount = new CounterMap<String>();
	String query = 
		"SELECT phrase_placeholder, type_subject, type_object FROM " + this.unlabeled_facts;
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

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getT_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> t_lt_count = getT_LT_counts(availablePhrases);
	CounterMap<String> t_ut_count;
	if (includeNone){
	    t_ut_count = getT_UT_counts(availablePhrases);
	    t_lt_count.addAll(t_ut_count);
	}
	return t_lt_count;
    }

    /**
     * Returns the count of labeled types from labeled triples. 
     *       	-------------------------------------
     * 		             LABELED TYPEs
     * 		-------------------------------------
     * 		[Person]###[Settlement] 	: 857
     * 		[Athlete]###[Team] 		: 544
     * 		[Movie]###[FilmDirector] 	: 344
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    private CounterMap<String> getT_LT_counts(Set<String> availablePhrases){
	CounterMap<String> typesCount = new CounterMap<String>();
	String query = 
		"SELECT phrase_placeholder, type_subject, type_object FROM " + this.labeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    String types = ts+"\t"+to;
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			typesCount.add(types);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return typesCount;
    }

    /**
     * Returns the count of unlabeled types from unlabeled triples. 
     *       	-------------------------------------
     * 		             UNLABELED TYPEs
     * 		-------------------------------------
     * 		[Person]###[Settlement] 	: 857
     * 		[Athlete]###[Team] 		: 544
     * 		[Movie]###[FilmDirector] 	: 344
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    private CounterMap<String> getT_UT_counts(Set<String> availablePhrases){
	CounterMap<String> typesCount = new CounterMap<String>();
	String query = 
		"SELECT phrase_placeholder, type_subject, type_object FROM " + this.unlabeled_facts;
	try (PreparedStatement stmt = db.getConnection().prepareStatement(query)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next()){
		    String phrase_placeholder = rs.getString(1);
		    String ts = rs.getString(2);
		    String to = rs.getString(3);
		    String types = ts+"\t"+to;
		    if (availablePhrases.contains(phrase_placeholder) && !phrase_placeholder.equals(""))
			typesCount.add(types);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
	return typesCount;
    }

    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/


    /**
     * 
     * @param availablePhrases
     * @param includeNone
     * @return
     */
    public CounterMap<String> getP_count(Set<String> availablePhrases, boolean includeNone){
	CounterMap<String> p_lt_count = getP_LT_counts(availablePhrases);
	CounterMap<String> p_ut_count;
	if (includeNone){
	    p_ut_count = getP_UT_counts(availablePhrases);
	    p_lt_count.addAll(p_ut_count);
	}
	return p_lt_count;
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
    public CounterMap<String> getP_LT_counts(Set<String> availablePhrases){
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
    public CounterMap<String> getP_UT_counts(Set<String> availablePhrases){
	CounterMap<String> countPhrases = new CounterMap<String>();
	String phrasesCountQuery = "SELECT phrase_placeholder FROM " + unlabeled_facts;
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


    /**********************************************************************************
    /**********************************************************************************
    /**********************************************************************************/

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
    public Map<String, CounterMap<String>> getRtoCountedP_LT(Set<String> availablePhrases, boolean includeNone){
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

	if (includeNone)
	    relation2phrasesCount.put("NONE", this.getP_UT_counts(availablePhrases));

	return relation2phrasesCount;
    }

    /**
     * Returns the counts of relations assigned to each phraseLabeled.
     *      	--------------------------------------
     * 		          LABELED RELATIONS
     * 	    	--------------------------------------
     * 	    	was born in 	--> birthPlace : 334
     *      	plays for	--> team : 234
     *      	directed by	--> directed : 212
     * 		...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getPtoCountedR_LT(Set<String> availablePhrases, boolean includeNone){
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
	if (includeNone){
	    CounterMap<String> unlabeled = getP_UT_counts(availablePhrases);
	    for (Map.Entry<String, CounterMap<String>> entry : phrases2relationsCount.entrySet()){
		if (unlabeled.containsKey(entry.getKey()))
		    entry.getValue().add("NONE", unlabeled.get(entry.getKey()));
	    }
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
    public Map<String, CounterMap<String>> getRtoCountedPT_LT(Set<String> availablePhrases, boolean includeNone){
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
	
	if (includeNone)
	    relation2phrasesCount.put("NONE", this.getPT_UT_counts(availablePhrases));

	
	return relation2phrasesCount;
    }

    /**
     * Returns the counts of typedPhraseLabeled assigned to each relation.
     *      --------------------------------------------------------
     * 			        LABELED PHRASES
     * 	    --------------------------------------------------------
     *      [Person] was born in [Settlement]	--> birthPlace : 334
     *      [Athlete] plays for [Team] 		--> team : 234
     *      [Movie] directed by [FilmDirector]	--> directed : 212
     * 	    ...
     * 
     * @param availablePhrases the phrases available in the model
     * @return
     */
    public Map<String, CounterMap<String>> getPTtoCountedR_LT(Set<String> availablePhrases, boolean includeNone){
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
	
	if (includeNone){
	    CounterMap<String> unlabeled = getPT_UT_counts(availablePhrases);
	    for (Map.Entry<String, CounterMap<String>> entry : phrases2relationsCount.entrySet()){
		if (unlabeled.containsKey(entry.getKey()))
		    entry.getValue().add("NONE", unlabeled.get(entry.getKey()));
	    }
	}
	return phrases2relationsCount;
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
