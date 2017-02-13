package it.uniroma3.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
/**
 * 
 * 
 * 
 * @author matteo
 *
 */
public class WikiParser {
    //private static final Logger logger = LoggerFactory.getLogger(WikiLanguage.class);
    protected WikiLanguage lang;
    protected Cleaner cleaner;

    /**
     * 
     * @param lang
     */
    public WikiParser(WikiLanguage lang){
	this.lang = lang;
	this.cleaner = new Cleaner(lang);
    }

    /**
     * 
     * @param page
     * @return
     */
    public WikiArticle createArticleFromXml(String page){

	/*
	 * Obtain the title of the article and create a WikiArticle.
	 */
	String wikid = XMLParser.getFieldFromXmlPage(page, "title").replaceAll(" ", "_");
	String title = clean(wikid);
	String id = XMLParser.getFieldFromXmlPage(page, "id");
	String namespace = XMLParser.getFieldFromXmlPage(page, "ns");
	WikiArticle article = new WikiArticle(wikid, id, title, namespace, lang);

	/* ************************************************************************ */

	/*
	 * Check if it is an article that we can skip. 
	 */

	/* Filter to capture WIKIPEDIA portal articles */
	for (String portalHook : lang.getPortalIdentifiers()){
	    if (clean(wikid).startsWith(portalHook + ":")){
		article.setType(ArticleType.PORTAL);
		return article;
	    }
	}

	/* Filter to capture FILE articles */
	for (String fileHook : lang.getFileIdentifiers()){
	    if (clean(wikid).startsWith(fileHook + ":")){
		article.setType(ArticleType.FILE);
		return article;
	    }
	}

	/* Filter to capture HELP articles */
	for (String helpHook : lang.getHelpIdentifiers()){
	    if (clean(wikid).startsWith(helpHook + ":")){
		article.setType(ArticleType.HELP);
		return article;
	    }
	}

	/* Filter to capture CATEGORY articles */
	for (String categoryHook : lang.getCategoryIdentifiers()){
	    if (clean(wikid).startsWith(categoryHook + ":")){
		article.setType(ArticleType.CATEGORY);
		return article;
	    }
	}

	/* Filter to capture TEMPLATE articles */
	for (String templateHook : lang.getTemplateIdentifiers()){
	    if (clean(wikid).startsWith(templateHook + ":")){
		article.setType(ArticleType.TEMPLATE);
		return article;
	    }
	}

	/* Filter to capture DISCUSSION articles */
	for (String discussionHook : lang.getDiscussionIdentifiers()){
	    if (clean(wikid).startsWith(discussionHook + ":")){
		article.setType(ArticleType.DISCUSSION);
		return article;
	    }
	}

	/* Filter to capture LIST articles */
	for (String listHook : lang.getListIdentifiers()){
	    if (wikid.startsWith(listHook)){
		article.setType(ArticleType.LIST);
		return article;
	    }
	}

	/* ************************************************************************ */

	/*
	 * If not, get the content and use the first sentence (100 characters, if tere are) 
	 * to check if there is a redirect, or a disambiguation sentence.
	 */
	String rawContent = getContent(page);
	String toCheck = (rawContent.length() >= 100) ? rawContent.substring(0, 100) : rawContent.substring(0, rawContent.length());

	/* Filter to capture REDIRECT articles */
	for (String redirectHook : lang.getRedirectIdentifiers()){
	    if (toCheck.matches("^.*#" + redirectHook + "\\b.*")){
		article.setType(ArticleType.REDIRECT);
		return article;
	    }
	}

	/*
	 * Check if it is a disambiguation article.
	 */	
	article.setDisambiguation(getDisambiguation(wikid));

	/* Filter to capture DISAMBIGUATION articles */
	for (String disambiguationHook : lang.getDisambiguationIdentifiers()){
	    if (getDisambiguation(wikid).equals(disambiguationHook)){
		article.setType(ArticleType.DISAMBIGUATION);
		return article;
	    }
	}

	/* Filter to capture DISAMBIGUATION articles with text on the abstract */
	for (String disambiguationHook : lang.getDisambiguationTextIdentifiers()){
	    if (toCheck.contains(disambiguationHook)){
		article.setType(ArticleType.DISAMBIGUATION);
		return article;
	    }
	}

	/* ************************************************************************ */

	/*
	 * if it is not of all of this... it is an article! :) 
	 */
	article.setType(ArticleType.ARTICLE);

	// split the article in blocks - using all the ## headings ##
	Map<String, String> blocks = getBlocksOfContentFromXml(page);

	// select the emphasized words in the abstract as aliases for the primary entity
	if(blocks.containsKey("#Abstract"))
	    article.setAliases(getAlias(blocks.get("#Abstract")));

	// get all the sentences from the article -- large processing here!
	article.setContent(splitSentences(blocks));
	article.setBio(cleaner.retrieveAllWithSpecification(page, "{{", "bio", "}}").get(0));
	article.setTables(MarkupParser.getTablesFromXml(page, lang, cleaner));
	article.setLists(MarkupParser.getListsFromXml(page, lang));
	article.setWikilinks(getWikilinks(article.getContent()));

	return article;
    }

    /**
     * 
     * @param page
     * @return
     */
    private String getContent(String page){
	return cleanContent(XMLParser.getWikiMarkup(page));
    }

    /**
     * 
     * @param page
     * @return
     */
    private Map<String, String> getBlocksOfContentFromXml(String page) {
	String content = XMLParser.getWikiMarkup(page);

	/*
	 *  split the content in: 
	 *  name section --> content
	 */
	Map<String, String> blocks = MarkupParser.fragmentArticle(content);

	/*
	 * for each block, clean the content and replace the entry in the map
	 * name section --> clean content
	 */
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    blocks.put(block.getKey(), cleanContent(block.getValue()));
	}

	return blocks;
    }

    /**
     * 
     * @param blocks
     * @return
     */
    private Map<String, List<String>> splitSentences(Map<String, String> blocks){

	// remove '''emphasis''' from all the blocks
	for(Map.Entry<String, String> block : blocks.entrySet())
	    blocks.put(block.getKey(), removeEmphasis(block.getValue()));

	/*
	 * split each block in sentences
	 * (here we deal with wiki-links!)
	 * name section --> sentence1, sentence2, ...
	 */
	Map<String, List<String>> sentences = MarkupParser.fragmentParagraph(blocks, cleaner, lang);

	/*
	 * filter out undesired blocks
	 * name section (desired) --> sentence1, sentence2, ...
	 */
	sentences = MarkupParser.removeUndesiredBlocks(sentences, lang);
	return sentences;
    }

    /**
     * 
     * @param content
     * @return
     */
    private String cleanContent(String content){
	String cleanBlock = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(content));
	cleanBlock = MarkupParser.removeGallery(cleanBlock);
	cleanBlock = MarkupParser.fixUnitConversion(cleanBlock);
	cleanBlock = MarkupParser.removeMath(cleanBlock);
	cleanBlock = MarkupParser.removeNoToc(cleanBlock);
	cleanBlock = MarkupParser.removeInterWikiLinks(cleanBlock);
	cleanBlock = MarkupParser.removeRefs(cleanBlock);
	cleanBlock = MarkupParser.removeCategoryLinks(cleanBlock, lang);
	cleanBlock = MarkupParser.removeHtmlComments(cleanBlock);
	cleanBlock = MarkupParser.removeHtmlTags(cleanBlock);
	cleanBlock = MarkupParser.removeIndentation(cleanBlock);
	return cleanBlock;
    }


    /**
     * }}}}}}}}}}}}}}}}}}}}
     * 
     * @param s
     * @return
     */
    protected Map<String, Set<String>> getWikilinks(Map<String, List<String>> sentences) {
	Map<String, Set<String>> wikilinks = new HashMap<String, Set<String>>();
	Pattern LINKS1 = Pattern.compile("(?<=\\[\\[)([^\\]]+)\\|([^\\]]+)(?=\\]\\])");
	Pattern LINKS2 = Pattern.compile("(?<=\\[\\[)([^\\]\\|]+)(?=\\]\\])");

	for(Map.Entry<String, List<String>> section : sentences.entrySet()){
	    for(String sentence : section.getValue()){

		// composite wikilinks, e.g. [[Barack_Obama|Obama]]
		Matcher m = LINKS1.matcher(sentence);
		while(m.find()){
		    String rendered = m.group(1).replaceAll("_", " ");
		    String wikid = m.group(2);
		    if (!wikilinks.containsKey(rendered))
			wikilinks.put(rendered, new TreeSet<String>());
		    wikilinks.get(rendered).add(wikid);
		}

		// simple wikilinks, e.g. [[Barack_Obama]]
		m = LINKS2.matcher(sentence);
		while(m.find()){
		    String wikid = m.group(1);
		    String rendered = m.group(1).replaceAll("_", " ");
		    if (!wikilinks.containsKey(wikid))
			wikilinks.put(wikid, new TreeSet<String>());
		    wikilinks.get(wikid).add(rendered);
		}
	    }
	}
	return wikilinks;
    }


    /**
     * We extract the bold (or italic) names that are in the block.
     * 
     * @param s
     * @return
     */
    protected List<String> getAlias(String block) {
	List<String> aliases = new LinkedList<String>();
	Pattern ALIASES = Pattern.compile("('''|''''')(.*?)('''|''''')");
	Matcher m = ALIASES.matcher(block);
	while(m.find()){
	    String alias = m.group(2).replaceAll("(\\[|\\]|'')*", "").replaceAll("^'", "").replaceAll("'$", "").trim();
	    if (!alias.isEmpty())
		aliases.add(alias);
	}
	return aliases;
    }

    /**
     * 
     * @param content
     * @return
     */
    protected String removeEmphasis(String block) {
	Pattern EMPHASIS = Pattern.compile("('''|'')");
	return EMPHASIS.matcher(block).replaceAll("");
    }

    /**
     * 
     * @param wikid
     * @return
     */
    protected String getDisambiguation(String wikid){
	String disambiguation = "-";
	Pattern DISAMBIGATION = Pattern.compile("_\\(.*\\)$");
	Matcher m = DISAMBIGATION.matcher(wikid);
	if (m.find()){
	    disambiguation = m.group(0).replaceAll("(_\\(|\\))*", "").trim();
	}
	return disambiguation;
    }

    /**
     * 
     * @param title
     */
    protected String clean(String wikid){
	String title = wikid;
	Pattern DISAMBIGATION = Pattern.compile("^.+(?=_\\(.*\\))");
	Matcher m = DISAMBIGATION.matcher(wikid);
	if (m.find()){
	    title = m.group(0);
	}
	return title.replaceAll("_", " ").trim();
    }

}
