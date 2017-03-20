package it.uniroma3.parser;

import java.util.Map;

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
    private WikiLanguage lang;
    private ArticleTyper articleTyper;
    private XMLParser xmlParser;
    private BlockParser blockParser;
    private TextParser textParser;

    /**
     * 
     * @param lang
     */
    public WikiParser(WikiLanguage lang){
	this.lang = lang;
	this.articleTyper = new ArticleTyper(lang);
	this.xmlParser = new XMLParser();
	this.blockParser = new BlockParser(lang);
	this.textParser = new TextParser(lang);
    }

    /**
     * It parses the XML extracting all the metadata 
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
	String wikid = xmlParser.extractsWikid(page);
	String title = textParser.getTitle(wikid);
	String id = xmlParser.getFieldFromXmlPage(page, "id");
	String namespace = xmlParser.getFieldFromXmlPage(page, "ns");
	String originalMarkup = xmlParser.getWikiMarkup(page);
	
	WikiArticle article = new WikiArticle(wikid, id, title, namespace, lang, originalMarkup);

	/*
	 * Process only WikiArticle of type ARTICLE.
	 * (we could extend the process to other kind of articles here...)
	 */
	ArticleType type = articleTyper.findArticleType(article);
	switch(type){
	
	case ARTICLE:
	    article.setType(type);
	    processArticle(article);
	    break;
	    
	default:
	    article.setType(type);
	    break;
	}
	return article;
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
	 * 
	 * For now:
	 * - TABLES
	 * - LISTS
	 */
	if(Configuration.extractTables())
	    article.setTables(blockParser.extractTables(blocks));

	if(Configuration.extractLists())
	    article.setLists(blockParser.extractLists(blocks));

	/*
	 * Removing the noise.
	 */
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    String blockContent = textParser.fixSomeTemplates(block.getValue());
	    blockContent = textParser.removeNoise(blockContent, lang); // lang is needed for categories
	    blockContent = textParser.removeUselessWikilinks(blockContent); // commonsense and blacklist
	    blocks.put(block.getKey(), blockContent);
	}

	/*
	 * Store the first clean sentence of the article and clean it, 
	 * in order to extract seed types and run NLP tools.
	 */
	article.setFirstSentence(textParser.obtainCleanFirstSentence(blockParser.getAbstractSection(blocks)));

	/*
	 * Harvest all the wikilinks from the WHOLE article, 
	 * setting the variable in the WikiArticle object.
	 */
	if (!Configuration.getOnlyTextWikilinks())
	    for (Map.Entry<String, String> block : blocks.entrySet()){
		blocks.put(block.getKey(), textParser.harvestWikilinks(block.getValue(), article));
	    }

	/*
	 * Textual cleaning of the articles from, essentially from:
	 *  - <refs>
	 *  - [[File:]]
	 *  - lists 
	 *  - tables
	 *  - infobox
	 *  - etc. 
	 */
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    blocks.put(block.getKey(), textParser.removeStructuredContents(block.getValue()));
	}

	/*
	 * Harvest all the wikilinks from the TEXT article, 
	 * setting the variable in the WikiArticle object.
	 */
	if (Configuration.getOnlyTextWikilinks())
	    for (Map.Entry<String, String> block : blocks.entrySet()){
		blocks.put(block.getKey(), textParser.harvestWikilinks(block.getValue(), article));
	    }

	/*
	 * Finally we remove undesired sections, expressed in the WikiLanguage file.
	 */
	blockParser.removeUndesiredBlocks(blocks, lang);

	/* Finally, we:
	 * (1) extract the disambiguation text from the wikid. For example, Cold_war_(movie) --> "movie"
	 * (2) extract the aliases from the abstract section (token in bold characters).
	 * (3) assign blocks to the article performing some further cleaning (normalize spaces between token 
	 * 								and sentences, remove bold text, etc.)
	 */
	article.setDisambiguation(textParser.getDisambiguation(article.getWikid()));
	article.setAliases(textParser.getAlias(blockParser.getAbstractSection(blocks)));
	article.setBlocks(textParser.finalCleanText(blocks));

	return article;
    }

}
