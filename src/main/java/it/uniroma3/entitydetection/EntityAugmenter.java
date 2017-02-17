package it.uniroma3.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;

import it.uniroma3.model.WikiArticle;
import it.uniroma3.util.ExpertNLP;
import it.uniroma3.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class EntityAugmenter {

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
    private static WikiArticle detectSeeds(WikiArticle article){
	article.setSeeds(fsm.get().findSeed(article.getCleanFirstSentence()));
	return article;
    }

    /**
     * Detect the pronoun used in the article to mention the primary entities 
     * (e.g. find "He" for male person).
     * 
     * It uses a pronoun density over the article, matching possible pronouns:
     * (1) 	at the beginning of the sentence
     * (2)	after a comma
     * 
     * @param article
     * @param THRESHOLD --> used to limit the usage of wrong pronouns
     * @return
     */
    public static WikiArticle detectPronoun(WikiArticle article, double THRESHOLD){
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
		regexes.add("(?<=, )" + pronoun.toLowerCase() + "(?=[^\\]])(?=\\b)");
	    regexes.add("(?<=(. |\\n))" + pronoun + "(?=[^\\]])(?=\\b)");
	    regexesPronouns.put(pronoun, regexes);
	}

	/*
	 * collect the stats of the pronouns from each sentence of the article,
	 * using the patterns created above. While there is a match with that specific regex
	 * add the pronoun in the stats.
	 */
	for(Map.Entry<String, String> blocks : article.getBlocks().entrySet()){
	    for (Map.Entry<String, List<String>> regexes : regexesPronouns.entrySet()){
		for (String pattern : regexes.getValue()){
		    Matcher m = Pattern.compile(pattern).matcher(blocks.getValue());
		    while(m.find())
			pronounsStatsTmp.add(regexes.getKey());
		}
	    }
	}

	/*
	 * group the stats of the pronouns by personal and impersonal pronouns
	 * and use reinforcements to add more evidence for each pronoun
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
    private static List<Pair<String, String>> getSeedRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	for(String seed : article.getSeeds())
	    if (!seed.equals("-"))
		regexes.add(Pair.make("(?<=\\b)(?<=[^\\[])(t|T)he " + seed.toLowerCase() + "(?=[^\\]])(?=\\b)", article.getPrimaryTag()));
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getNameRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	regexes.add(Pair.make("(?<=\\b)(?<=[^\\[])" + article.getTitle() + "(?=[^\\]])(?=\\b)", article.getPrimaryTag()));
	for(String alias : article.getAliases())
	    regexes.add(Pair.make("(?<=\\b)(?<=[^\\[])" + alias + "(?=[^\\]])(?=\\b)", article.getPrimaryTag()));
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getPronounRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	String pronoun = article.getPronoun();
	if(!pronoun.equals("-")){
	    if (!pronoun.equals("It"))
		regexes.add(Pair.make("(?<=, )" + pronoun.toLowerCase() + "(?=[^\\]])(?=\\b)", article.getPrimaryTag()));
	    regexes.add(Pair.make("(?<=(. |\\n|^))" + pronoun + "(?=[^\\]])(?=\\b)", article.getPrimaryTag()));
	}
	return regexes;
    }


    /**
     * 
     * @param article
     * @return
     */
    private static TreeSet<Pair<String, String>> getSecondaryEntitiesRegex(WikiArticle article){
	TreeSet<Pair<String, String>> regexes2entity = new TreeSet<Pair<String, String>>(new PatternComparator());

	for(Map.Entry<String, Set<String>> sec_ent : article.getWikilinks().entrySet()){
	    for (String possibleName : sec_ent.getValue()){
		Pair<String, String> p = Pair.make("(?<=\\b)(?<=[^\\[])" + Pattern.quote(sec_ent.getKey()) + "(?=[^\\]])(?=\\b)", "[[##" + possibleName + "##]]");
		regexes2entity.add(p);
	    }
	}
	return regexes2entity;
    }

    /**
     * 
     * @param sentence
     * @param patterns
     * @return
     * @throws Exception 
     */
    private static String applyRegex(WikiArticle article, String sentence, String replacement, String pattern) throws Exception{
	try{ 
	    
	    sentence = sentence.replaceAll(pattern, Matcher.quoteReplacement(replacement));

	}catch(Exception e){	    
	    throw new Exception();
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

	article = detectSeeds(article);
	article = detectPronoun(article, 0.7);

	/*
	 * Collect all the patterns for Primary Entity (PE)
	 */
	ConcurrentSkipListSet<Pair<String, String>> regex2entity = new ConcurrentSkipListSet<Pair<String, String>>(new PatternComparator());
	regex2entity.addAll(getNameRegex(article));
	regex2entity.addAll(getPronounRegex(article));
	regex2entity.addAll(getSeedRegex(article));
	regex2entity.addAll(getSecondaryEntitiesRegex(article));

	/*
	 * Run everything!
	 */
	for(Map.Entry<String, String> section : article.getBlocks().entrySet())
	    for(Pair<String, String> regex : regex2entity){
		try{
		article.getBlocks().put(section.getKey(), applyRegex(article, section.getValue(), regex.value, regex.key));
		}catch(Exception e){
		    System.out.println("Exception in:	" + article.getWikid());
		    System.out.println("occurred for entity:	" + regex.value);
		    System.out.println("using the regex:	" + regex.key);
		    System.out.println("--------------------------------------------------");
		    break;
		}
	    }

	return article;

    }

}
