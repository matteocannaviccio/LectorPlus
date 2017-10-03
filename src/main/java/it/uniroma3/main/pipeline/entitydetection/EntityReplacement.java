package it.uniroma3.main.pipeline.entitydetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiArticle;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.nlp.StupidNLP;
/**
 * 
 * @author matteo
 *
 */
public class EntityReplacement {
    
    /**
     * To match a name it has to be in a sentence surrounded by two boarders (\\b) that are
     * not square bracket, _ or pipe | (which are terms that are inside a wiki-link).
     * <p>
     * 
     * https://regex101.com/r/afAe9X/1
     * 
     * The reg-ex matches a WORD that:
     * 
     * follows:
     *  - \\s[^\\sA-Z]++\\s			a space + a lower-case token (with or without numbers and special characters) + a space
     *  	   OR
     *  - (?:^|\\. |, |: |\\n)(?:\\w++\\s)?	a word (lower or upper case) that follows a period, a new-line, a column, or a comma + a space
     * 
     * is not followed by:
     *  - \\s[A-Z][a-z]++			a space + upper-case word
     *  - -					a dash
     *  - <| < 					the direct start of an annotated entity
     *  - [^>]*>> 				the direct end of the entity itself
     *
     * @param name
     * @return
     */
    public static String createRegexName(String name) {
	return "(" + "\\s[^\\sA-Z]++\\s" + "|" + "(?:^|\\. |, |: |\\n)(?:\\w++\\s)?"  + ")"    +   "\\b(" + Pattern.quote(name) + ")\\b"  +   "(?!\\s[A-Z][a-z]++|-|<| <|[^>]*>>)";
    }

    /**
     * To match a seed it has to be preceded by a determiner (the) and can not be followed by
     * a term with a capital letter or an other entity.
     * TODO: english specific rule.
     * 
     * @param article
     * @return
     */
    private List<Pair<String, String>> getSeedRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	if (article.getSeeds() != null){
	    for(String seed : article.getSeeds()){
		if (seed != null){
		    regexes.add(Pair.make(createRegexSeed("the " + seed.toLowerCase()), "<<PE-SEED><" + article.getWikid() + "><the " + seed +">>"));
		    regexes.add(Pair.make(createRegexSeed("The " + seed.toLowerCase()), "<<PE-SEED><" + article.getWikid() + "><The " + seed +">>"));
		}
	    }
	}
	return regexes;
    }

    /**
     * To match a seed it has to be preceded by a determiner (the) and can not be followed by
     * a term with a capital letter or an other entity.
     * 
     * @param name
     * @return
     */
    private String createRegexSeed(String name){
	return "((?<!<[A-Z-]<)\\b)(" + Pattern.quote(name) + ")\\b(?![^<]*?>>|\\s[A-Z][a-z]++|-|<| <)";
    }


    /**
     * 
     * @param article
     * @return
     */
    private List<Pair<String, String>> getPronounRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	String pronoun = article.getPronoun();
	if(pronoun != null){
	    if (!pronoun.equals("It") && !pronoun.equals("They")){
		regexes.add(Pair.make("((?<=\\s)(?<!<[A-Z-]<)\\b)(" + Pattern.quote(pronoun.toLowerCase()) + ")\\b(?![^<]*?>>)", "<<PE-PRON><" + article.getWikid() + "><" + pronoun.toLowerCase() +">>"));
		regexes.add(Pair.make("(^|\\.\\s)(" + Pattern.quote(pronoun) + ")\\b(?![^<]*?>>)", "<<PE-PRON><" + article.getWikid() + "><" + pronoun +">>"));
	    }
	}
	return regexes;
    }


    /**
     * 
     * @param article
     * @return
     */
    private List<Pair<String, String>> getDisambiguationRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	String disamb = article.getDisambiguation();
	if (disamb != null){
	    regexes.add(Pair.make(createRegexSeed("the " + disamb.toLowerCase()), "<<PE-DISAMB><" + article.getWikid() + "><the " + disamb + ">>"));
	    regexes.add(Pair.make(createRegexSeed("The " + disamb.toLowerCase()), "<<PE-DISAMB><" + article.getWikid() + "><The " + disamb + ">>"));
	}
	return regexes;
    }

    /**
     * 
     * @param article
     * @return
     */
    private List<Pair<String, String>> getNameRegex(WikiArticle article){
	List<Pair<String, String>> regexes = new ArrayList<Pair<String, String>>();
	
	regexes.add(Pair.make(createRegexName(article.getTitle()), "<<PE-TITLE><" + article.getWikid() + "><" + article.getTitle() + ">>"));
	
	for(String alias : article.getAliases())
	    if (!alias.equals(article.getTitle()))
		regexes.add(Pair.make(createRegexName(alias), "<<PE-ALIAS><" + article.getWikid() + "><" + alias + ">>"));
	
	if (article.getSubName() != null && !article.getSubName().equals(article.getTitle()) )
	    regexes.add(Pair.make(createRegexName(article.getSubName()), "<<PE-SUBTITLE><" + article.getWikid() + "><" + article.getSubName() + ">>"));
	
	return regexes;
    }


    /**
     * 
     * @param article
     * @return
     */
    private Set<Pair<String, String>> getSecondaryEntitiesRegex(WikiArticle article){
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
     * @param article
     * @param sentence
     * @param replacement
     * @param pattern
     * @return
     * @throws Exception
     */
    private static String applyRegex(WikiArticle article, String sentence, String replacement, String pattern) throws Exception{
	StringBuffer tmp = new StringBuffer();
	try{ 
	    Pattern p = Pattern.compile(pattern);
	    Matcher m = p.matcher(sentence);
	    while (m.find()){
		// we attached the part of text before the entities (m.group(1)) and then the entity replaced.
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
    public WikiArticle augmentEvidence(WikiArticle article){

	try{
	    /*
	     * Collect all the patterns for Primary Entity (PE)
	     */
	    List<Pair<String, String>> regex2entity = new ArrayList<Pair<String, String>>();
	    Set<Pair<String, String>> primaryEntityNames = new HashSet<Pair<String, String>>();

	    // add all of those if they exist (check if they are not null)
	    primaryEntityNames.addAll(getNameRegex(article));    
	    primaryEntityNames.addAll(getSeedRegex(article));		 // lang dependent
	    primaryEntityNames.addAll(getDisambiguationRegex(article));  // lang dependent
	    primaryEntityNames.addAll(getPronounRegex(article));	 // lang dependent
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
	     * Sort them.
	     */
	    Collections.sort(regex2entity, new PatternComparator()); 

	    /*
	     * Run everything!
	     */
	    for(Map.Entry<String, String> block : article.getBlocks().entrySet()){
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

		/*
		 * HERE WE SPLIT BLOCKS INTO SENTENCES!
		 * If we are using DBpedia Spotlight, detect all the missing entities and split the 
		 * sentences inside that method, otherwise split the sentences only.
		 */
		if (Configuration.useDBpediaSpotlight())
		    article.getSentences().put(block.getKey(), Lector.getDBSpot().annotateText(block.getValue(), article.getWikid()));
		else
		    article.getSentences().put(block.getKey(), StupidNLP.splitInSentence(block.getValue()));
		//article.getSentences().put(block.getKey(), Lector.getNLPExpert().processBlock(block.getValue()));


	    }
	}catch(Exception e){
	    e.printStackTrace();
	    System.out.println("Error in Entity Detection(apply replacements) on article:  " + article.getWikid());
	}

	return article;

    }
}
