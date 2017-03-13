package it.uniroma3.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.components.RedirectResolver;
import it.uniroma3.configuration.Configuration;
import it.uniroma3.model.WikiArticle;

/**
 * 
 * 
 * 
 * 
 * @author matteo
 *
 */
public class MarkupParser {


    private static Set<String> commonsense_entities = new HashSet<String>(
	    Arrays.asList("United States dollar", "Euro", "Japanese yen", 
		    "Pound sterling", "Australian dollar", "Canadian dollar", 
		    "Swiss franc", "Chinese yuan", "Swedish krona", "Mexican peso",
		    "New Zealand dollar", "Singapore dollar", "Hong Kong dollar",
		    "Norwegian krone", "South Korean won", "Turkish lira", 
		    "Indian rupee", "Russian ruble", "Brazilian real", "South African rand"));

    /**
     * Detect all the wikilinks and normalize them in text.
     * E.g.:  ... artist SE-ORG<Britney_Spears> by SE-ORG<JIVE_Records>.
     * became: ... artist Britney Spears by JIVE Records.
     * 
     * @param text
     * @return
     */
    public static String removeAllWikilinks(String text) {
	Pattern WIKILINK = Pattern.compile("[A-Z-]+" + "<([^>]*?)>");
	Matcher m = WIKILINK.matcher(text);
	while(m.find()){
	    String renderedName = m.group(1).replaceAll("_", " ");
	    text = text.replaceAll(Pattern.quote(m.group(0)), Matcher.quoteReplacement(renderedName));
	}
	return text;
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
     * @param text
     * @return
     */
    public static String cleanEmptyTemplateWikilinks(String text){
	/*
	 * [[Mount_Rushmore#Heigth|]] --> Mount Rushmore Heigth
	 */
	Pattern TEMPLATEWIKILINK = Pattern.compile("\\[\\[+" + "([^\\]]+)\\|" + "\\]\\]+");
	Matcher m = TEMPLATEWIKILINK.matcher(text);
	while(m.find()){
	    text = m.replaceAll(m.group(1).replaceAll("_", " ").replaceAll("#", " "));
	}
	return text;
    }

    /**
     * Detect commonsense entities with pipes, and transform them
     * in normal entities e.g. [[multinational corporation|multinational]] -> multinational
     * 
     * @param text
     * @return
     */
    public static String removeCommonSenseWikilinks(String text){
	Pattern COMMONSENSE = Pattern.compile("\\[\\[+" + "([^\\]]*\\|)?" + "('')?" + "([a-z][^A-Z].*?)" + "('')?" + "\\]\\]+");
	Matcher m = COMMONSENSE.matcher(text);
	while(m.find()){
	    if ( m.group(2) != null && m.group(4) != null){
		text = text.replace(m.group(0), m.group(2) + m.group(3) + m.group(4));
	    }else{
		text = text.replace(m.group(0), m.group(3));
	    }
	}
	return text;
    }

    /**
     * For example:
     * 
     * [[Byzantine Empire|Byzantines]]
     * 
     * we have "Byzantines" in the text that refers to the wikid "Byzantine_Empire".
     * 
     * 
     * @param s
     * @return
     */
    public static String harvestAllWikilinks(String text, WikiArticle article) {

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
	Matcher m = ENTITY.matcher(text);

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
	     * last moment to eliminate commonsese entities 
	     * (essentially the ones that are not lower cased strings)
	     */
	    if (commonsense_entities.contains(wikid)){
		text = text.replace(Pattern.quote(m.group(0)), Matcher.quoteReplacement(rendered));
	   
	    }else{

		/*
		 * 
		 */
		if (Configuration.solveRedirect())
		    wikid = RedirectResolver.getTargetPage(wikid);

		/*
		 * we can have an empty rendered in case of presence of template (the we eliminate previously).
		 * Indeed, a wikilink such as [[Dipavamsa|{{IAST|Dīpavaṃsa}}]] would become [[Dipavamsa|]]
		 * with an empty rendered name.
		 */
		if (!rendered.matches(specialCharacters) && !rendered.isEmpty()){
		    if (!wikilinks.containsKey(rendered))
			wikilinks.put(rendered, new HashSet<String>());
		    wikilinks.get(rendered).add("SE-AUG<" + wikid +  ">");
		    /*
		     * 
		     * SOSTITUISCI CON addREPLACEMENT!!
		     * 
		     */
		    text = text.replaceAll(Pattern.quote(m.group(0)), Matcher.quoteReplacement("SE-ORG<" + wikid + ">"));
		}
	    }
	}
	article.addWikilinks(wikilinks);
	return text;
    }

    /**
     * 
     * @param cleanText
     * @return
     */
    public static String cleanAllWikilinks(String cleanText) {
	Pattern ENTITY = Pattern.compile("('')?" + "\\[\\[+" + "([^\\]\\|]*\\|)?" + "('')?([^\\]]*?)('')?" + "\\]\\]+" + "('')?");
	Matcher m = ENTITY.matcher(cleanText);

	StringBuffer cleanBlock = new StringBuffer();
	try {
	    /*
	     * For each matching ...
	     */
	    String rendered;
	    cleanBlock = new StringBuffer();
	    while(m.find()){
		if (m.group(2) != null){
		    if ( m.group(3) != null && m.group(5) != null){
			rendered = m.group(3) + m.group(4) + m.group(5);
		    }else if ( m.group(1) != null && m.group(6) != null){
			rendered = m.group(1) + m.group(4) + m.group(6);
		    }else{
			rendered = m.group(4);
		    }
		}else{
		    rendered = m.group(4);
		}
		m.appendReplacement(cleanBlock, Matcher.quoteReplacement(rendered));
	    }
	    m.appendTail(cleanBlock);

	} catch (Exception e) {
	    System.out.println(cleanText);
	    e.printStackTrace();
	}

	return cleanBlock.toString();
    }



}
