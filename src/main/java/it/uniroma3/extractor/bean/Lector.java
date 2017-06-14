package it.uniroma3.extractor.bean;

import java.util.Locale;
import java.util.Set;

import it.uniroma3.extractor.bean.WikiLanguage.Lang;
import it.uniroma3.extractor.entitydetection.FSMNationality;
import it.uniroma3.extractor.entitydetection.FSMSeed;
import it.uniroma3.extractor.entitydetection.ReplAttacher;
import it.uniroma3.extractor.entitydetection.ReplFinder;
import it.uniroma3.extractor.kg.DBPedia;
import it.uniroma3.extractor.parser.ArticleTyper;
import it.uniroma3.extractor.parser.BlockParser;
import it.uniroma3.extractor.parser.MarkupParser;
import it.uniroma3.extractor.parser.TextParser;
import it.uniroma3.extractor.parser.WikiParser;
import it.uniroma3.extractor.parser.XMLParser;
import it.uniroma3.extractor.triples.Triplifier;
import it.uniroma3.extractor.util.nlp.OpenNLP;
import it.uniroma3.extractor.util.nlp.StanfordNLP;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.model.extraction.DBFacts;
/**
 * 
 * @author matteo
 *
 */
public class Lector {

    private static WikiLanguage wikiLang;

    /* Needed in Article Parsing */
    private static XMLParser xmlParser;
    private static WikiParser wikiParser;
    private static ArticleTyper articleTyper;
    private static MarkupParser markupParser; 
    private static BlockParser blockParser;
    private static TextParser textParser;

    /* Needed in Entity Detection */
    private static ThreadLocal<StanfordNLP> stanfordExpert;
    private static ThreadLocal<OpenNLP> openNLPExpert;
    private static ThreadLocal<FSMSeed> fsm;
    private static ThreadLocal<FSMNationality> fsm_nat;
    private static ReplFinder entitiesFinder;
    private static ReplAttacher entitiesTagger;

    /* Needed in Triple Extraction */
    private static DBPedia dbpedia;
    private static Triplifier triplifier;
    /* Keep the (open) connections here */
    private static DBFacts dbfacts;
    private static DBModel dbmodel;

    /**
     * Here we initialize all the components of the tool.
     * Lector is a global class that initializes and keep a reference to all the components.
     * 
     * @param config
     */
    public static void init(WikiLanguage lang, Set<String> pipeline) {
	System.out.println("\n**** INITIALIZING LECTOR ****");
	wikiLang = lang;
	if (pipeline.contains("AP"))
	    initAP();
	if (pipeline.contains("ED"))
	    initED();
	if (pipeline.contains("TE"))
	    initTE();
	initDBpedia();
    }

    /**
     * Initializes Article Parser.
     */
    public static void initAP(){
	wikiParser = new WikiParser();
	markupParser = new MarkupParser();
	articleTyper = new ArticleTyper();
	xmlParser = new XMLParser();
	blockParser = new BlockParser();
	textParser = new TextParser();
    }

    /**
     * Initializes Entity Detection.
     */
    public static void initED(){
	entitiesFinder = new ReplFinder();
	entitiesTagger = new ReplAttacher();
	stanfordExpert = new ThreadLocal<StanfordNLP>() {
	    @Override protected StanfordNLP initialValue() {
		return new StanfordNLP();
	    }
	};
	openNLPExpert = new ThreadLocal<OpenNLP>() {
	    @Override protected OpenNLP initialValue() {
		return new OpenNLP();
	    }
	};
	fsm = new ThreadLocal<FSMSeed>() {
	    @Override protected FSMSeed initialValue() {
		return new FSMSeed(openNLPExpert.get());
	    }
	};
	fsm_nat = new ThreadLocal<FSMNationality>() {
	    @Override protected FSMNationality initialValue() {
		return new FSMNationality();
	    }
	};
    }

    /**
     * Initializes Triple Extractor.
     */
    public static void initTE(){
	triplifier = new Triplifier();
    }

    /**
     * Initialize the KG.
     */
    public static void initDBpedia(){
	dbpedia = new DBPedia();
    }


    /**
     * 
     * @return
     */
    public static MarkupParser getMarkupParser() {
	return markupParser;
    }

    /**
     * 
     * @return
     */
    public static StanfordNLP getNLPExpert() {
	return stanfordExpert.get();
    }

    /**
     * 
     * @return
     */
    public static ArticleTyper getArticleTyper() {
	return articleTyper;
    }

    /**
     * @return the xmlParser
     */
    public static XMLParser getXmlParser() {
	return xmlParser;
    }

    /**
     * @return the blockParser
     */
    public static BlockParser getBlockParser() {
	if (blockParser == null)
	    blockParser = new BlockParser();
	return blockParser;
    }

    /**
     * @return the textParser
     */
    public static TextParser getTextParser() {
	if (textParser == null)
	    textParser = new TextParser();
	return textParser;
    }

    /**
     * @return the triplifier
     */
    public static Triplifier getTriplifier() {
	return triplifier;
    }

    /**
     * @return the openNLPExpert
     */
    public static OpenNLP getOpenNLPExpert() {
	return openNLPExpert.get();
    }


    /**
     * @return the fsm
     */
    public static FSMSeed getFsm() {
	return fsm.get();
    }

    /**
     * @return the fsm_nat
     */
    public static FSMNationality getFsmNat() {
	return fsm_nat.get();
    }

    /**
     * @return the kg
     */
    public static DBPedia getDBPedia() {
	return dbpedia;
    }

    /**
     * @return the wikiParser
     */
    public static WikiParser getWikiParser() {
	return wikiParser;
    }

    /**
     * @return the entitiesFinder
     */
    public static ReplFinder getEntitiesFinder() {
	return entitiesFinder;
    }

    /**
     * @return the entitiesTagger
     */
    public static ReplAttacher getEntitiesTagger() {
	return entitiesTagger;
    }

    /**
     * 
     * @return
     */
    public static WikiLanguage getWikiLang() {
	return wikiLang;
    }


    /**
     * 
     * @param create
     * @return
     */
    public static DBModel getDbmodel(boolean create) {
	if (dbmodel == null){
	    dbmodel = new DBModel(Configuration.getDBModel());
	    if (create)
		dbmodel.createModelDB();
	}
	return dbmodel;
    }


    /**
     * 
     * @param create
     * @return
     */
    public static DBFacts getDbfacts(boolean create) {
	if (dbfacts == null){
	    dbfacts = new DBFacts(Configuration.getDBFacts());
	    if (create)
		dbfacts.createFactsDB();
	}
	return dbfacts;
    }

    /**
     * 
     */
    public static void closeAllConnections(){
	if (dbfacts != null){
	    dbfacts.closeConnection();
	    dbfacts = null;
	}
	if (dbmodel != null){
	    dbmodel.closeConnection();
	    dbmodel = null;
	}
    }

    /**
     * 
     * @return
     */
    public static Locale getLocale(){
	if (wikiLang.equals(Lang.en))
	    return Locale.ENGLISH;
	if (wikiLang.equals(Lang.en))
	    return new Locale("es", "ES");
	if (wikiLang.equals(Lang.it))
	    return Locale.getDefault();
	if (wikiLang.equals(Lang.de))
	    return Locale.GERMANY;
	if (wikiLang.equals(Lang.fr))
	    return Locale.FRANCE;
	return null;
    }


}
