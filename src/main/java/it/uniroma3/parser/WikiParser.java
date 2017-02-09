package it.uniroma3.parser;

import java.util.ArrayList;
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
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.model.WikiArticle.ArticleType;
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
	 * If not, get the content and use the first sentence to check if it is a redirect.
	 */

	// get all the sentences from the article -- large processing here!
	Map<String, List<String>> content = getContentFromXml(page);
	article.setContent(content);


	/* Filter to capture REDIRECT articles */
	for (String redirectHook : lang.getRedirectIdentifiers()){
	    if(content.containsKey("#Abstract"))
		if (content.get("#Abstract").get(0).matches("^.*#" + redirectHook + "\\b.*")){
		    article.setType(ArticleType.REDIRECT);
		    return article;
		}
	}

	/* ************************************************************************ */

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
	if(content.containsKey("#Abstract")){
	    for (String disambiguationHook : lang.getDisambiguationTextIdentifiers()){
		for(String sentenceAbstract : content.get("#Abstract"))
		    if (sentenceAbstract.contains(disambiguationHook)){
			article.setType(ArticleType.DISAMBIGUATION);
			return article;
		    }
	    }
	}

	/* ************************************************************************ */

	/*
	 * if it is not of all of this... it is an article! :) 
	 */
	article.setType(ArticleType.ARTICLE);
	if(content.containsKey("#Abstract"))
	    article.setAliases(getAlias(content.get("#Abstract")));

	article.setBio(cleaner.retrieveAllWithSpecification(page, "{{", "bio", "}}").get(0));
	article.setContent(removeEmphasis(content));
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
    private Map<String, List<String>> getContentFromXml(String page) {
	String content = XMLParser.getWikiMarkup(page);
	Map<String, List<String>> sentences = null;
	try {
	    /*
	     *  split the content in: 
	     *  name section --> content
	     */
	    Map<String, String> blocks = MarkupParser.fragmentArticle(content);

	    /*
	     * clean the content of each block
	     * name section --> clean content
	     */
	    for (Map.Entry<String, String> block : blocks.entrySet()){
		String cleanBlock = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(block.getValue()));
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
		blocks.put(block.getKey(), cleanBlock);
	    }

	    /*
	     * split each block in sentences
	     * (here we deal with wiki-links!)
	     * name section --> sentence1, sentence2, ...
	     */
	    sentences = MarkupParser.fragmentParagraph(blocks, cleaner, lang);

	    /*
	     * filter out undesired blocks
	     * name section (desired) --> sentence1, sentence2, ...
	     */
	    sentences = MarkupParser.removeUndesiredBlocks(sentences, lang);

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	    System.out.println(page);
	    System.out.println(content);
	    System.exit(1);
	}

	return sentences;
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
     * We extract the bold (or italic) names that are in s.
     * 
     * @param s
     * @return
     */
    protected List<String> getAlias(List<String> s) {
	List<String> aliases = new LinkedList<String>();
	for(String sentence : s){
	    Pattern ALIASES = Pattern.compile("('''|''''')(.*?)('''|''''')");
	    Matcher m = ALIASES.matcher(sentence);
	    while(m.find()){
		String alias = m.group(2).replaceAll("(\\[|\\]|'')*", "").replaceAll("^'", "").replaceAll("'$", "").trim();
		if (!alias.isEmpty())
		    aliases.add(alias);
	    }
	}
	return aliases;
    }

    /**
     * 
     * @param content
     * @return
     */
    protected Map<String, List<String>> removeEmphasis(Map<String, List<String>> content) {
	Pattern EMPHASIS = Pattern.compile("('''|'')");
	for(Map.Entry<String, List<String>> paragraph : content.entrySet()){
	    List<String> cleanSentences = new ArrayList<String>(paragraph.getValue().size());
	    for(String sentence : paragraph.getValue()){
		cleanSentences.add(EMPHASIS.matcher(sentence).replaceAll(""));
	    }
	    content.put(paragraph.getKey(), cleanSentences);
	}
	return content;
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
