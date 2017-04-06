package it.uniroma3.configuration;

import java.io.File;

import it.uniroma3.entitydetection.SeedFSM;
import it.uniroma3.kg.DBPedia;
import it.uniroma3.kg.RedirectResolver;
import it.uniroma3.kg.ontology.TypesAssigner;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.ArticleTyper;
import it.uniroma3.parser.BlockParser;
import it.uniroma3.parser.MarkupParser;
import it.uniroma3.parser.TextParser;
import it.uniroma3.parser.XMLParser;
import it.uniroma3.triples.Triplifier;
import it.uniroma3.util.nlp.OpenNLP;
import it.uniroma3.util.nlp.StanfordNLP;

public class Lector {
    
    private static RedirectResolver redirectResolver;
    private static DBPedia kg;
    private static TypesAssigner typesAssigner;   
    private static MarkupParser markupParser; 
    private static ArticleTyper articleTyper;
    private static XMLParser xmlParser;
    private static BlockParser blockParser;
    private static TextParser textParser;
    private static Triplifier triplifier;
    
    /*
     * Each thread uses its own specific object
     */
    private static ThreadLocal<StanfordNLP> stanfordExpert;
    private static ThreadLocal<OpenNLP> openNLPExpert;
    private static ThreadLocal<SeedFSM> fsm;

    
    /**
     * 
     * @param config
     */
    public static void init(WikiLanguage lang) {
	redirectResolver = new RedirectResolver();
	typesAssigner = new TypesAssigner();
	markupParser = new MarkupParser();
	articleTyper = new ArticleTyper(lang);
	xmlParser = new XMLParser();
	blockParser = new BlockParser(lang);
	textParser = new TextParser(lang);
	triplifier = new Triplifier();
	kg = new DBPedia();
	
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
	
	initMVLFile();
    }

    /**
     * @return the redirectResolver
     */
    public static RedirectResolver getRedirectResolver() {
        return redirectResolver;
    }
    
    /**
     * @return the redirectResolver
     */
    public static MarkupParser getMarkupParser() {
        return markupParser;
    }

    /**
     * @return the typesAssigner
     */
    public static TypesAssigner getTypesAssigner() {
        return typesAssigner;
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
    
    private static void initMVLFile(){
	File file = new File(Configuration.getMVLFile());
	if(file.exists()){
	    file.delete();
	}
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
    public static DBPedia getKg() {
        return kg;
    }
    
    

}
