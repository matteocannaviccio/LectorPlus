package it.uniroma3.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import it.uniroma3.lectorplus.WikiArticle;
import it.uniroma3.lectorplus.WikiArticle.ArticleType;
import it.uniroma3.lectorplus.WikiLanguage;
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
	String wikid = XMLParser.getFieldFromXml(page, "title").replaceAll(" ", "_");
	String title = getCleanTitle(wikid);
	String id = XMLParser.getFieldFromXml(page, "id");
	String namespace = XMLParser.getFieldFromXml(page, "ns");

	WikiArticle article = new WikiArticle(wikid, id, title, namespace, lang);

	/* ************************************************************************ */

	/* Filter to capture WIKIPEDIA portal articles */
	for (String portalHook : lang.getPortalIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(portalHook + ":")){
		article.setType(ArticleType.PORTAL);
		return article;
	    }
	}

	/* Filter to capture FILE articles */
	for (String fileHook : lang.getFileIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(fileHook + ":")){
		article.setType(ArticleType.FILE);
		return article;
	    }
	}
	
	/* Filter to capture HELP articles */
	for (String helpHook : lang.getHelpIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(helpHook + ":")){
		article.setType(ArticleType.HELP);
		return article;
	    }
	}

	/* Filter to capture CATEGORY articles */
	for (String categoryHook : lang.getCategoryIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(categoryHook + ":")){
		article.setType(ArticleType.CATEGORY);
		return article;
	    }
	}

	/* Filter to capture TEMPLATE articles */
	for (String templateHook : lang.getTemplateIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(templateHook + ":")){
		article.setType(ArticleType.TEMPLATE);
		return article;
	    }
	}

	/* Filter to capture DISCUSSION articles */
	for (String discussionHook : lang.getDiscussionIdentifiers()){
	    if (getCleanTitle(wikid).startsWith(discussionHook + ":")){
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

	// get all the sentences from the article
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

	/* if it is not of all of this... it is an article! :)  */
	article.setType(ArticleType.ARTICLE);
	if(content.containsKey("#Abstract"))
	    article.setAliases(getAlias(content.get("#Abstract")));

	article.setBio(cleaner.retrieveAllWithSpecification(page, "{{", "bio", "}}").get(0));
	article.setContent(removeEmphasis(content));
	article.setTables(MarkupParser.getTablesFromXml(page, lang, cleaner));
	article.setLists(MarkupParser.getListsFromXml(page, lang));

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
    protected String getCleanTitle(String wikid){
	String title = wikid;
	Pattern DISAMBIGATION = Pattern.compile("^.+(?=_\\(.*\\))");
	Matcher m = DISAMBIGATION.matcher(wikid);
	if (m.find()){
	    title = m.group(0);
	}
	return title.replaceAll("_", " ").trim();
    }
    
    


}
