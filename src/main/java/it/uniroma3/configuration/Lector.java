package it.uniroma3.configuration;

import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.ArticleTyper;
import it.uniroma3.parser.BlockParser;
import it.uniroma3.parser.MarkupParser;
import it.uniroma3.parser.TextParser;
import it.uniroma3.parser.XMLParser;
import it.uniroma3.tools.RedirectResolver;
import it.uniroma3.tools.TypesResolver;
import it.uniroma3.util.StanfordExpertNLP;

public class Lector {
    
    private static RedirectResolver redirectResolver;
    private static TypesResolver typesResolver;   
    private static MarkupParser markupParser; 
    private static ArticleTyper articleTyper;
    private static XMLParser xmlParser;
    private static BlockParser blockParser;
    private static TextParser textParser;
    
    /*
     * Each thread uses its own specific expert
     */
    private static ThreadLocal<StanfordExpertNLP> expert;

    
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
	
	expert = new ThreadLocal<StanfordExpertNLP>() {
	    @Override protected StanfordExpertNLP initialValue() {
		return new StanfordExpertNLP();
	    }
	};
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
        return expert.get();
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
     * @return the expert
     */
    public static ThreadLocal<StanfordExpertNLP> getExpert() {
        return expert;
    }

}
