package it.uniroma3.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
/**
 * 
 * @author matteo
 *
 */
public class TextParser {

    private Cleaner cleaner;

    /**
     * 
     * @param lang
     */
    public TextParser(WikiLanguage lang){
	this.cleaner = new Cleaner(lang);
    }

    /**
     * 
     * @param text
     * @param lang
     * @return
     */
    public String hugeCleaningText(String titleBlock, String text, WikiArticle article, WikiLanguage lang){
	String cleanText = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(text));
	cleanText = fixUnitConversion(cleanText);	// this is an attempt to save some templates.
	cleanText = fixLangTemplate(cleanText);	// this is an attempt to save some templates.
	cleanText = removeNoToc(cleanText);
	cleanText = removeInterWikiLinks(cleanText);
	cleanText = removeRefs(cleanText);
	cleanText = removeCategoryLinks(cleanText, lang);
	cleanText = removeHtmlComments(cleanText);
	cleanText = removeHtmlTags(cleanText); 		// it is important to run it after "removeRefs"
	cleanText = removeIndentation(cleanText);

	/*
	 * remove noisy wikilinks and template
	 * this is done running specific methods of the cleaner that remove 
	 * balanced content in between two specific span of text, "{{" and "}}" 
	 * for example would remove templates.
	 * We also remove wikilinks that are not named entities.
	 */
	cleanText = cleaner.cleanBlockOfContent(cleanText, "{{", "}}");				// removes templates
	cleanText = cleaner.cleanBlockOfContentFromSpecific(cleanText, "[[", "image", "]]"); 	// removes media
	cleanText = MarkupParser.cleanEmptyTemplateWikilinks(cleanText);
	cleanText = MarkupParser.removeCommonSenseWikilinks(cleanText);
	
	/* 
	 * wikilinks - alternative one
	 * Here we extract all the wikilinks present in the WHOLE page that have been not removed above.
	 * We harvest wikilinks from the text, from the tables, reference block, etc.
	 */
	if(!Configuration.getOnlyTextWikilinks()){
	    cleanText = MarkupParser.harvestAllWikilinks(cleanText, article);
	}

	/*
	 * after extract all the wikilinks in the page (that are not category or media, remove above)
	 * we can extract whole span of text that are relative to structured contentes such as tables,
	 * infoboxes, lists, etc. using the same stack-based method in the cleaner.
	 */
	cleanText = cleaner.cleanBlockOfContent(cleanText, "{|", "|}"); 			// removes tables
	cleanText = cleaner.removeLists(cleanText);						// remove lists

	if(Configuration.getEDTestingMode()){
	    String blockNoWikilink = MarkupParser.cleanAllWikilinks(cleanText, article);
	    article.getCleanBlocks().put(titleBlock, blockNoWikilink);
	}
	
	/* 
	 * wikilinks - alternative two
	 * Here we extract all the wikilinks present only in the TEXT of the page, that have been not removed above. 
	 */
	if(Configuration.getOnlyTextWikilinks()){
	    cleanText = MarkupParser.harvestAllWikilinks(cleanText, article);
	}

	return cleanText;
    }


    /**
     * Note that WiktionaryLinks have the form [[wikt:anarchism|anarchism]], which is easily confused
     * with inter-wikilinks. The distinguishing characteristic is the lack of pipe (|).
     * 
     * @param s
     * @return
     */
    private String removeInterWikiLinks(String s) {
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
    private String removeRefs(String s) {
	Pattern BR = Pattern.compile("(<|&lt;|&#60;)br */(>|&gt;|&#62;)");
	Pattern REF1 = Pattern.compile("(<|&lt;|&#60;)ref[^/]+/(>|&gt;|&#62;)", Pattern.DOTALL);
	Pattern REF2 = Pattern.compile("(<|&lt;|&#60;)ref.*?(<|&lt;|&#60;)/ref(>|&gt;|&#62;)", Pattern.DOTALL);
	Pattern MATH1 = Pattern.compile("(<|&lt;|&#60;)math[^/]+/(>|&gt;|&#62;)", Pattern.DOTALL);
	Pattern MATH2 = Pattern.compile("(<|&lt;|&#60;)math.*?(<|&lt;|&#60;)/math(>|&gt;|&#62;)", Pattern.DOTALL);
	Pattern GALLERY1 = Pattern.compile("(<|&lt;|&#60;)gallery[^/]+/(>|&gt;|&#62;)", Pattern.DOTALL);
	Pattern GALLERY2 = Pattern.compile("(<|&lt;|&#60;)gallery.*?(<|&lt;|&#60;)/gallery(>|&gt;|&#62;)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	s = BR.matcher(s).replaceAll(""); 
	s = REF1.matcher(s).replaceAll("");
	s = REF2.matcher(s).replaceAll("");
	s = MATH1.matcher(s).replaceAll("");
	s = MATH2.matcher(s).replaceAll("");
	s = GALLERY1.matcher(s).replaceAll("");
	s = GALLERY2.matcher(s).replaceAll("");
	return s;
    }

    /**
     * Remove category links.
     * 
     * @param text
     * @param lang
     * @return
     */
    private String removeCategoryLinks(String text, WikiLanguage lang) {
	List<String> keywordsCategory = lang.getCategoryIdentifiers();
	for (String keyword : keywordsCategory){
	    text = text.replaceAll("\\[\\[" + Pattern.quote(keyword) + ":([^\\]]*)\\]\\]", "");
	}
	return text;
    }

    /**
     * Remove HTML comments.
     * 
     * @param s
     * @return
     */
    private String removeHtmlComments(String s) {
	Pattern HTML_COMMENT = Pattern.compile("(<|&lt;|&#60;)!--.*?--(>|&gt;|&#62;)", Pattern.DOTALL);
	s = HTML_COMMENT.matcher(s).replaceAll("");
	return s;
    }

    /**
     * Fixs unit conversion.
     * 
     * @param s
     * @return
     */
    private String fixUnitConversion(String s) {
	Pattern UNIT_CONVERSION1 = Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\}\\}");
	Pattern UNIT_CONVERSION2 = Pattern.compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\|[^}]+\\}\\}");
	String t = UNIT_CONVERSION1.matcher(s).replaceAll("$1 $2");
	return UNIT_CONVERSION2.matcher(t).replaceAll("$1 $2");
    }
    
    /**
     * Fixs lang template.
     * 
     * https://en.wikipedia.org/wiki/Template:Lang
     * https://regex101.com/r/PbaZjp/1
     * 
     * @param s
     * @return
     */
    private String fixLangTemplate(String s) {
	Pattern LANG1 = Pattern.compile("\\{\\{lang\\|[^\\|]+\\|([^\\|]+)\\}\\}", Pattern.CASE_INSENSITIVE);
	Pattern LANG2 = Pattern.compile("\\{\\{lang-[^\\|]+\\|([^\\|]+)\\}\\}", Pattern.CASE_INSENSITIVE);
	String t = LANG1.matcher(s).replaceAll("$1");
	return LANG2.matcher(t).replaceAll("$1");
    }

    /**
     * Removes HTML tags.
     * 
     * @param s
     * @return
     */
    private String removeHtmlTags(String s) {
	Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
	return HTML_TAGS.matcher(s).replaceAll("");
    }

    /**
     * Removes NOTOC tag.
     * It is used to denote article that do not have table of content.
     * 
     * @param text
     * @return
     */
    private String removeNoToc(String text) {
	text = text.replaceAll("__NOTOC__", "");
	return text;
    }

    /**
     * Removes indentations.
     * This includes a regex to remove sentences that should be included in template but
     * for errors in wikimarkup they are in free text.
     * e.g. look at the initial distinguish in "https://en.wikipedia.org/wiki/Catullus"
     * 
     * @param s
     * @return
     */
    private String removeIndentation(String text) {
	text = text.replaceAll("(?m)^\\;.+\\n", "");
	text = text.replaceAll("(?m)^\\:''[^\\.']+\\.''", "");
	text = text.replaceAll("[\\n\\r]:\\s*", "\n");
	return text;
    }

    /* ***********************************************************************************
     * 				CLEAN CONTENT
     * ***********************************************************************************/

    /**
     * Returns the first sentence of the article, without the wikilinks!
     * 
     * @return
     */
    public String getFirstSentence(String text){
	return splitSentences(text).get(0);
    }

    /**
     * 
     * @param text
     * @return
     */
    public String removeLinks(String text){
	return MarkupParser.removeAllWikilinks(text);
    }

    /**
     * Splits the paragraphs in sentences.
     * 
     * @param paragraphs
     * @return
     */
    public static List<String> splitSentences(String text){
	List<String> sentences = new LinkedList<String>();
	String regex = "((?<=[a-z0-9\\]\\\"]{2}?[.?!])|(?<=[a-z0-9\\]\\\"]{2}?[.?!]\\\"))(\\s+|(\\r)*\\n|(\\s)*\\n)(?=\\\"?(\\[\\[)?[A-Z])";
	for(String sent : text.split(regex)){
	    sentences.add(sent);
	}
	return sentences;
    }

    /**
     * We extract the bold names that are in the first block.
     * We take into account the italics and we distinguish them from the normal keywords.
     * E.g. we distinguish ''Batman'' from Batman.
     * 
     * 
     * @param s
     * @return
     */
    public List<String> getAlias(String block) {
	List<String> aliases = new LinkedList<String>();
	// we add the constraint of "{" and "}" because we remove {{template}} after this step
	Pattern ALIASES = Pattern.compile("'''('')?([^\\{\\}\\(\\)\\+\\*]*?)('')?'''");
	Matcher m = ALIASES.matcher(block);
	while(m.find()){
	    String name = m.group(2).replaceAll("(\\[|\\])*", "").trim();
	    /*
	     * check if it is an italic name
	     */
	    if (m.group(1)!=null && m.group(3)!=null)
		name = "''" + name + "''";
	    if (!name.equals(""))
		aliases.add(name);
	}
	return aliases;
    }

    /**
     * 
     * @param blocks
     * @return
     */
    public Map<String, String> finalCleanText(Map<String, String> blocks){
	for(Map.Entry<String, String> block : blocks.entrySet()){
	    //String cleanContent = removeParenthesis(block.getValue());
	    String cleanContent = removeEmphasis(block.getValue());
	    cleanContent = cleanContent.replaceAll(" {2,}", " ");				// remove double spaces
	    cleanContent = cleanContent.replaceAll("\n{2,}", "\n");				// remove double new lines
	    cleanContent = cleanContent.replaceAll(" , ", ", ").trim();				// remove space before commma
	    blocks.put(block.getKey(), cleanContent);
	}
	return blocks;
    }


    /**
     * Remove words in bold but keeps italic names.
     * 
     * @param block
     * @return
     */
    private String removeEmphasis(String block) {
	Pattern ALIASES = Pattern.compile("'''('')?([^\\{\\}\\(\\)\\+\\*]*?)('')?'''");
	Matcher m = ALIASES.matcher(block);
	while(m.find()){
	    /*
	     * check if it is an italic name
	     */
	    String name = m.group(2);
	    /*
	     * check if it is an italic name
	     */
	    if (m.group(1)!=null && m.group(3)!=null)
		name = "''" + name + "''";
	    block = block.replaceAll(Pattern.quote(m.group(0)), Matcher.quoteReplacement(name));
	}
	return block;

    }

    /**
     * Removes all the parenthesis (maybe surrounded by '''), and the spaces before them.
     * 
     * @param block
     * @return
     */
    private String removeParenthesis(String block){
	Pattern PARENTHESIS = Pattern.compile("(\\s|_)?'*(\\([^\\(]*?\\))'*");	// remove parenthesis and content ( )
	Matcher m = PARENTHESIS.matcher(block);
	while(m.find()){
	    block = m.replaceAll("");
	    m = PARENTHESIS.matcher(block);
	}
	return block.trim();
    }


}
