package it.uniroma3.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.model.WikiArticle.ArticleType;
/**
 * This parser is able to assign a specific type to 
 * the article using its wikid or original wiki-markup.
 * 
 * @author matteo
 *
 */
public class ArticleTyper {

    private WikiLanguage lang;

    /**
     * 
     * @param lang
     */
    public ArticleTyper(WikiLanguage lang){
	this.lang = lang;
    }

    /**
     * Check if the article can be a REDIRECT using the whole text.
     * if it contains the reference #REDIRECT.
     * 
     * @param blocks
     * @return
     */
    private boolean checkIsRedirect(WikiArticle article){
	Pattern REDIRECT = Pattern.compile("#redirect", Pattern.CASE_INSENSITIVE);
	Matcher m = REDIRECT.matcher(article.getOriginalMarkup());
	if (m.find())
	    return true;
	return false;
    }

    /**
     * Check if the article can be a DATE ARTICLE using the templates in the abstract section.
     * 
     * @param blocks
     * @return
     */
    private boolean checkIsDate(WikiArticle article){
	if (article.getOriginalMarkup().contains("{{Day}}") 
		|| article.getOriginalMarkup().contains("{{Year article header|")){
	    return true;
	}
	return false;
    }

    /**
     * Check if the article can be a DISAMBIGUAITON using the text in the abstract 
     * section or a template in the whole article.
     * 
     * @param blocks
     * @param article
     * @return
     */
    private boolean checkIsDisambiguation(WikiArticle article){
	List<Pattern> DIS_PAT = new ArrayList<Pattern>();
	Pattern DIS1 = Pattern.compile("\\{\\{" + "[^\\(\\[]*?" + "\\b(disambiguation)\\b" + "([^\\)\\]]*?)" + "\\}\\}", 
		Pattern.CASE_INSENSITIVE);
	Pattern DIS3 = Pattern.compile("\\{\\{" + "[^\\(\\[]*?" + "\\b(disamb)\\b" + "([^\\)\\]]*?)" + "\\}\\}", 
		Pattern.CASE_INSENSITIVE);
	Pattern DIS4 = Pattern.compile("\\{\\{" + "[^\\(\\[]*?" + "\\b(disambig)\\b" + "([^\\)\\]]*?)" + "\\}\\}", 
		Pattern.CASE_INSENSITIVE);
	DIS_PAT.add(DIS1);
	DIS_PAT.add(DIS3);
	DIS_PAT.add(DIS4);
	for (Pattern p : DIS_PAT){
	    Matcher m = p.matcher(article.getOriginalMarkup());
	    while (m.find()){
		if (!m.group(2).contains("needed")){
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Find the ArticleType using the wikid.
     * 
     * Generally particular types of articles have particular wikids 
     * such as "Category:Italian footballer" or "Discussion:something else".
     *  
     * @param article
     * @return
     */
    public ArticleType findArticleType(WikiArticle article){
	/* Filter to capture REDIRECT articles */
	if (checkIsRedirect(article))
	    return ArticleType.REDIRECT;

	/* Filter to capture DISAMBIGUATION articles */
	if (checkIsDisambiguation(article))
	    return ArticleType.DISAMBIGUATION;

	/* Filter to capture DATE articles */
	if (checkIsDate(article))
	    return ArticleType.DATE;

	/* Filter to capture WIKIPEDIA portal articles */
	for (String portalHook : lang.getPortalIdentifiers())
	    if (article.getWikid().startsWith(portalHook + ":")){
		return ArticleType.PORTAL;
	    }

	/* Filter to capture FILE articles */
	for (String fileHook : lang.getFileIdentifiers())
	    if (article.getWikid().startsWith(fileHook + ":")){
		return ArticleType.FILE;
	    }

	/* Filter to capture HELP articles */
	for (String helpHook : lang.getHelpIdentifiers())
	    if (article.getWikid().startsWith(helpHook + ":")){
		return ArticleType.HELP;
	    }

	/* Filter to capture CATEGORY articles */
	for (String categoryHook : lang.getCategoryIdentifiers())
	    if (article.getWikid().startsWith(categoryHook + ":")){
		return ArticleType.CATEGORY;
	    }

	/* Filter to capture TEMPLATE articles */
	for (String templateHook : lang.getTemplateIdentifiers())
	    if (article.getWikid().startsWith(templateHook + ":")){
		return ArticleType.TEMPLATE;
	    }

	/* Filter to capture DISCUSSION articles */
	for (String discussionHook : lang.getDiscussionIdentifiers())
	    if (article.getWikid().startsWith(discussionHook + ":")){
		return ArticleType.DISCUSSION;
	    }

	/* Filter to capture LIST articles */
	if (article.getWikid().startsWith("List_of_")){
	    return ArticleType.LIST;
	}

	/* Filter to capture OUTLINE articles */
	if (article.getWikid().startsWith("Outline_of_")){
	    return ArticleType.OUTLINE;
	}

	return ArticleType.ARTICLE;
    }

}
