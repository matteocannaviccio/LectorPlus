package it.uniroma3.extractor.configuration;

import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.entitydetection.ReplAttacher;
import it.uniroma3.extractor.entitydetection.ReplFinder;
import it.uniroma3.extractor.entitydetection.SeedFSM;
import it.uniroma3.extractor.kg.KGEndPoint;
import it.uniroma3.extractor.parser.ArticleTyper;
import it.uniroma3.extractor.parser.BlockParser;
import it.uniroma3.extractor.parser.MarkupParser;
import it.uniroma3.extractor.parser.TextParser;
import it.uniroma3.extractor.parser.WikiParser;
import it.uniroma3.extractor.parser.XMLParser;
import it.uniroma3.extractor.triples.Triplifier;
import it.uniroma3.extractor.util.nlp.OpenNLP;
import it.uniroma3.extractor.util.nlp.StanfordNLP;
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
    private static ThreadLocal<SeedFSM> fsm;
    private static ReplFinder entitiesFinder;
    private static ReplAttacher entitiesTagger;
    
    /* Needed in Triple Extraction */
    private static KGEndPoint kg;
    private static Triplifier triplifier;
    
    /**
     * 
     * @param config
     */
    public static void init(WikiLanguage lang) {
	wikiParser = new WikiParser(lang);
	kg = new KGEndPoint();
	markupParser = new MarkupParser();
	articleTyper = new ArticleTyper(lang);
	xmlParser = new XMLParser();
	blockParser = new BlockParser(lang);
	textParser = new TextParser(lang);
	triplifier = new Triplifier(Configuration.createModelDB());
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
	fsm = new ThreadLocal<SeedFSM>() {
	    @Override protected SeedFSM initialValue() {
		return new SeedFSM(openNLPExpert.get());
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
    public static ThreadLocal<OpenNLP> getOpenNLPExpert() {
        return openNLPExpert;
    }
    

    /**
     * @return the fsm
     */
    public static ThreadLocal<SeedFSM> getFsm() {
        return fsm;
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


    
    

}
