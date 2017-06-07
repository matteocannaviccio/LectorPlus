package it.uniroma3.extractor.parser;

import java.util.Map;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
/**
 * 
 * 
 * 
 * @author matteo
 *
 */
public class WikiParser {

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
	String wikid = Lector.getXmlParser().extractsWikid(page);
	String title = Lector.getTextParser().getTitle(wikid);
	String id = Lector.getXmlParser().getFieldFromXmlPage(page, "id");
	String namespace = Lector.getXmlParser().getFieldFromXmlPage(page, "ns");
	String originalMarkup = Lector.getXmlParser().getWikiMarkup(page);
	WikiArticle article = new WikiArticle(wikid, id, title, namespace, Lector.getWikiLang().getLang(), originalMarkup);

	try{
	    /*
	     * Process only WikiArticle of type ARTICLE.
	     * (we could extend the process to other kind of articles here...)
	     */
	    ArticleType type = Lector.getArticleTyper().findArticleType(article);
	    switch(type){

	    case ARTICLE:
	    case LIST:
		article.setType(type);
		processArticle(article);
		break;

	    default:
		article.setType(type);
		break;
	    }
	    
	}catch(Exception e){
	    article.setType(ArticleType.ERROR);
	    System.out.println("Error in processing article: " + article.getWikid());
	    e.printStackTrace();
	}
	return article;
    }


    /**
     * Here we process he article using a particular order.
     * 
     * (1)	First of all, we fragment the article in blocks starting 
     * 		from its original WikiMarkup content, and using the 
     * 		=== header === of each section, up to h4. Note, we consider
     * 		all the section in a flat order, without keeping their nesting.
     * 
     * 
     * @param article
     * @return
     */
    private WikiArticle processArticle(WikiArticle article){
	/*
	 * (1)	blocks is a map of contents of sections keyed by their header.
	 * 	The section in the first position is the #Abstract.
	 */
	Map<String, String> blocks = Lector.getBlockParser().fragmentArticle(article.getOriginalMarkup());

	/* ********************* */

	/*
	 * Extract structured contents from the WikiMarkup.
	 * 
	 * For now:
	 * - TABLES
	 * - LISTS
	 */
	if(Configuration.extractTables())
	    article.setTables(Lector.getBlockParser().extractTables(blocks));

	if(Configuration.extractLists())
	    article.setLists(Lector.getBlockParser().extractLists(blocks));

	/* ********************* */

	/*
	 * Remove the noise.
	 */
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    String blockContent = Lector.getTextParser().fixSomeTemplates(block.getValue()); 	// we try to fix easy templates
	    blockContent = Lector.getTextParser().removeNoise(blockContent); 		
	    blockContent = Lector.getTextParser().removeUselessWikilinks(blockContent); 	// commonsense wikilinks
	    blocks.put(block.getKey(), blockContent);
	}
	/* ********************* */

	/*
	 * Harvest all the wikilinks from the WHOLE article, 
	 * setting the variable in the WikiArticle object.
	 */
	if (!Configuration.getOnlyTextWikilinks()){
	    for (Map.Entry<String, String> block : blocks.entrySet()){
		blocks.put(block.getKey(), Lector.getMarkupParser().harvestAllWikilinks(block.getValue(), article));
	    }
	}
	/* ********************* */

	/*
	 * Textual cleaning of the articles from, essentially from:
	 *  - lists 
	 *  - tables
	 *  - infobox
	 *  - etc. 
	 */
	for (Map.Entry<String, String> block : blocks.entrySet()){
	    blocks.put(block.getKey(), Lector.getTextParser().removeStructuredContents(block.getValue()));
	}
	/* ********************* */

	/*
	 * Harvest all the wikilinks from the TEXT article, 
	 * setting the variable in the WikiArticle object.
	 */
	if (Configuration.getOnlyTextWikilinks()){
	    for (Map.Entry<String, String> block : blocks.entrySet()){
		blocks.put(block.getKey(), Lector.getMarkupParser().harvestAllWikilinks(block.getValue(), article));
	    }
	}
	/* ********************* */

	/*
	 * Finally we remove undesired sections, expressed in the WikiLanguage file.
	 */
	Lector.getBlockParser().removeUndesiredBlocks(blocks);
	/* ********************* */

	/* 
	 * Finally, we assign blocks to the article performing some further cleaning 
	 * (normalize spaces between token and sentences, remove bold text, etc.)
	 */
	article.setAliases(Lector.getTextParser().getAlias(Lector.getBlockParser().getAbstractSection(blocks)));
	article.setBlocks(Lector.getTextParser().finalCleanText(blocks));
	/* ********************* */
	
	return article;
    }

}
