package it.uniroma3.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    private static List<String> findSeeds(WikiArticle article){
	List<String> seeds = fsm.get().findSeed(article.getFirstSentence());
	return seeds;
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
    private static String detectPronoun(WikiArticle article, double THRESHOLD){
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
		regexes.add("(?<=, )(?<![A-Z-]<)\\b" + pronoun.toLowerCase() + "\\b(?![^<]*?>)");
	    regexes.add("(?<=\\. |\\n)(?<![A-Z-]<)\\b" + pronoun + "\\b(?![^<]*?>)");
	    regexesPronouns.put(pronoun, regexes);
	}

	/*
	 * collect the stats of the pronouns from each sentence of the article,
	 * using the patterns created above. While there is a match with that specific regex
	 * add the pronoun in the stats.
	 */
	for (Map.Entry<String, List<String>> regexes : regexesPronouns.entrySet()){
	    for (String pattern : regexes.getValue()){
		Matcher m = Pattern.compile(pattern).matcher(article.getOriginalMarkup());
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

    /* ********************************************************************************************** */

    /**
     * 
     * @param s
     * @param t
     * @return
     */
    public static Set<String> longestCommonSubstrings(String s, String t) {
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
     * Obtains the longest substring between the title, the aliases and all the sentences.
     * 
     * @param article
     * @return
     */
    private static Multiset<String> obtainStatsSubNames(WikiArticle article){
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
     * @param article
     * @return
     */
    private static String findPossibleSubNames(WikiArticle article, double THRESHOLD){
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


    /* ********************************************************************************************** */

    /**
     * To match a name it has to be in a sentence sourrounded by two boarders (\\b) that are
     * not square bracket, _ or pipe | (which are terms that are inside a wikilink).
     * 
     * https://regex101.com/r/qdZyYl/4
     * 
     * @param name
     * @return
     */
    private static String createRegexName(String name){
	return "(\\s[^\\sA-Z]++\\s|(?:^|\\. |\\n)(?:\\w++\\s)?)(\\b" + Pattern.quote(name) + "\\b)(?!\\s[A-Z][a-z]++|-|<)";
    }

    /**
     * To match a name it has to be in a sentence sourrounded by two boarders (\\b) that are
     * not square bracket, _ or pipe | (which are terms that are inside a wikilink).
     * @param name
     * @return
     */
    private static String createRegexSeed(String name){
	return "((?<![A-Z-]<)\\b)(" + Pattern.quote(name) + ")\\b(?![^<]*?>)";
    }

    
    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getPronounRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	String pronoun = article.getPronoun();
	if(pronoun != null){
	    if (!pronoun.equals("It"))
		regexes.add(Pair.make("((?<=\\, )(?<![A-Z-]<)\\b)(" + Pattern.quote(pronoun.toLowerCase()) + ")\\b(?![^<]*?>)", "PE-PRON<" + article.getWikid() + ">"));
	    regexes.add(Pair.make("((?<=\\. |\\n|^)(?<![A-Z-]<)\\b)(" + Pattern.quote(pronoun) + ")\\b(?![^<]*?>)", "PE-PRON<" + article.getWikid() + ">"));
	}
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getSeedRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	for(String seed : article.getSeeds()){
	    if (seed != null){
		regexes.add(Pair.make(createRegexSeed("the " + seed.toLowerCase()), "PE-SEED<" + article.getWikid() + ">"));
		regexes.add(Pair.make(createRegexSeed("The " + seed.toLowerCase()), "PE-SEED<" + article.getWikid() + ">"));
	    }
	}
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getDisambiguationRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	String disamb = article.getDisambiguation();
	if (disamb != null){
	    regexes.add(Pair.make(createRegexSeed("the " + disamb.toLowerCase()), "PE-DISAMB<" + article.getWikid() + ">"));
	    regexes.add(Pair.make(createRegexSeed("The " + disamb.toLowerCase()), "PE-DISAMB<" + article.getWikid() + ">"));
	}
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private static List<Pair<String, String>> getNameRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	regexes.add(Pair.make(createRegexName(article.getTitle()), "PE-TITLE<" + article.getWikid() + ">"));
	for(String alias : article.getAliases())
	    regexes.add(Pair.make(createRegexName(alias), "PE-ALIAS<" + article.getWikid() + ">"));
	if (article.getSubName() != null)
	    regexes.add(Pair.make(createRegexName(article.getSubName()), "PE-SUBTITLE<" + article.getWikid() + ">"));
	return regexes;
    }


    /**
     * 
     * @param article
     * @return
     */
    private static Set<Pair<String, String>> getSecondaryEntitiesRegex(WikiArticle article){
	Set<Pair<String, String>> regexes2secentity = new HashSet<Pair<String, String>>();
	for(Map.Entry<String, Set<String>> sec_ent : article.getWikilinks().entrySet()){
	    for (String possibleName : sec_ent.getValue()){
		Pair<String, String> p = Pair.make(createRegexName(sec_ent.getKey()), possibleName);
		regexes2secentity.add(p);
	    }
	}
	return regexes2secentity;
    }

    /**
     * 
     * @param sentence
     * @param patterns
     * @return
     * @throws Exception 
     */
    private static String applyRegex(WikiArticle article, String sentence, String replacement, String pattern) throws Exception{
	StringBuffer tmp = new StringBuffer();
	try{ 
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(sentence);
	    while (m.find()){
		m.appendReplacement(tmp, Matcher.quoteReplacement(m.group(1)) + Matcher.quoteReplacement(replacement));
	    }
	    m.appendTail(tmp);

	}catch(Exception e){
	    e.printStackTrace();
	    throw new Exception();
	}
	return tmp.toString();
    }


    /**
     * 
     * 
     * @param article
     * @return
     */
    public static WikiArticle augmentEntities(WikiArticle article){

	article.setSeeds(findSeeds(article));
	article.setPronoun(detectPronoun(article, 0.5));
	/*
	 * Here we use this constraint:
	 * an article, in order to describe a named entity, needs to have at least one alias (bold names).
	 * Also, the subname can not override one of the secondary entities (for constraint) and
	 * the title or the aliases (for performance).
	 * 
	 */
	if (!article.getAliases().isEmpty()){
	    String candidateSubname = findPossibleSubNames(article, 0.5);
	    if (!article.getWikilinks().containsKey(candidateSubname) &&
		    !article.getTitle().equals(candidateSubname) &&
		    !article.getSeeds().contains(candidateSubname))
		article.setSubName(candidateSubname);
	}

	/*
	 * Collect all the patterns for Primary Entity (PE)
	 */
	List<Pair<String, String>> regex2entity = new ArrayList<Pair<String, String>>();
	Set<Pair<String, String>> primaryEntityNames = new HashSet<Pair<String, String>>();
	primaryEntityNames.addAll(getNameRegex(article));
	primaryEntityNames.addAll(getPronounRegex(article));
	primaryEntityNames.addAll(getSeedRegex(article));
	primaryEntityNames.addAll(getDisambiguationRegex(article));
	regex2entity.addAll(primaryEntityNames);

	/*
	 * Adds a Secondary Entity (SE) only if it does not have a conflict of name with the primary entity! 
	 */
	for (Pair<String, String> secondaryEntity : getSecondaryEntitiesRegex(article)){
	    boolean createsConflict = false;
	    for (Pair<String, String> possiblePrimaryEntityName : primaryEntityNames){
		if (secondaryEntity.key.equals(possiblePrimaryEntityName.key)){
		    createsConflict = true;
		    break;
		}
	    }
	    if(!createsConflict){
		regex2entity.add(secondaryEntity);
	    }
	}

	/*
	 * Sort it.
	 */
	Collections.sort(regex2entity, new PatternComparator()); 

	/*
	 * Run everything!
	 */

	for(Map.Entry<String, String> block : article.getBlocks().entrySet()){
	    /*
	     * calcola le entita e dividi in frasi, poi applia le regex
	     */
	    for(Pair<String, String> regex : regex2entity){
		try{

		    article.getBlocks().put(block.getKey(), applyRegex(article, block.getValue(), regex.value, regex.key));

		}catch(Exception e){
		    System.out.println("Exception in:	" + article.getWikid());
		    System.out.println("Sentence:	" + block.getValue());
		    System.out.println("occurred for entity:	" + regex.value);
		    System.out.println("using the regex:	" + regex.key);
		    System.out.println("--------------------------------------------------");
		    break;
		}
	    }
	}
	return article;

    }

}
