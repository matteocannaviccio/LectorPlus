package it.uniroma3.parser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.util.Reader;

/**
 * 
 * 
 * 
 * 
 * @author matteo
 *
 */
public class MarkupParser {

    private Set<String> blacklist;

    /**
     * 
     */
    public MarkupParser(){
	this.blacklist = new HashSet<String>();
	this.blacklist.addAll(Reader.getLines(Configuration.getCurrenciesList()));
	this.blacklist.addAll(Reader.getLines(Configuration.getNationalitiesList()));
	this.blacklist.addAll(Reader.getLines(Configuration.getProfessionsList()));
    }

    /**
     * Detect all the wikilinks and normalize them in text.
     * 
     * E.g.:  .
     * 		.. artist SE-ORG<Britney_Spears> by SE-ORG<JIVE_Records>.
     * became: 
     * 		... artist Britney Spears by JIVE Records.
     * 
     * @param originalText
     * @return
     */
    public String removeAllWikilinks(String originalText) {
	Pattern WIKILINK = Pattern.compile("[A-Z-]+" + "<([^>]*?)>");
	Matcher m = WIKILINK.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	while(m.find()){
	    String renderedName = m.group(1).replaceAll("_", " ");
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(renderedName));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * Detect wrong marked wikilinks that were originally rendered with a template
     * but we remove them in a previous step.
     * 
     * For example, wikilinks such as:
     * 		[[Mount_Rushmore#Height|{{template}}]]
     * 
     * become, after the cleaning step:
     * 		[[Mount_Rushmore#Height|]]
     * 
     * @param originalText
     * @return
     */
    public String cleanEmptyTemplateWikilinks(String originalText){
	Pattern TEMPLATEWIKILINK = Pattern.compile("\\[\\[+" + "([^\\]]+)\\|" + "\\]\\]+");
	Matcher m = TEMPLATEWIKILINK.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	while(m.find()){
	    m.appendReplacement(cleanText, Matcher.quoteReplacement(m.group(1).replaceAll("_", " ").replaceAll("#", " ")));
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * 
     * @param originalText
     * @return
     */
    public String cleanAllWikilinks(String originalText) {
	Pattern ENTITY = Pattern.compile("('')?" + "\\[\\[+" + "([^\\]\\|]*\\|)?" + "('')?([^\\]]*?)('')?" + "\\]\\]+" + "('')?");
	Matcher m = ENTITY.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	try {
	    String rendered;
	    while(m.find()){
		if ( m.group(3) != null && m.group(5) != null){
		    rendered = m.group(3) + m.group(4) + m.group(5);
		}else if ( m.group(1) != null && m.group(6) != null){
		    rendered = m.group(1) + m.group(4) + m.group(6);
		}else{
		    rendered = m.group(4);
		}
		m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));
	    }
	    m.appendTail(cleanText);

	} catch (Exception e) {
	    System.out.println(originalText);
	    e.printStackTrace();
	}
	return cleanText.toString();
    }


    /**
     * Detect commonsense entities with pipes, and transform them in normal entities.
     * 
     *  e.g. [[multinational corporation|multinational]] -> multinational
     * 
     * @param originalText
     * @return
     */
    public String removeCommonSenseWikilinks(String originalText){
	Pattern COMMONSENSE = Pattern.compile("\\[\\[+" + "([^\\]]*\\|)?" + "('')?" + "([a-z][^A-Z].*?)" + "('')?" + "\\]\\]+");
	Matcher m = COMMONSENSE.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();
	while(m.find()){
	    if (m.group(2) != null && m.group(4) != null){
		m.appendReplacement(cleanText, m.group(2) + m.group(3) + m.group(4));
	    }else{
		m.appendReplacement(cleanText, m.group(3));
	    }
	}
	m.appendTail(cleanText);
	return cleanText.toString();
    }

    /**
     * Harvest all the wiki-links from the original article and replace them with 
     * a specific SE-ORG tag. At the same time, it assigns the wikilinks to the article.
     * 
     * It:
     *  - solve the redirects
     *  - solve the blank-list entities
     *  
     * 	    [[Byzantine Empire|Byzantines]]  -->   SE-ORG<Byzantines>
     * 
     * @param originalText
     * @param article
     * @return
     */
    public String harvestAllWikilinks(String originalText, WikiArticle article) {
	Map<String, Set<String>> wikilinks = new HashMap<String, Set<String>>();
	/*
	 * We need to use some *strong* filtering here to avoid considering strange cases like
	 * the wiki links between ")" and "Mahavira" in the article: en.wikipedia.org/wiki/Gautama_Buddha 
	 * We adopt this heuristic: if the rendered entity has only special characters we skip it.
	 * We use the following regex to express special character that we do not want alone.
	 * We also skip wikiliks composed by only numbers.
	 */
	String specialCharacters = "^[" + "0-9/@#!-*\\$%^'&._+={}()" + "]+$" ;

	/*
	 * The following regex captures all the Wikilinks (after removed common-sense ones, see above)
	 * Named entities wikilinks can be sorrouned by '', which can stay inside square brackets or outside.
	 */
	Pattern ENTITY = Pattern.compile("('')?" + "\\[\\[+" + "([^\\]\\|]*\\|)?" + "('')?([^\\]]*?)('')?" + "\\]\\]+" + "('')?");
	Matcher m = ENTITY.matcher(originalText);
	StringBuffer cleanText = new StringBuffer();

	/*
	 * For each matching ...
	 */
	String wikid;
	String rendered;
	while(m.find()){
	    if (m.group(2) != null){
		wikid = m.group(2).replaceAll(" ", "_").substring(0, m.group(2).length()-1);
		if ( m.group(3) != null && m.group(5) != null){
		    rendered = m.group(3) + m.group(4) + m.group(5);
		}else if ( m.group(1) != null && m.group(6) != null){
		    rendered = m.group(1) + m.group(4) + m.group(6);
		}else{
		    rendered = m.group(4);
		}
	    }else{
		rendered = m.group(4);
		wikid = m.group(4).replaceAll(" ", "_");
	    }

	    /*
	     * eliminate blaklisted entities 
	     */
	    if (blacklist.contains(wikid)){
		m.appendReplacement(cleanText, Matcher.quoteReplacement(rendered));

	    }else{
		if (Configuration.solveRedirect())
		    wikid = Lector.getRedirectResolver().resolveRedirect(wikid);

		if (!rendered.matches(specialCharacters) && !rendered.isEmpty()){
		    if (!wikilinks.containsKey(rendered))
			wikilinks.put(rendered, new HashSet<String>());
		    wikilinks.get(rendered).add("SE-AUG<" + wikid +  ">");
		    m.appendReplacement(cleanText, Matcher.quoteReplacement("SE-ORG<" + wikid + ">"));
		}
	    }
	}
	m.appendTail(cleanText);
	article.addWikilinks(wikilinks);
	return cleanText.toString();
    }
}
