package it.uniroma3.components;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniroma3.entitydetection.SeedFSM;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.util.ExpertNLP;
/**
 * 
 * @author matteo
 *
 */
public class TypesMapperCreator {

    /*
     * Each thread uses its own specific fsm
     */
    private static final ThreadLocal<SeedFSM> fsm =
	    new ThreadLocal<SeedFSM>() {
	@Override protected SeedFSM initialValue() {
	    return new SeedFSM(new ExpertNLP());
	}
    };

    /**
     * Run the FSM to detect the seeds in the articles.
     * 
     * @param article
     * @return
     */
    private static List<String> findSeeds(WikiArticle article){
	List<String> seeds = fsm.get().findSeed(article.getFirstSentence());
	return seeds;
    }


    /**
     * 
     * 
     * @param article
     * @return
     */
    public static WikiArticle mapSeedsToTypes(WikiArticle article){
	List<String> seeds = findSeeds(article);
	Set<String> types = new HashSet<String>(TypesResolver.assignTypes(article.getWikid()));
	article.setTypes(new ArrayList<String>(types));
	article.setSeeds(seeds);
	return article;

    }

}
