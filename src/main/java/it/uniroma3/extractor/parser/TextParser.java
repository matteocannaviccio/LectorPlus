package it.uniroma3.extractor.parser;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.util.nlp.StupidNLP;
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
    public TextParser(){
	this.cleaner = new Cleaner();
    }

    /**
     * It clean the wikid obtaining the title,
     * without underscores or disambiguations.
     * 
     * @param wikid
     * @return
     */
    public String getTitle(String wikid){
	wikid = wikid.replaceAll("_", " ");
	wikid = wikid.replaceAll(" \\(\\w+\\)$", "");
	return wikid;
    }

    /**
     * In this method we put some manually-done templates resolver.
     * 
     * @param block
     * @return
     */
    public String fixSomeTemplates(String block){
	String cleanBlock = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(block));
	cleanBlock = fixUnitConversion(cleanBlock);
	cleanBlock = fixLangTemplate(cleanBlock);
	return cleanBlock;
    }

    /**
     * In this method we put some noise removal operations on the text.
     * Finally we remove block of content that are contained between 
     * curly brackets or square brackets (templates, for example).
     * 
     * @param block
     * @param lang
     * @return
     */
    public String removeNoise(String block){
	String cleanBlock = removeNoToc(block);
	cleanBlock = removeInterWikiLinks(cleanBlock);
	cleanBlock = removeRefs(cleanBlock);
	cleanBlock = removeCategoryLinks(cleanBlock);
	cleanBlock = removeHtmlComments(cleanBlock);
	cleanBlock = removeHtmlTags(cleanBlock);
	cleanBlock = removeIndentation(cleanBlock);
	cleanBlock = cleaner.cleanBlockOfContentFromSpecific(cleanBlock, "[[", "image", "]]"); 	// removes media
	return cleanBlock;
    }

    /**
     * This methods removes wikilinks that are not named entities, 
     * or that are empty after the removing of wikilinks with templates.
     * 
     * @param block
     * @return
     */
    public String removeUselessWikilinks(String block){
	String cleanBlock = Lector.getMarkupParser().cleanEmptyTemplateWikilinks(block);
	cleanBlock = Lector.getMarkupParser().removeCommonSenseWikilinks(cleanBlock);
	return cleanBlock;
    }

    /**
     * This method extracts structured contents such as tables, infobox,
     * using the stack-based method of the cleaner.
     * 
     * @param block
     * @return
     */
    public String removeStructuredContents(String block){
	String cleanBlock = cleaner.cleanBlockOfContent(block, "{{", "}}");			// removes templates
	cleanBlock = cleaner.cleanBlockOfContent(cleanBlock, "{|", "|}"); 			// removes tables
	//cleanBlock = cleaner.replaceListsWithNormalSentences(cleanBlock);			// replace lists
	cleanBlock = cleaner.replaceListsWithEmptyContent(cleanBlock);
	return cleanBlock;
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
    private String removeCategoryLinks(String text) {
	List<String> keywordsCategory = Lector.getWikiLang().getCategoryIdentifiers();
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
	Pattern LANG1 = Pattern.compile("\\{\\{lang\\|[^\\|]+\\|([^\\{\\|]+)\\}\\}", Pattern.CASE_INSENSITIVE);
	Pattern LANG2 = Pattern.compile("\\{\\{lang-[^\\|]+\\|([^\\{\\|]+)\\}\\}", Pattern.CASE_INSENSITIVE);
	Pattern LANG3_JAP = Pattern.compile("\\{\\{Nihongo\\|([^\\|]+)\\|[^\\{\\}]+?\\}\\}", Pattern.CASE_INSENSITIVE);
	String t = LANG1.matcher(s).replaceAll("$1");
	t = LANG2.matcher(t).replaceAll("$1");
	t = LANG3_JAP.matcher(t).replaceAll("$1");
	return t;
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
	    String cleanContent = block.getValue().replaceAll(" {2,}", " ");	// remove double spaces
	    cleanContent = removeEmphasis(cleanContent, true);
	    cleanContent = cleanContent.replaceAll("\n{2,}", "\n");		// remove double new lines
	    cleanContent = cleanContent.replaceAll(" , ", ", ").trim();		// remove space before commma
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
    private String removeEmphasis(String block, boolean keepItalics) {
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
	    if(keepItalics){
		if (m.group(1)!=null && m.group(3)!=null)
		    name = "''" + name + "''";
	    }
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
    public String removeParenthesis(String block){
	Pattern PARENTHESIS = Pattern.compile("(\\s|_)?'*(\\([^\\(]*?\\))'*(?!>)");	// detect parenthesis and content
	Matcher m = PARENTHESIS.matcher(block);
	while(m.find()){
	    block = m.replaceAll("");
	    m = PARENTHESIS.matcher(block);
	}
	return block.trim();
    }

    /**
     * It return the first sentence of the article, after
     * cleaning it in order to use with the seed FSM in the next step.
     * 
     * 
     * @param abstractSection
     * @return
     */
    public String obtainCleanFirstSentence(String abstractSection) {
	String firstSentence = null;
	try{
	    firstSentence = removeParenthesis(abstractSection);
	    firstSentence = removeEmphasis(firstSentence, false);
	    firstSentence = firstSentence.replaceAll("\"", "");				// remove ""
	    firstSentence = firstSentence.replaceAll(" {2,}", " ");			// remove double spaces
	    firstSentence = firstSentence.replaceAll("\n{2,}", "\n");			// remove double new lines
	    firstSentence = firstSentence.replaceAll(" , ", ", ").trim();		// remove space before commma
	    firstSentence = Lector.getMarkupParser().removeAllWikilinks(firstSentence);	// remove wikilinks: <SE-ORG<...>> become ...
	    firstSentence = StupidNLP.splitSentence(firstSentence).get(0);	   	// here we use a "light" sentence splitter
	}catch(Exception e){}
	return firstSentence;
    }


}
