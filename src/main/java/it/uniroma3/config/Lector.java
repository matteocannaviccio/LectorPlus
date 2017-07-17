package it.uniroma3.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.io.Files;

import it.uniroma3.main.bean.WikiLanguage;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.util.nlp.OpenNLP;
import it.uniroma3.main.util.nlp.StanfordNLP;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.pipeline.articleparser.ArticleTyper;
import it.uniroma3.pipeline.articleparser.BlockParser;
import it.uniroma3.pipeline.articleparser.MarkupParser;
import it.uniroma3.pipeline.articleparser.TextParser;
import it.uniroma3.pipeline.articleparser.WikiParser;
import it.uniroma3.pipeline.articleparser.XMLParser;
import it.uniroma3.pipeline.entitydetection.FSMNationality;
import it.uniroma3.pipeline.entitydetection.FSMSeed;
import it.uniroma3.pipeline.entitydetection.ReplAttacher;
import it.uniroma3.pipeline.entitydetection.ReplFinder;
import it.uniroma3.pipeline.entitydetection.dbsp.DBPediaSpotlight;
import it.uniroma3.pipeline.triplesextractor.Triplifier;
/**
 * This static class declares and initializes all the components needed in the system.
 * Some components are declared ThreadLocal, to ensure the existance of a copy for each thread.
 * e using DBPedia Spotlight, Lector class contains the refenrece to the external process.
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

    /* ** DBpedia Spotlight ** */
    private static ThreadLocal<DBPediaSpotlight> dbspot;
    private static Process localServerSideProcess;

    /* Needed in Triple Extraction */
    private static DBPedia dbpedia;
    private static Triplifier triplifier;
    /* Keep the (open) connections here */
    private static DBModel dbmodel;


    /**
     * Here we initialize all the components of the tool.
     * Lector is a global class that initializes and keep a reference to all the components.
     * 
     * @param config
     */
    public static void init(WikiLanguage lang, Set<String> pipeline) {
	System.out.println("\nInitializing");
	System.out.println("------------");
	wikiLang = lang;
	if (pipeline.contains("AP"))
	    initAP();
	initDBpedia();
	if (pipeline.contains("ED"))
	    initED();
	if (pipeline.contains("TE"))
	    initTE();
    }

    /**
     * Initializes Article Parser.
     */
    public static void initAP(){
	System.out.println("\t-> Init Article Parser (AP)");
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
	System.out.println("\t-> Init Entity Detector (ED)");

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

	if(Configuration.useDBpediaSpotlight()){

	    try {
		localServerSideProcess = runSpotlight();

	    } catch (IOException e) {
		System.out.println("Problems with running local DBPedia Spotlight process!");
		e.printStackTrace();
	    }

	    dbspot = new ThreadLocal<DBPediaSpotlight>() {
		@Override protected DBPediaSpotlight initialValue() {
		    return new DBPediaSpotlight(0.5, 0);
		}
	    };
	}
    }

    /**
     * Initializes Triple Extractor.
     */
    public static void initTE(){
	System.out.println("\t-> Init Triple Extractor (TE) and model DB");
	triplifier = new Triplifier();
    }

    /**
     * Initialize the KG.
     */
    public static void initDBpedia(){
	System.out.println("\t-> Init Knowledge Graph...");
	dbpedia = new DBPedia();
    }

    /**
     * 
     * @return
     */
    public static DBPediaSpotlight getDBSpot(){
	return dbspot.get();
    }

    /**
     * 
     * @return
     */
    public static MarkupParser getMarkupParser() {
	if (markupParser == null)
	    markupParser = new MarkupParser();
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
	if (articleTyper == null)
	    articleTyper = new ArticleTyper();
	return articleTyper;
    }

    /**
     * @return the xmlParser
     */
    public static XMLParser getXmlParser() {
	if (xmlParser == null)
	    xmlParser = new XMLParser();
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
     * Close all the connections that are open.
     */
    public static void close(){
	if (dbmodel != null){
	    dbmodel.closeConnection();
	    dbmodel = null;
	}
	// kill DBPedia SPotlight process, if exists
	closeDBPediaSpotlight();
    }

    /**
     * Pull down the (local) server where DBPedia SPotlight is running on.
     */
    public static void closeDBPediaSpotlight() {
	if(Configuration.useDBpediaSpotlight() && localServerSideProcess != null){
	    localServerSideProcess.destroy();
	}
    }

    /**
     * Returns the process where DBPedia Spotlight is running on.
     * TODO: improve fixed-time waiting for the creation. 
     * @throws IOException 
     */
    private static Process runSpotlight() throws IOException {
	System.out.println("\t-> Loading DBPedia Spotlight on " + Configuration.getSpotlightLocalURL());
	ProcessBuilder pb = new ProcessBuilder(
		"java",
		"-Xmx16g",
		"-Dfile.encoding=utf-8",
		"-jar",
		Configuration.getSpotlightJar(),
		Configuration.getSpotlightModel(),
		Configuration.getSpotlightLocalURL()
		);
	pb.redirectError(new File(Configuration.getSpotlightLocalERR(2222)));

	/*
	 * Start a new separate process but keep an hook shutdown process
	 * for its termination based on the parent process.
	 */
	Process p = pb.start();
	Runtime.getRuntime().addShutdownHook(new Thread() {
	    @Override
	    public void run() {
		p.destroy();
	    }
	});


	try {
	    // wait untill the server started
	    BufferedReader logBR;
	    String last = "";
	    int maxChecks = 50;
	    while(!last.startsWith("Server started") && maxChecks>0){
		p.waitFor(5, TimeUnit.SECONDS);
		logBR = new BufferedReader(new FileReader(Configuration.getSpotlightLocalERR(2222)));
		String line = null;
		while ((line = logBR.readLine()) != null) {
		    last = line;
		}
		maxChecks -=1;
	    }

	    File log = new File(Configuration.getSpotlightLocalERR(2222));
	    if (log.exists()) {
		log.delete();     
	    }


	} catch (IOException | InterruptedException e ) {
	    e.printStackTrace();
	} 
	return p;
    }

}
