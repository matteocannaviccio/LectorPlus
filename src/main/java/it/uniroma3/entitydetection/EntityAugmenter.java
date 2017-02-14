package it.uniroma3.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;

import it.uniroma3.model.WikiArticle;
import it.uniroma3.util.ExpertNLP;
/**
 * 
 * @author matteo
 *
 */
public class EntityAugmenter {

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
    public static WikiArticle detectSeeds(WikiArticle article){
	article.setSeeds(fsm.get().findSeed(article.getCleanFirstSentence()));
	return article;
    }

    /**
     * Detect the pronoun used in the article to mention the primary entities (e.g. "He" for male person).
     * It uses a pronoun density over the article, matching possible pronouns:
     * (1) 	at the beginning of the sentence
     * (2)	after a comma
     * 
     * @param article
     * @param THRESHOLD --> used to limit the usage of wrong pronouns
     * @return
     */
    private static WikiArticle detectPronoun(WikiArticle article, double THRESHOLD){
	Multiset<String> pronounsStatsTmp = TreeMultiset.create();
	Multiset<String> pronounsStats = TreeMultiset.create();

	List<String> possiblePronouns = Arrays.asList("He", "She", "It","They", "His", "Her", "Their", "Its");

	/* 
	 * for each pronoun in the list, create a specific pattern to use
	 */
	Map<String, List<String>> regexesPronouns = new HashMap<String, List<String>>(possiblePronouns.size() * 2);
	for(String pronoun : possiblePronouns){
	    List<String> regexes = new ArrayList<String>(2);
	    if (!pronoun.equals("It"))
		regexes.add("(?<=, )" + pronoun.toLowerCase() + "(?=\\b)");
	    regexes.add("(?<=(. |\\n))" + pronoun + "(?=\\b)");
	    regexesPronouns.put(pronoun, regexes);
	}

	/*
	 * collect the stats of the pronouns from each sentence of the article,
	 * using the patterns created above.
	 */
	for(Map.Entry<String, String> blocks : article.getBlocks().entrySet()){
	    for (Map.Entry<String, List<String>> regexes : regexesPronouns.entrySet()){
		for (String pattern : regexes.getValue()){
		    Matcher m = Pattern.compile(pattern).matcher(blocks.getValue());
		    if(m.find())
			pronounsStatsTmp.add(regexes.getKey());
		}
	    }
	}

	/*
	 * group the stats of the pronouns by personal and impersonal pronouns 
	 */
	int total = 0;
	for(String pronoun : pronounsStatsTmp.elementSet()){
	    if (pronoun.equals("He") || pronoun.equals("His")){
		pronounsStats.add("He", pronounsStatsTmp.count(pronoun));
		total += pronounsStatsTmp.count(pronoun);
	    }else if (pronoun.equals("She") || pronoun.equals("Her")){
		pronounsStats.add("She", pronounsStatsTmp.count(pronoun));
		total += pronounsStatsTmp.count(pronoun);
	    }else if (pronoun.equals("It") || pronoun.equals("Its")){
		pronounsStats.add("It", pronounsStatsTmp.count(pronoun));
		total += pronounsStatsTmp.count(pronoun);
	    }else if (pronoun.equals("They") || pronoun.equals("Their")){
		pronounsStats.add("They", pronounsStatsTmp.count(pronoun));
		total += pronounsStatsTmp.count(pronoun);
	    }
	}

	/*
	 * calculate the stats for each pronouns and return the one that is over a THRESHOLD
	 */
	String pronoun = "-";
	if(!pronounsStats.isEmpty()){
	    String candidate = Multisets.copyHighestCountFirst(pronounsStats).asList().get(0);
	    double reliability = (double)pronounsStats.count(candidate)/total;
	    if(reliability > THRESHOLD)
		pronoun = candidate;
	}

	article.setPronoun(pronoun);
	return article;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static Map<String, String> getSeedRegex(WikiArticle article){
	Map<String, String> regexes = new HashMap<String, String>(article.getSeeds().size());
	for(String seed : article.getSeeds())
	    if (!seed.equals("-"))
		regexes.put("(?<=\\b)(t|T)he " + seed.toLowerCase() + "(?=\\b)", article.getPrimaryTag());
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static Map<String, String> getNameRegex(WikiArticle article){
	Map<String, String> regexes = new HashMap<String, String>();
	regexes.put("(?<=\\b)" + article.getTitle() + "(?=\\b)", article.getPrimaryTag());
	for(String alias : article.getAliases())
	    regexes.put("(?<=\\b)" + alias + "(?=\\b)", article.getPrimaryTag());
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static Map<String, String> getPronounRegex(WikiArticle article){
	Map<String, String> regexes = new HashMap<String, String>();
	String pronoun = article.getPronoun();
	if(!pronoun.equals("-")){
	    if (!pronoun.equals("It"))
		regexes.put("(?<=, )" + pronoun.toLowerCase() + "(?=\\b)", article.getPrimaryTag());
	    regexes.put("(?<=(. |\\n))" + pronoun + "(?=\\b)", article.getPrimaryTag());
	}
	return regexes;
    }


    /**
     * 
     * @param article
     * @return
     */
    private static Map<String, String> getSecondaryEntitiesRegex(WikiArticle article){
	Map<String, String> regexes2entity = new HashMap<String, String>();
	Map<String, Set<String>> wikilinks = article.getWikilinks();

	for(Map.Entry<String, Set<String>> sec_ent : wikilinks.entrySet()){
	    for (String possibleName : sec_ent.getValue()){
		regexes2entity.put("(?<=\\b)" + Pattern.quote(possibleName) + "(?=\\b)", "[[##" + sec_ent.getKey() + "##]]");
	    }
	}
	return regexes2entity;
    }

    /**
     * 
     * @param article
     * @return
     */
    public static WikiArticle findPrimaryEntitiesHooks(WikiArticle article){
	article = detectSeeds(article);
	article = detectPronoun(article, 0.7);
	return article;
    }

    /**
     * 
     * @param sentence
     * @param patterns
     * @return
     */
    public static String applyRegex(WikiArticle article, String sentence, String replacement, String pattern){
	try{ 
	    sentence = sentence.replaceAll(pattern, replacement);
	}catch(Exception e){
	    System.out.println(article);
	}

	return sentence;
    }


    /**
     * 
     * 
     * @param article
     * @return
     */
    public static WikiArticle augmentEntities(WikiArticle article){
	/*
	 * Collect all the patterns for Primary Entity (PE)
	 */
	TreeMap<String, String> regex2entity = new TreeMap<String, String>(new PatternComparator());
	regex2entity.putAll(getNameRegex(article));
	regex2entity.putAll(getPronounRegex(article));
	regex2entity.putAll(getSeedRegex(article));

	/*
	 * Collect all the patterns for Secondary Entities (SE)
	 */
	regex2entity.putAll(getSecondaryEntitiesRegex(article));

	/*
	 * Run everything!
	 */
	for(Map.Entry<String, String> section : article.getBlocks().entrySet())
	    for(Map.Entry<String, String> regex : regex2entity.entrySet())
		article.getBlocks().put(section.getKey(), applyRegex(article, section.getValue(), regex.getValue(), regex.getKey()));

	return article;

    }

}
