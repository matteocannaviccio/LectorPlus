package it.uniroma3.main.pipeline.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.collect.TreeMultiset;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.config.WikiLanguage.Lang;
import it.uniroma3.main.bean.WikiArticle;
/**
 * 
 * @author matteo
 *
 */
public class ReplFinder {

    /**
     * Run the FSM to detect the seeds in the articles.
     * 
     * @param article
     * @return
     */
    private List<String> findSeeds(WikiArticle article){
	return Lector.getFsm().findSeed(article.getFirstSentence());
    }

    /**
     * Run a simple nationality-detector on the first sentence.
     * 
     * @return
     */
    private String findNationality(WikiArticle article){
	return Lector.getFsmNat().detectNationality(article.getFirstSentence());
    }

    /**
     * Detect the pronoun used in the article to mention 
     * the primary entities: (e.g. find "He" for male person).
     * 
     * It uses a pronoun density over the article, 
     * matching possible pronouns:
     * (1) 	at the beginning of the sentence
     * (2)	after a comma
     * 
     * @param article
     * @param THRESHOLD --> used to limit the usage of wrong pronouns
     * @return
     */
    private String findPronoun(WikiArticle article, double THRESHOLD){
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
		regexes.add("(?<=, )(?<!<[A-Z-]<)\\b" + pronoun.toLowerCase() + "\\b(?![^<]*?>>)");
	    regexes.add("(?<=\\. |\\n)(?<!<[A-Z-]<)\\b" + pronoun + "\\b(?![^<]*?>>)");
	    regexesPronouns.put(pronoun, regexes);
	}

	/*
	 * collect the stats of the pronouns from each sentence of the article,
	 * using the patterns created above. While there is a match with that specific regex
	 * add the pronoun in the stats.
	 */
	for (Map.Entry<String, List<String>> regexes : regexesPronouns.entrySet()){
	    for (String pattern : regexes.getValue()){
		Matcher m = Pattern.compile(pattern).matcher(article.getWholeText());
		while(m.find())
		    pronounsStatsTmp.add(regexes.getKey());
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
	String pronoun = null;
	if(!pronounsStats.isEmpty()){
	    String candidate = Multisets.copyHighestCountFirst(pronounsStats).asList().get(0);
	    double reliability = (double)pronounsStats.count(candidate)/total;
	    if(reliability > THRESHOLD)
		pronoun = candidate;
	}

	return pronoun;
    }

    /**
     * Obtains the longest substring between the title, 
     * the aliases and all the sentences.
     * 
     * @param article
     * @param THRESHOLD
     * @return
     */
    private String findSubNames(WikiArticle article, double THRESHOLD){
	Multiset<String> stats = obtainStatsSubNames(article);
	int total = 0;
	for(String subName : stats.elementSet()){
	    total += stats.count(subName);
	}
	/*
	 * calculate the stats for each pronouns and return the one that is over a THRESHOLD
	 */
	String subName = null;
	if(!stats.isEmpty()){
	    String candidate = Multisets.copyHighestCountFirst(stats).asList().get(0);
	    double reliability = (double)stats.count(candidate)/total;
	    if(reliability > THRESHOLD)
		subName = candidate;
	}
	return subName;
    }

    /**
     * 
     * @param article
     * @return
     */
    private Multiset<String> obtainStatsSubNames(WikiArticle article){
	Multiset<String> subnamesStats = TreeMultiset.create();
	List<String> names = new ArrayList<String>();
	names.add(article.getTitle());
	names.addAll(article.getAliases());
	for(Map.Entry<String, String> block : article.getBlocks().entrySet()){
	    for (String sentence : block.getValue().split("(\\.\\s|\\.\\n)")){
		for (String wholeName : names){
		    wholeName = wholeName.replaceAll("(\\s|_)?'*(\\([^\\(]*?\\))'*", "");
		    // update stats with subsequences
		    for (String lcs : longestCommonSubstrings(wholeName, sentence)){
			// we consider only substrings with at least one capital letter
			if (!lcs.toLowerCase().equals(lcs))
			    subnamesStats.add(lcs);
		    }
		}
	    }
	}
	return subnamesStats;
    }

    /**
     * 
     * @param s
     * @param t
     * @return
     */
    private Set<String> longestCommonSubstrings(String s, String t) {
	String[] sAr = s.toLowerCase().split("\\s");
	String[] tAr = t.toLowerCase().split("\\s");
	int[][] table = new int[sAr.length][tAr.length];
	int longest = 0;
	Set<String> result = new HashSet<String>();

	for (int i = 0; i < sAr.length; i++) {
	    for (int j = 0; j < tAr.length; j++) {
		if (!sAr[i].equals(tAr[j])) {
		    continue;
		}
		table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
		if (table[i][j] > longest) {
		    longest = table[i][j];
		    result.clear();
		}
		if (table[i][j] == longest)
		    result.add(String.join(" ", Arrays.copyOfRange(s.split("\\s"), i - longest + 1, i + 1)));
	    }
	}
	return result;
    }


    /**
     * 
     * @param wikid
     * @return
     */
    private String getDisambiguation(String wikid){
	String disambiguation = null;
	Pattern DISAMBIGATION = Pattern.compile("_\\(.*\\)$");
	Matcher m = DISAMBIGATION.matcher(wikid);
	if (m.find()){
	    disambiguation = m.group(0).replaceAll("(_\\(|\\))*", "").trim();
	}
	return disambiguation;
    }


    /**
     * This is the main part of this step: find all possible 
     * replacements to augment instances of primary entity.
     * 
     * Here we look for the following replacements:
     * - seeds
     * - pronoun			
     * - subnames 	(only named-entities NE)
     * 
     * We use this constraint to determine if it is a named entity (NE):
     * - the article needs to have at least one alias (bold names)
     * This allows to avoid finding subnames for articles such as "List of 
     * Albanian lakes", which should not have a bold name in the first sentence.
     * 
     * Also, the subname can not override one of the secondary 
     * entities (for constraint).
     * 
     * 
     * @param article
     * @return
     */
    public WikiArticle increaseEvidence(WikiArticle article){
	try{

	    /*
	     * Store the first clean sentence of the article and clean it, 
	     * in order to extract seed types and run NLP tools.
	     */
	    String firstSentence = Lector.getTextParser().obtainCleanFirstSentence(Lector.getBlockParser().getAbstractSection(article.getBlocks()));
	    article.setFirstSentence(firstSentence);
	    article.setNationality(findNationality(article));

	    /*
	     * Pronouns and seeds are used only in ENGLISH.
	     */
	    if (Lector.getWikiLang().getLang().equals(Lang.en)){
		if (!article.getFirstSentence().equals("-"))
		    article.setSeeds(findSeeds(article));
		article.setPronoun(findPronoun(article, Configuration.getPronounThreshold()));
	    }

	    /*
	     * we assign subnames only to articles that describe named entities.
	     * we check it using the presence of an alias in the first sentence.
	     */
	    if (Lector.getWikiLang().getLang().equals(Lang.en)){
		article.setDisambiguation(getDisambiguation(article.getWikid()));
		if (!article.getAliases().isEmpty()){
		    String candidateSubname = findSubNames(article, Configuration.getSubnameThreshold());
		    article.setSubName(candidateSubname);
		}
	    }else{
		String candidateSubname = findSubNames(article, Configuration.getSubnameThreshold());
		article.setSubName(candidateSubname);
	    }

	}catch(Exception e){
	    e.printStackTrace();
	    System.out.println("Error in Entity Detection(finding replacements) article:  " + article.getWikid());
	}

	return article;
    }


}
