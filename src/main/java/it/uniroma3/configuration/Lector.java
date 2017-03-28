package it.uniroma3.configuration;

import java.io.File;

import it.uniroma3.entitydetection.SeedFSM;
import it.uniroma3.kg.DBPedia;
import it.uniroma3.kg.RedirectResolver;
import it.uniroma3.kg.TypesResolver;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.ArticleTyper;
import it.uniroma3.parser.BlockParser;
import it.uniroma3.parser.MarkupParser;
import it.uniroma3.parser.TextParser;
import it.uniroma3.parser.XMLParser;
import it.uniroma3.triples.Triplifier;
import it.uniroma3.util.ExpertNLP;
import it.uniroma3.util.StanfordExpertNLP;

public class Lector {
    
    private static RedirectResolver redirectResolver;
    private static DBPedia kg;
    private static TypesResolver typesResolver;   
    private static MarkupParser markupParser; 
    private static ArticleTyper articleTyper;
    private static XMLParser xmlParser;
    private static BlockParser blockParser;
    private static TextParser textParser;
    private static Triplifier triplifier;
    
    /*
     * Each thread uses its own specific object
     */
    private static ThreadLocal<StanfordExpertNLP> stanfordExpert;
    private static ThreadLocal<ExpertNLP> openNLPExpert;
    private static ThreadLocal<SeedFSM> fsm;

    
    /**
     * 
     * @param config
     */
    public static void init(WikiLanguage lang) {
	redirectResolver = new RedirectResolver();
	typesResolver = new TypesResolver();
	markupParser = new MarkupParser();
	articleTyper = new ArticleTyper(lang);
	xmlParser = new XMLParser();
	blockParser = new BlockParser(lang);
	textParser = new TextParser(lang);
	triplifier = new Triplifier();
	kg = new DBPedia();
	
	stanfordExpert = new ThreadLocal<StanfordExpertNLP>() {
	    @Override protected StanfordExpertNLP initialValue() {
		return new StanfordExpertNLP();
	    }
	};
	
	openNLPExpert = new ThreadLocal<ExpertNLP>() {
	    @Override protected ExpertNLP initialValue() {
		return new ExpertNLP();
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
     * @return the typesResolver
     */
    public static TypesResolver getTypesResolver() {
        return typesResolver;
    }
    
    /**
     * @return the typesResolver
     */
    public static StanfordExpertNLP getNLPExpert() {
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
    public static ThreadLocal<ExpertNLP> getOpenNLPExpert() {
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
