package it.uniroma3.extractor.configuration;

import java.util.Locale;

import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.entitydetection.FSMSeed;
import it.uniroma3.extractor.entitydetection.ReplAttacher;
import it.uniroma3.extractor.entitydetection.ReplFinder;
import it.uniroma3.extractor.kg.KGEndPoint;
import it.uniroma3.extractor.parser.ArticleTyper;
import it.uniroma3.extractor.parser.BlockParser;
import it.uniroma3.extractor.parser.MarkupParser;
import it.uniroma3.extractor.parser.TextParser;
import it.uniroma3.extractor.parser.WikiParser;
import it.uniroma3.extractor.parser.XMLParser;
import it.uniroma3.extractor.triples.Triplifier;
import it.uniroma3.extractor.util.nlp.DBPediaSpotlight;
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

    /* Needed in Article Parsing */
    private static WikiParser wikiParser;
    private static MarkupParser markupParser; 
    private static ArticleTyper articleTyper;
    private static XMLParser xmlParser;
    private static BlockParser blockParser;
    private static TextParser textParser;

    /* Needed in Entity Detection */
    private static ThreadLocal<StanfordNLP> stanfordExpert;
    private static ThreadLocal<OpenNLP> openNLPExpert;
    private static ThreadLocal<FSMSeed> fsm;
    private static ReplFinder entitiesFinder;
    private static ReplAttacher entitiesTagger;

    /* Needed in Triple Extraction */
    private static KGEndPoint kg;
    private static Triplifier triplifier;

    /* Keep the (open) connections here */
    private static DBFacts dbfacts;
    private static DBModel dbmodel;
    
    private static DBPediaSpotlight dbpediaSpotlight;

    private static String langCode;

    /**
     * 
     * @param config
     */
    public static void init(WikiLanguage lang) {
	langCode = lang.getCode();
	wikiParser = new WikiParser(lang);
	kg = new KGEndPoint();
	markupParser = new MarkupParser();
	articleTyper = new ArticleTyper(lang);
	xmlParser = new XMLParser();
	blockParser = new BlockParser(lang);
	textParser = new TextParser(lang);
	triplifier = new Triplifier();

	entitiesFinder = new ReplFinder();
	entitiesTagger = new ReplAttacher();
	
	//dbpediaSpotlight = new DBPediaSpotlight(0.5, 0);

	stanfordExpert = new ThreadLocal<StanfordNLP>() {
	    @Override protected StanfordNLP initialValue() {
		return new StanfordNLP(langCode);
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
    }


    /**
     * @return the redirectResolver
     */
    public static MarkupParser getMarkupParser() {
	return markupParser;
    }

    /**
     * @return the typesResolver
     */
    public static StanfordNLP getNLPExpert() {
	return stanfordExpert.get();
    }

    /**
     * @return the articleTyper
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
	return blockParser;
    }

    /**
     * @return the textParser
     */
    public static TextParser getTextParser() {
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
     * @return the kg
     */
    public static KGEndPoint getKg() {
	return kg;
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
     * @return the langCode
     */
    public static String getLangCode() {
	return langCode;
    }
    
    /**
     * 
     * @return
     */
    public static DBPediaSpotlight getSpotlight(){
	return dbpediaSpotlight;
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
     * @return
     */
    public static Locale getLocale(){
	if (langCode.equals("en"))
	    return Locale.ENGLISH;
	if (langCode.equals("es"))
	    return new Locale("es", "ES");
	if (langCode.equals("it"))
	    return Locale.ITALIAN;
	if (langCode.equals("ge"))
	    return Locale.GERMANY;
	if (langCode.equals("fr"))
	    return Locale.FRENCH;
	return null;
    }


}
