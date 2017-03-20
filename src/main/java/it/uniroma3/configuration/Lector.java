package it.uniroma3.configuration;

import it.uniroma3.tools.RedirectResolver;
import it.uniroma3.tools.TypesResolver;
import it.uniroma3.util.StanfordExpertNLP;

public class Lector {
    
    private static RedirectResolver redirectResolver;
    private static TypesResolver typesResolver;    
    
    /*
     * Each thread uses its own specific expert
     */
    private static ThreadLocal<StanfordExpertNLP> expert;

    
    /**
     * 
     * @param config
     */
    public static void init() {
	redirectResolver = new RedirectResolver();
	typesResolver = new TypesResolver();
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

}
