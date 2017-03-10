package it.uniroma3.parser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Configuration;
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
    protected WikiLanguage lang;
    protected XMLParser xmlParser;
    protected BlockParser blockParser;
    protected TextParser textParser;

    /**
     * 
     * @param lang
     */
    public WikiParser(WikiLanguage lang){
	this.lang = lang;
	this.xmlParser = new XMLParser();
	this.blockParser = new BlockParser(lang);
	this.textParser = new TextParser(lang);
    }

    /**
     * It parse the xml extracting all the metadata 
     * such as wikid, title, id, etc. Then it creates 
     * a WikiArticle object assigning an ArticltType.
     * 
     * If the article is an ARTICLE, we further process it.
     * 
     * @param page
     * @return
     */
    public WikiArticle createArticleFromXml(String page){

	/*
	 * Obtain the title and metadata of the article from the xml and create a WikiArticle.
	 */
	String wikid = this.extractsWikid(page);
	String title = this.getTitle(wikid);
	String id = xmlParser.getFieldFromXmlPage(page, "id");
	String namespace = xmlParser.getFieldFromXmlPage(page, "ns");
	WikiArticle article = new WikiArticle(wikid, id, title, namespace, lang);

	/*
	 * Process only WikiArticle of type ARTICLE.
	 * (we could extend the process to other kind of articles here...)
	 */
	ArticleType type = findArticleType(article);
	switch(type){
	case ARTICLE:
	    article.setType(type);
	    article.setOriginalMarkup(xmlParser.getWikiMarkup(page));
	    processArticle(article);
	    break;
	default:
	    article.setType(type);
	    break;
	}
	return article;
    }

    /**
     * Uses the wikid of the article to find the type of the article.
     * Generally particular types of articles have particular wikids 
     * such as "Category:Italian footballer" or "Discussion:something else".
     * 
     * It returns an ArticleType parsing the wikid of the page.
     * 
     * @param article
     * @return
     */
    private ArticleType findArticleType(WikiArticle article){
	/* Filter to capture WIKIPEDIA portal articles */
	for (String portalHook : lang.getPortalIdentifiers()){
	    if (article.getWikid().startsWith(portalHook + ":")){
		return ArticleType.PORTAL;
	    }
	}

	/* Filter to capture FILE articles */
	for (String fileHook : lang.getFileIdentifiers()){
	    if (article.getWikid().startsWith(fileHook + ":")){
		return ArticleType.FILE;
	    }
	}

	/* Filter to capture HELP articles */
	for (String helpHook : lang.getHelpIdentifiers()){
	    if (article.getWikid().startsWith(helpHook + ":")){
		return ArticleType.HELP;
	    }
	}

	/* Filter to capture CATEGORY articles */
	for (String categoryHook : lang.getCategoryIdentifiers()){
	    if (article.getWikid().startsWith(categoryHook + ":")){
		return ArticleType.CATEGORY;
	    }
	}

	/* Filter to capture TEMPLATE articles */
	for (String templateHook : lang.getTemplateIdentifiers()){
	    if (article.getWikid().startsWith(templateHook + ":")){
		return ArticleType.TEMPLATE;
	    }
	}

	/* Filter to capture DISCUSSION articles */
	for (String discussionHook : lang.getDiscussionIdentifiers()){
	    if (article.getWikid().startsWith(discussionHook + ":")){
		return ArticleType.DISCUSSION;
	    }
	}

	/* Filter to capture LIST articles */
	for (String listHook : lang.getListIdentifiers()){
	    if (article.getWikid().startsWith(listHook)){
		return ArticleType.LIST;
	    }
	}

	/* Filter to capture DISAMBIGUATION articles */	
	for (String disambiguationHook : lang.getDisambiguationIdentifiers()){
	    String disambiguationToken = getDisambiguation(article.getWikid());
	    if (disambiguationToken != null && disambiguationToken.equals(disambiguationHook)){
		return ArticleType.DISAMBIGUATION;
	    }
	}

	return ArticleType.ARTICLE;
    }


    /**
     * Here we process he article using a particular order.
     * 
     * 1-
     * First of all, we fragment the article in blocks starting 
     * from its original WikiMarkup content, and using the 
     * === header === of each section, up to h4. Note, we consider
     * all the section in a flat order, without keeping their nesting.
     * 
     * 2-
     * Then we extract structured contents that could be of interest
     * such as tables and lists. We consider infoboxes as tables.
     * We extract them now becuase we remve them from the blocks in the
     * next stage.
     * 
     * 3-
     * For each block, we use a text parser {@link it.uniroma3.parser.
     * TextParser} to clean most of noisy contents, replacing the clean 
     * block in the map. In particular, we replace each "structured" 
     * content (table, list, template,..) with empty content. In this way,
     * a block that contains only tables would be empty.
     * 
     * 4-
     * In this stage we filter out blocks that we do not want to process.
     * We remove the empty blocks obtained above and we also filter out
     * undesired blocks using matching with specific headers declared in 
     * the config file. 
     * 
     * 5-
     * Later, after checking that it is not an redirect article, we extract 
     * some others information from wikid and the text. We then extract the 
     * aliases and only after we can finish to clean the text removing bold 
     * tokens and content inside parenthesis.
     * 
     * 
     * @param article
     * @return
     */
    private WikiArticle processArticle(WikiArticle article){
	/*
	 * blocks is a map of contents of sections keyed by their header.
	 * The section in the first position is the #Abstract.
	 */
	Map<String, String> blocks = blockParser.fragmentArticle(article.getOriginalMarkup());

	/*
	 * Extract structured contents from the WikiMarkup.
	 */
	// "bio" are used in italian wikipedia
	// article.setBio(blockParser.retrieveAllWithSpecification(page, "{{", "bio", "}}").get(0));

	if(Configuration.extractTables())
	    article.setTables(blockParser.extractTables(blocks));

	if(Configuration.extractLists())
	    article.setLists(blockParser.extractLists(blocks));

	/*
	 * Clean the text of the articles from <refs>, [[File:]], lists, 
	 * tables, infobox, etc. and removed undesired blocks from the map.
	 */
	for (Map.Entry<String, String> block : blocks.entrySet())
	    blocks.put(block.getKey(), textParser.hugeCleaningText(block.getKey(), block.getValue(), article, lang));
	blockParser.removeUndesiredBlocks(blocks, Configuration.getOnlyAbstractFlag());

	if (Configuration.getEDTestingMode())
	    blockParser.removeUndesiredBlocks(article.cleanBlocks(), Configuration.getOnlyAbstractFlag());

	/*
	 * If it is a REDIRECT or a DISAMBIGUATION, we set the type to the article and immediately return it.
	 */
	if (checkIsRedirect(blocks)){
	    article.setType(ArticleType.REDIRECT);
	    return article;
	}
	
	if (checkIsDisambiguation(blocks)){
	    article.setType(ArticleType.DISAMBIGUATION);
	    return article;
	}

	/*
	 * if it is not of all of this... it is an article! :) 
	 * 
	 * We process it:
	 * (1) extracting the disambiguation text from the wikid. For example, "movie" <-- Cold_war_(movie)
	 * (2) extracting the first sentence (clean because we remove parenthesis and wikilinks from it.)
	 * (3) extracting the aliases from the abstract section (token in bold characters).
	 * (4) finally, we assignblock to the article performing some further cleaning of the text, which was 
	 * not possible before. We remove parenthesis, bold text and normalize spaces between token and sentences. 
	 */
	String abstractSection = textParser.removeLinks(blockParser.getAbstractSection(blocks));
	article.setFirstSentence(TextParser.splitSentences(abstractSection).get(0));
	article.setDisambiguation(getDisambiguation(article.getWikid()));
	article.setAliases(textParser.getAlias(abstractSection));
	article.setBlocks(textParser.finalCleanText(blocks));

	return article;
    }

    /**
     * Check if the article can be a REDIRECT using the text in the abstract section.
     * if it contains the reference #REDIRECT.
     * 
     * @param blocks
     * @return
     */
    private boolean checkIsRedirect(Map<String, String> blocks){
	String abstractSection = textParser.removeLinks(blockParser.getAbstractSection(blocks));
	boolean isRedirect = false;
	for (String redirectHook : lang.getRedirectIdentifiers()){
	    if (abstractSection.matches("^.*#" + redirectHook + "\\b.*")){
		isRedirect = true;
	    }
	}
	return isRedirect;
    }
    
    /**
     * Check if the article can be a DISAMBIGUAITON using the text in the abstract section.
     * if it contains the reference #REDIRECT.
     * 
     * @param blocks
     * @return
     */
    private boolean checkIsDisambiguation(Map<String, String> blocks){
	String abstractSection = textParser.removeLinks(blockParser.getAbstractSection(blocks));
	boolean isDisambiguation = false;
	for (String disambiguationHook : lang.getDisambiguationTextIdentifiers()){
	    if (abstractSection.contains(disambiguationHook)){
		isDisambiguation = true;
	    }
	}
	return isDisambiguation;
    }

    /**
     * 
     * @param wikid
     * @return
     */
    private String getDisambiguation(String wikid){
	String disambiguation = null;
	Pattern DISAMBIGATION = Pattern.compile("_\\(.*\\)$");
	Matcher m = DISAMBIGATION.matcher(wikid);
	if (m.find()){
	    disambiguation = m.group(0).replaceAll("(_\\(|\\))*", "").trim();
	}
	return disambiguation;
    }

    /**
     * It calls the XMLParser to extract a files 
     * named "title" from the xml.
     * 
     * @param page
     * @return
     */
    public String extractsWikid(String page){
	return xmlParser.getFieldFromXmlPage(page, "title").replaceAll(" ", "_");
    }
    
    /**
     * It clean the wikid obtaining the title,
     * without underscores or disambiguations.
     * 
     * @param wikid
     * @return
     */
    private String getTitle(String wikid){
	wikid = wikid.replaceAll("_", " ");
	wikid = wikid.replaceAll(" \\(\\w+\\)$", "");
	return wikid;
    }

}
