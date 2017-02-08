package it.uniroma3.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.lectorplus.WikiLanguage;

public class MarkupParser {

    /**
     * Fragment the article in multiple (sub)sections.
     * 
     * @param content
     * @return
     * @throws Exception 
     */
    public static Map<String, String> fragmentArticle(String content){
	String ABSTRACT = "#Abstract";
	String regex_first = "(?m)^==\\s?([^=]+)\\s?==$";	// h1
	String regex_second = "(?m)^===\\s?([^=]+)\\s?===$";	// h2
	String regex_third = "(?m)^====\\s?([^=]+)\\s?====$";	// h3

	// content --> first sections
	Map<String, String> first_sections = getBlocksFromContent(content, regex_first, ABSTRACT, "#");

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
    private static Map<String, String> getBlocksFromContent(String content, String regex, String nameFirst, String separator){
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

	    // get the title of the section (removing possible wikilinks) and normalize it (adapting for the urls)
	    String title_block = removeWikilinks(m_sec.group(0).replaceAll("=", "").trim().replaceAll(" ", "_"));
	    String header = separator + title_block;

	    // insert the other sections
	    if (list_subsection.length > subsections_count)
		subsections.put(header, list_subsection[subsections_count]);

	}
	return subsections;
    }

    /**
     * General method to remove wiki-links from text.
     * (used to remove wiki-links from sections title)
     * 
     * @param s
     * @return
     */
    private static String removeWikilinks(String s) {
	/*
	 * detect entities with pipes, and transform them in normal entities 
	 * e.g. [[multinational corporation|multinational]] -> [[multinational]]
	 */
	Pattern LINKS1 = Pattern.compile("\\[[^\\]]+\\|([^\\]]+)\\]\\]");
	s = LINKS1.matcher(s).replaceAll("$1");

	/*
	 * detect normal named entities and remove brackets, e.g. [[multinational]] -> multinational
	 */
	Pattern LINKS3 = Pattern.compile("\\[\\[([^\\]\\|]+)\\]\\]");
	s = LINKS3.matcher(s).replaceAll("$1");

	return s;
    }

    /**
     * Note that WiktionaryLinks have the form [[wikt:anarchism|anarchism]], which is easily confused
     * with inter-wikilinks. The distinguishing characteristic is the lack of pipe (|).
     * 
     * @param s
     * @return
     */
    public static String removeInterWikiLinks(String s) {
	Pattern INTER_WIKI_LINKS = Pattern.compile("\\[\\[[a-z\\-]+:[^|\\]]+\\]\\]");
	Pattern EXTERNAL_LINKS = Pattern.compile("\\[http[^\\s]+\\]");
	Pattern EXTERNAL_LINKS_WITH_TEXT = Pattern.compile("\\[http[^\\s]+((\\s)[^\\]]+)?\\]");

	s = INTER_WIKI_LINKS.matcher(s).replaceAll(" ");
	s = EXTERNAL_LINKS.matcher(s).replaceAll("");
	s = EXTERNAL_LINKS_WITH_TEXT.matcher(s).replaceAll("$1");

	return s;
    }

    /**
     * Remove refs blocks.
     * 
     * E.g. <ref> content <\ref>
     * 
     * 
     * @param s
     * @return
     */ 
    public static String removeRefs(String s) {
	Pattern BR = Pattern.compile("&lt;br */&gt;");
	Pattern REF1 = Pattern.compile("&lt;ref[^/]+/&gt;", Pattern.DOTALL);
	Pattern REF2 = Pattern.compile("&lt;ref.*?&lt;/ref&gt;", Pattern.DOTALL);
	s = BR.matcher(s).replaceAll(""); // See test case for why we do this.
	s = REF1.matcher(s).replaceAll("");
	s = REF2.matcher(s).replaceAll("");
	return s;
    }

    /**
     * Remove category links.
     * 
     * @param s
     * @param lang
     * @return
     */
    public static String removeCategoryLinks(String s, WikiLanguage lang) {
	List<String> keywordsCategory = lang.getCategoryIdentifiers();
	for (String keyword : keywordsCategory){
	    Pattern CATEGORY_LINK = Pattern.compile("\\[\\["+keyword+":([^\\]]+)\\]\\]");
	    s = CATEGORY_LINK.matcher(s).replaceAll("");
	}
	return s;
    }

    /**
     * Remove HTML comments.
     * 
     * @param s
     * @return
     */
    public static String removeHtmlComments(String s) {
	Pattern HTML_COMMENT = Pattern.compile("(<|&lt;|&#60;)!--.*?--(>|&gt;|&#62;)", Pattern.DOTALL);
	s = HTML_COMMENT.matcher(s).replaceAll("");
	return s;
    }

    /**
     * Compress multiple lines.
     * 
     * @param s
     * @return
     */
    public static String compressMultipleNewlines(String s) {
	Pattern START_NEWLINES = Pattern.compile("^[\\n\\r]+");
	Pattern MULTIPLE_NEWLINES = Pattern.compile("[\\n\\r][\\n\\r]+");
	s = MULTIPLE_NEWLINES.matcher(s).replaceAll("\n");
	s = START_NEWLINES.matcher(s).replaceAll("");
	return s;
    }

    /**
     * Fixs unit conversion.
     * 
     * @param s
     * @return
     */
    public static String fixUnitConversion(String s) {
	Pattern UNIT_CONVERSION1 = Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\}\\}");
	Pattern UNIT_CONVERSION2 = Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\|[^}]+\\}\\}");
	String t = UNIT_CONVERSION1.matcher(s).replaceAll("$1 $2");
	return UNIT_CONVERSION2.matcher(t).replaceAll("$1 $2");
    }

    /**
     * Removes HTML tags.
     * 
     * @param s
     * @return
     */
    public static String removeHtmlTags(String s) {
	Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
	return HTML_TAGS.matcher(s).replaceAll("");
    }

    /**
     * Removes gallery tags and content.
     * 
     * E.g. <gallery> content </gallery>
     * 
     * (modified removing &gt at the end of the first line)
     * 
     * @param s
     * @return
     */
    public static String removeGallery(String s) {
	Pattern GALLERY = Pattern.compile("&lt;gallery.*?&lt;/gallery&gt;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	return GALLERY.matcher(s).replaceAll("");
    }

    /**
     * Removes NOTOC.
     * 
     * @param s
     * @return
     */
    public static String removeNoToc(String s) {
	Pattern NO_TOC = Pattern.compile("__NOTOC__");
	return NO_TOC.matcher(s).replaceAll("");
    }

    /**
     * Removes indentations.
     * 
     * @param s
     * @return
     */
    public static String removeIndentation(String s) {
	Pattern INDENTATION = Pattern.compile("[\\n\\r]:\\s*");
	return INDENTATION.matcher(s).replaceAll("\n");
    }

    /**
     * Remove math tags.
     * 
     * E.g. <math> content </math>
     * 
     * @param s
     * @return
     */
    public static String removeMath(String s) {
	Pattern MATH = Pattern.compile("&lt;math&gt;.*?&lt;/math&gt;", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	return MATH.matcher(s).replaceAll("");
    }

    /**
     * Check each block of the article and remove composite structures such as template, tables, etc.
     * Then, remove the common-sense wiki-links (we are not interested in them) and normalize them.
     * Then, split the content in sentences and return the final content.
     * 
     * @param blocks
     * @return
     */
    public static Map<String, List<String>> fragmentParagraph(Map<String, String> blocks, Cleaner cleaner, WikiLanguage lang){
	Map<String, List<String>> textContent = new LinkedHashMap<String, List<String>>();

	for(Map.Entry<String, String> block : blocks.entrySet()){
	    String cleanContent = cleaner.cleanBlockOfContent(block.getValue(), "{{", "}}");			// removes templates
	    cleanContent = cleaner.cleanBlockOfContentFromSpecific(cleanContent, "[[", "image", "]]"); 		// removes media
	    cleanContent = cleaner.cleanBlockOfContent(cleanContent, "{|", "|}"); 				// removes tables

	    // before to split in sentences, clean and normalize important wiki-links!
	    cleanContent = removeCommonsenseWikilinks(cleanContent, lang);
	    cleanContent = normalizeWikilinks(cleanContent);

	    /* REMOVE LISTS!! */
	    cleanContent = removeLists(cleanContent);

	    List<String> sentences = splitSentences(cleanContent);

	    // add only if there is some textual content
	    if (!sentences.isEmpty() && !sentences.get(0).isEmpty())
		textContent.put(block.getKey(), sentences);
	}

	return textContent;
    }
    
    /**
     * Splits the paragraphs in sentences.
     * 
     * @param paragraphs
     * @return
     */
    public static List<String> splitSentences(String text){
	List<String> sentences = new LinkedList<String>();
	String regex = "((?<=[a-z0-9\\]\\\"]{2}?[.?!])|(?<=[a-z0-9\\]\\\"]{2}?[.?!]\\\"))(\\s+|\\r\\n)(?=\\\"?(\\[\\[)?[A-Z])";
	for(String sent : text.split(regex)){
	    sentences.add(cleanAndNormalizeContent(sent));
	}
	return sentences;
    }

    /**
     * Remove common sense wiki-links.
     * (used to remove wiki-links in the text)
     * 
     * @param s
     * @return
     */
    public static String removeCommonsenseWikilinks(String s, WikiLanguage lang) {
	/*
	 * detect commonsense entities with pipes, and transform them
	 * in normal entities e.g. [[multinational corporation|multinational]] -> [[multinational]]
	 */
	Pattern LINKS1 = Pattern.compile("\\[[^\\]]+\\|([^A-Z][^\\]]+)\\]\\]");
	s = LINKS1.matcher(s).replaceAll("$1");

	/*
	 * detect commonsense entities using the presence of some keywords (i.e. List) in the target wikid.
	 * e.g. [[List of canadian territories|terriories in Alberta]]
	 */
	for (String listHook : lang.getListIdentifiers()){
	    listHook = listHook.replaceAll("_", " ");
	    Pattern LINKS2 = Pattern.compile("\\[\\[(?>"+listHook+")[^\\]]+\\|([^\\]]+)\\]\\]");
	    s = LINKS2.matcher(s).replaceAll("$1");
	}

	/*
	 * detect normal named entities and remove brackets, e.g. [[multinational]] -> multinational
	 */
	Pattern LINKS3 = Pattern.compile("\\[\\[([^A-Z][^\\]\\|]+)\\]\\]");
	s = LINKS3.matcher(s).replaceAll("$1");

	return s;
    }
    
    /**
     * 
     * @param content
     * @return
     */
    public static String cleanAndNormalizeContent(String content){
	content = removeParenthesis(content);
	return content.replaceAll("\\s{2,}", " ").trim();
    }


    /**
     * Normalized wiki-links to appear with underscores instead of their keyword names.
     * 
     * @param s
     * @return
     */
    public static String normalizeWikilinks(String s) {
	/*
	 * transform entities in wikids
	 * e.g. [[multinational corporation|multinational]] -> [[multinational_corporation|multinational]]
	 */
	StringBuffer newContent = new StringBuffer();
	Pattern LINKS1 = Pattern.compile("(?<=\\[\\[)([^\\]]+)\\|([^\\]]+)(?=\\]\\])");
	Matcher m1 = LINKS1.matcher(s);
	int begin = 0;
	while(m1.find()){
	    int start = m1.start(0);
	    int end = m1.end(0);
	    newContent.append(s.substring(begin, start));
	    newContent.append(m1.group(1).replaceAll(" ", "_") + "|" + m1.group(2));
	    begin = end;
	}
	newContent.append(s.substring(begin));

	String newContentTmp = newContent.toString();
	StringBuffer newContent2 = new StringBuffer();

	/*
	 * transform entities in wikids, 
	 * e.g. [[multinational corporation]] -> [[multinational_corporation]]
	 */
	Pattern LINKS2 = Pattern.compile("(?<=\\[\\[)([^\\]]+)(?=\\]\\])");
	Matcher m2 = LINKS2.matcher(newContentTmp);
	begin = 0;
	while(m2.find()){
	    int start = m2.start(0);
	    int end = m2.end(0);
	    newContent2.append(newContentTmp.substring(begin, start));
	    newContent2.append(m2.group(1).replaceAll(" ", "_"));
	    begin = end;
	}
	newContent2.append(newContentTmp.substring(begin));

	return newContent2.toString();
    }


    /**
     * 
     * @param page
     * @return
     * @throws Exception 
     */
    public static Map<String, List<String>> getTablesFromXml(String page, WikiLanguage lang, Cleaner cleaner){
	String content = XMLParser.getWikiMarkup(page);
	Map<String, String> blocks = fragmentArticle(content);
	return removeUndesiredBlocks(retrieveTables(blocks, cleaner), lang);
    }
    

    /**
     * Remove undesired blocks (from the lang file!).
     *  
     * @param sentences
     * @return
     */
    public static Map<String, List<String>> removeUndesiredBlocks(Map<String, List<String>> sentences, WikiLanguage lang) {
	List<String> sectionToDelete = lang.getFooterIdentifiers();
	Map<String, List<String>> filteredSections = sentences;
	
	// remove undesired sections (notes, references, etc.)
	for(String undesiredSection : sectionToDelete){
	    String normUndesiredSection = "#" + undesiredSection.replace(" ", "_");
	    filteredSections.remove(normUndesiredSection); // we need to change it!
	}
	
	// remove empty contents
	for(Map.Entry<String, List<String>> entry : filteredSections.entrySet()){
	    if(entry.getValue().isEmpty())
		filteredSections.remove(entry.getKey());
	}
	return filteredSections;
    }


    /**
     * 
     * @param page
     * @return
     * @throws Exception 
     */
    public static Map<String, List<String>> getListsFromXml(String page, WikiLanguage lang){
	String content = XMLParser.getWikiMarkup(page);
	Map<String, String> blocks = fragmentArticle(content);
	return removeUndesiredBlocks(retrieveLists(blocks), lang);
    }

    /**
     * 
     * @param blocks
     * @return
     */
    public static Map<String, List<String>> retrieveTables(Map<String, String> blocks, Cleaner cleaner){
	Map<String, List<String>> tablesContent = new LinkedHashMap<String, List<String>>();

	for(Map.Entry<String, String> block : blocks.entrySet()){
	    List<String> tables = cleaner.retrieveAll(block.getValue(), "{|", "|}");	// retrieve tables	    
	    // add only if there is some textual content
	    if (!tables.isEmpty())
		tablesContent.put(block.getKey(), tables);
	}

	return tablesContent;
    }
    
    /**
     * 
     * @param blocks
     * @return
     */
    public static String removeParenthesis(String block){
	return block.replaceAll("\\(.*\\)", "");
    }


    /**
     * 
     * @param blocks
     * @return
     */
    public static String removeLists(String block){
	return block.replaceAll("(?m)^\\*.*", "");
    }

    /**
     * 
     * @param blocks
     * @return
     */
    public static Map<String, List<String>> retrieveLists(Map<String, String> blocks){
	Map<String, List<String>> listContent = new LinkedHashMap<String, List<String>>();
	Pattern LISTS = Pattern.compile("(\\*[^\n]+(\n|\r|$)){2,}+");

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

}
