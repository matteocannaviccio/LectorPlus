package it.uniroma3.parser;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.model.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class BlockParser {

    private Cleaner cleaner;
    private WikiLanguage lang;

    /**
     * 
     * @param lang
     */
    public BlockParser(WikiLanguage lang){
	this.lang = lang;
	this.cleaner = new Cleaner(lang);
    }

    /**
     * 
     * @param text
     * @return
     */
    public String getAbstractSection(Map<String, String> blocks){
	String ABSTRACT = "#Abstract";
	String abstractSection = "-";
	if (blocks.containsKey(ABSTRACT))
	    abstractSection = blocks.get(ABSTRACT);
	return abstractSection;
    }

    /**
     * Fragment the article in multiple (sub)sections.
     * 
     * @param text
     * @return
     * @throws Exception 
     */
    public Map<String, String> fragmentArticle(String text){

	/*
	 * Then, fragment the article in blocks
	 */
	String ABSTRACT = "#Abstract";
	String regex_first = "(?m)^==\\s?([^=]+)\\s?==$";	// h1
	String regex_second = "(?m)^===\\s?([^=]+)\\s?===$";	// h2
	String regex_third = "(?m)^====\\s?([^=]+)\\s?====$";	// h3

	// content --> first sections
	Map<String, String> first_sections = getBlocksFromContent(text, regex_first, ABSTRACT, "#");

	// first sections --> second sections
	Map<String, String> second_sections = new LinkedHashMap<String, String>();
	for(Map.Entry<String, String> entry : first_sections.entrySet()){
	    String head = entry.getKey();
	    String cont = entry.getValue();
	    Map<String, String> tmp = getBlocksFromContent(cont, regex_second, ABSTRACT, "#");
	    for(Map.Entry<String, String> entries : tmp.entrySet()){
		if (!entries.getKey().equals(ABSTRACT))
		    second_sections.put(entries.getKey(), entries.getValue());
		else
		    second_sections.put(head, entries.getValue());
	    }
	}

	// second sections --> third sections
	Map<String, String> third_sections = new LinkedHashMap<String, String>();
	for(Map.Entry<String, String> entry : second_sections.entrySet()){
	    String head = entry.getKey();
	    String cont = entry.getValue();
	    Map<String, String> tmp = getBlocksFromContent(cont, regex_third, ABSTRACT, "#");
	    for(Map.Entry<String, String> entries : tmp.entrySet()){
		if (!entries.getKey().equals(ABSTRACT))
		    third_sections.put(entries.getKey(), entries.getValue());
		else
		    third_sections.put(head, entries.getValue());
	    }
	}

	return third_sections;
    }

    /**
     * Splits a piece of content in multiple sub-sections based on the regex in input.
     * The regex capture sub-sections from paragraphs such as h1, h2, h3,.. .
     * 
     * @param content
     * @param regex
     * @param nameFirst
     * @param separator
     * @return
     */
    private Map<String, String> getBlocksFromContent(String content, String regex, String nameFirst, String separator){
	Map<String,String> subsections = new LinkedHashMap<String, String>();

	Pattern SECTIONS = Pattern.compile(regex);
	Matcher m_sec = SECTIONS.matcher(content);

	// for sure there is at least one subsection
	String[] list_subsection = content.split(regex);

	// insert the abstract
	int subsections_count = 0;
	subsections.put(nameFirst, list_subsection[subsections_count]);

	// if there is at least one (sub)section in the article...
	while(m_sec.find()){
	    subsections_count+=1;

	    // get the title of the section (removing possible wikilinks from the titles) and normalize it (adapting for the urls)
	    String title_block = m_sec.group(0).replaceAll("=", "").trim().replaceAll(" ", "_");
	    String header = separator + title_block;

	    // insert the other sections
	    if (list_subsection.length > subsections_count)
		subsections.put(header, list_subsection[subsections_count]);

	}
	return subsections;
    }


    /**
     * Remove undesired blocks (described in the lang file!) from the blocks.
     * 
     * @param blocks
     * @param onlyAbstract
     * @return
     */
    public Map<String, String> removeUndesiredBlocks(Map<String, String> blocks, boolean onlyAbstract) {

	if(onlyAbstract){
	    Set<String> desiredSectionNames = new HashSet<String>();
	    desiredSectionNames.add("#Abstract");
	    blocks.keySet().retainAll(desiredSectionNames);
	}else{
	    /* get the titles of all undesired sections ... */
	    Set<String> undesiredSectionNames = new HashSet<String>();
	    undesiredSectionNames.addAll(lang.getFooterIdentifiers());	

	    /* get the titles of all empty sections and add to the undesired */
	    for(Map.Entry<String, String> entry : blocks.entrySet()){
		if(!isInterestingBlock(entry.getValue())){
		    undesiredSectionNames.add(entry.getKey());
		}
	    }
	  
	    /* and eliminated them! */
	    blocks.keySet().removeAll(undesiredSectionNames);
	}

	return blocks;
    }
    
    /**
     * 
     * @param block
     * @return
     */
    private boolean isInterestingBlock(String block){
	// optimistic start :)
	boolean isInteresting = true;
	block = block.trim();
	if(block.isEmpty() || block.matches(Pattern.quote("")))
	    isInteresting = false;
	return isInteresting;
    }


    /**
     * 
     * @param fragmentedArticle
     * @return
     */
    public Map<String, List<String>> extractTables(Map<String, String> fragmentedArticle){
	return cleaner.retrieveTables(fragmentedArticle);
    }

    /**
     * 
     * @param fragmentedArticle
     * @return
     */
    public Map<String, List<String>> extractLists(Map<String, String> fragmentedArticle){
	return cleaner.retrieveLists(fragmentedArticle);
    }


}
