package it.uniroma3.extractor._articleparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiLanguage;
/**
 * Finite state machine to capture composite structures in Wikipedia Markup Language.
 * Those structures can be: infobox, bio, tables, template, etc.
 * 
 * Everything that is exressed with an opening tag of two characters (e.g. "{|" for tables, 
 * "{{" for template) and closing of two characters (e.g. "|}" for tables, "}}" for template).
 * 
 * Moreover, it is possible to specify a keyword to capture a specific composite structure 
 * (e.g. Bio, Infobox).
 * 
 * @author matteo
 *
 */
public class Cleaner {
    private static final int DEFAULT = 0;
    private static final int CLOSE = 1;
    private static final int OPEN = 2;

    /**
     * Returns a list of all the composite structures in the page. 
     * 
     * page:				WikiMarkup text
     * initialDoubleCharacterLabel:	the pair of characters at the begin, e.g. "{{"
     * finalDoubleCharacterLabel:	the pair of characters at the end, e.g. "}}"
     * 
     * @param page
     * @param initialDoubleCharacterLabel
     * @param specification
     * @param finalDoubleCharacterLabel
     * @return
     */
    public List<String> retrieveAll(String page, String initialDoubleCharacterLabel, String finalDoubleCharacterLabel){

	List<String> structureList = new ArrayList<String>();
	String init = initialDoubleCharacterLabel;
	Pattern search_init = Pattern.compile(Pattern.quote(init));
	Matcher m = search_init.matcher(page);
	int i, cur;

	boolean matched = true;
	if (m.find()){
	    matched = true;
	    while(matched){
		m = search_init.matcher(page);
		if (m.find()){
		    i = m.start();
		    cur = retrieveBalancedSpan(i + init.length(), page, initialDoubleCharacterLabel, finalDoubleCharacterLabel);
		    structureList.add(page.substring(i, cur));
		    page = page.substring(cur);
		    cur = i + 1;
		}else{
		    matched = false;
		}
	    }
	}
	return structureList;
    }

    /**
     * Returns a list of composite structures. If not specified as a parameter, it captures all
     * the span of text between the initialDoubleCharacterLabel and finalDoubleCharacterLabel.
     * 
     * page:				WikiMarkup text
     * initialDoubleCharacterLabel:	the pair of characters at the begin, e.g. "{{"
     * specification:			the keyword to specify a structure (put "" for general case) , e.g. "Bio"
     * finalDoubleCharacterLabel:	the pair of characters at the end, e.g. "}}"
     * 
     * @param page
     * @param initialDoubleCharacterLabel
     * @param specification
     * @param finalDoubleCharacterLabel
     * @return
     */
    public List<String> retrieveAllWithSpecification(String page, String initialDoubleCharacterLabel, 
	    String specification, String finalDoubleCharacterLabel){

	List<String> structureList = new ArrayList<String>();
	List<String> keywords = Lector.getWikiLang().getIdentifiers(specification);
	if(keywords.isEmpty())
	    keywords.add("");

	for(String keyword : keywords){
	    String init = initialDoubleCharacterLabel + keyword;
	    Pattern search_init = Pattern.compile(Pattern.quote(init) + "(?=\\b)");
	    Matcher m = search_init.matcher(page);
	    int i, cur;

	    boolean matched = true;
	    if (m.find()){
		matched = true;
		while(matched){
		    m = search_init.matcher(page);
		    if (m.find()){
			i = m.start();
			cur = retrieveBalancedSpan(i + init.length(), page, initialDoubleCharacterLabel, finalDoubleCharacterLabel);
			structureList.add(page.substring(i, cur));
			page = page.substring(cur);
			cur = i + 1;
		    }else{
			matched = false;
		    }
		}
	    }
	}

	if(structureList.isEmpty())
	    structureList.add("-");
	return structureList;
    }

    /**
     * Clean the page from all the specified composite structures. E.g. "Infobox", "Bio", etc.
     * 
     * blockOfContent:			WikiMarkup text
     * initialDoubleCharacterLabel:	the pair of characters at the begin, e.g. "{{"
     * specification:			the keyword to specify a structure (put "" for general case) , e.g. "Bio"
     * finalDoubleCharacterLabel:	the pair of characters at the end, e.g. "}}"
     * 
     * @param blockOfContent
     * @param initialDoubleCharacterLabel
     * @param specification
     * @param finalDoubleCharacterLabel
     * @return
     */
    public String cleanBlockOfContentFromSpecific(String blockOfContent, String initialDoubleCharacterLabel, 
	    String specification, String finalDoubleCharacterLabel){

	List<String> keywords = Lector.getWikiLang().getIdentifiers(specification);
	if(keywords.isEmpty())
	    keywords.add("");

	for(String keyword : keywords){
	    String init = initialDoubleCharacterLabel + keyword;
	    Pattern search_init = Pattern.compile(Pattern.quote(init) + "(?=\\b)");
	    Matcher m = search_init.matcher(blockOfContent);
	    boolean matched = true;
	    int i, cur;
	    if (m.find()){
		matched = true;
		while(matched){
		    m = search_init.matcher(blockOfContent);
		    if (m.find()){
			i = m.start();
			cur = retrieveBalancedSpan(i + init.length(), blockOfContent, initialDoubleCharacterLabel, finalDoubleCharacterLabel);
			blockOfContent = blockOfContent.substring(0, i).trim() + blockOfContent.substring(cur);
			cur = i + 1;
		    }else{
			matched = false;
		    }
		}
	    }
	}
	return blockOfContent;
    }

    /**
     * Clean the block of content from all the composite structures. It removes all
     * the spans of text between the initialDoubleCharacterLabel and finalDoubleCharacterLabel.
     * 
     * blockOfContent:			WikiMarkup text
     * initialDoubleCharacterLabel:	the pair of characters at the begin, e.g. "{{"
     * finalDoubleCharacterLabel:	the pair of characters at the end, e.g. "}}"
     * 
     * @param blockOfContent
     * @param initialDoubleCharacterLabel
     * @param specification
     * @param finalDoubleCharacterLabel
     * @return
     */
    public String cleanBlockOfContent(String blockOfContent, String initialDoubleCharacterLabel, String finalDoubleCharacterLabel){
	String init = initialDoubleCharacterLabel;
	Pattern search_init = Pattern.compile(Pattern.quote(init));
	Matcher m = search_init.matcher(blockOfContent);
	boolean matched = true;
	int i, cur;
	if (m.find()){
	    matched = true;
	    while(matched){
		m = search_init.matcher(blockOfContent);
		if (m.find()){
		    i = m.start();
		    cur = retrieveBalancedSpan(i + init.length(), blockOfContent, initialDoubleCharacterLabel, finalDoubleCharacterLabel);
		    blockOfContent = blockOfContent.substring(0, i).trim() + blockOfContent.substring(cur);
		    cur = i + 1;
		}else{
		    matched = false;
		}
	    }
	}
	return blockOfContent;
    }

    /**
     * Detects the next structure in the given page, starting from the current character ("cur").
     * 
     * 
     * @param cur
     * @param page
     * @param initialDoubleCharacterLabel
     * @param finalDoubleCharacterLabel
     * @return
     */
    private int retrieveBalancedSpan(int cur, String page, 
	    String initialDoubleCharacterLabel, String finalDoubleCharacterLabel){

	int state = DEFAULT;
	int nestingLevel = 1;
	// firstInitialDoubleCharacterLabel
	final char FI = initialDoubleCharacterLabel.charAt(0); 
	// secondInitialDoubleCharacterLabel
	final char SI = initialDoubleCharacterLabel.charAt(1);
	// firstFinalDoubleCharacterLabel
	final char FF = finalDoubleCharacterLabel.charAt(0);
	// secondFinalDoubleCharacterLabel
	final char SF = finalDoubleCharacterLabel.charAt(1);

	while (cur < page.length()) {
	    if (state == OPEN && page.charAt(cur) == SI) {
		nestingLevel++;
		state = DEFAULT;
	    }
	    // If there's only one close, move back to default state.
	    if (state == OPEN) {
		state = DEFAULT;
	    }
	    if (page.charAt(cur) == FI) {
		state = OPEN;
	    }

	    if (state == CLOSE && page.charAt(cur) == SF) {
		nestingLevel--;
		if (nestingLevel == 0) {
		    cur++;
		    break;
		}
		state = DEFAULT;
	    } else {
		// If there's only one close, move back to default state.
		if (state == CLOSE) {
		    state = DEFAULT;
		}
		if (page.charAt(cur) == FF) {
		    state = CLOSE;
		}
	    }
	    cur++;
	}
	return cur;
    }


    /* ***********************************************************************************
     * 				REMOVE - RETRIEVE LIST/TABLES
     * ***********************************************************************************/
    
    /**
     * 
     * @param blocks
     * @return
     */
    public Map<String, List<String>> retrieveTables(Map<String, String> blocks){
	Map<String, List<String>> tablesContent = new LinkedHashMap<String, List<String>>();
	for(Map.Entry<String, String> block : blocks.entrySet()){
	    List<String> tables = retrieveAll(block.getValue(), "{|", "|}");	// retrieve tables	    
	    // add only if there is some textual content
	    if (!tables.isEmpty())
		tablesContent.put(block.getKey(), tables);
	}

	return tablesContent;
    }

    /**
     * This method needs improvements. For now, we are limiting to retrieve all the entry of the lists
     * in each section and put them together.
     * 
     * https://regex101.com/r/JhwkQh/2
     * 
     * 
     * @param blocks
     * @return
     */
    public Map<String, List<String>> retrieveLists(Map<String, String> blocks){
	Map<String, List<String>> listContent = new LinkedHashMap<String, List<String>>();
	Pattern LISTS = Pattern.compile("(?m)^(;|#|\\*|:)(?:(?!\\n).)++");
	for(Map.Entry<String, String> block : blocks.entrySet()){
	    Matcher m = LISTS.matcher(block.getValue());
	    while (m.find()){
		if (!listContent.containsKey(block.getKey()))
		    listContent.put(block.getKey(), new ArrayList<String>());
		listContent.get(block.getKey()).add(m.group(0));
	    }
	}   
	return listContent;
    }
    
    /**
     * Normalize lists.
     * 
     * https://regex101.com/r/JhwkQh/2
     * 
     * @param blocks
     * @return
     */
    public String replaceListsWithNormalSentences(String block){
	Pattern LIST_CONTENT = Pattern.compile("(?m)^(;|#|\\*|:)+((?!\\n).+)++");
	Matcher m = LIST_CONTENT.matcher(block);
	StringBuffer sb = new StringBuffer();
	while(m.find()){
	    m.appendReplacement(sb, Matcher.quoteReplacement(m.group(2).trim() + ".\n"));
	}
	m.appendTail(sb);
	return sb.toString();
    }
    
    /**
     * Remove lists at all!
     * The method is used to remove remnants of infoboxes, tables, taxonomies as well.
     * 
     * https://regex101.com/r/JhwkQh/2
     * 
     * @param blocks
     * @return
     */
    public String replaceListsWithEmptyContent(String block){
	//Pattern LIST_CONTENT = Pattern.compile("(?m)^\\s*(;|#|\\*|:|\\||\\│)+((?!\\n).+)++");
	Pattern LIST_CONTENT = Pattern.compile("(?m)^\\s*(;|#|\\*|:)+((?!\\n).+)++");
	Matcher m = LIST_CONTENT.matcher(block);
	while(m.find()){
	    block = m.replaceAll("");
	}
	return block;
    }

}

