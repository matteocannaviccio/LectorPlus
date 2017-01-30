package it.uniroma3.lectorplus;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
/**
 * 
 * 
 * 
 * @author matteo
 *
 */
public class WikiClean {

    public enum WikiLanguage {EN, DE, ZH};

    private boolean withTitle;
    private boolean withFooter;
    private WikiLanguage lang;
    private int ALIAS_SPAN;

    // Use the builder to construct.
    protected WikiClean() {
    }

    protected void setWithTitle(boolean flag) {
	this.withTitle = flag;
    }

    public boolean getWithTitle() {
	return withTitle;
    }

    protected void setWithFooter(boolean flag) {
	this.withFooter = flag;
    }

    public boolean getWithFooter() {
	return withFooter;
    }

    protected void setLanguage(WikiLanguage lang) {
	this.lang = lang;
    }

    public WikiLanguage getLanguage() {
	return this.lang;
    }

    /*
     * GET TITLE OF THE PAGE 
     * 
     */
    private static final String XML_START_TAG_TITLE = "<title>";
    private static final String XML_END_TAG_TITLE = "</title>";
    public final String getTitle(String s) {
	int start = s.indexOf(XML_START_TAG_TITLE);
	int end = s.indexOf(XML_END_TAG_TITLE, start);
	if (start < 0 || end < 0) {
	    return "";
	}
	return StringEscapeUtils.unescapeHtml4(s.substring(start + 7, end));
    }

    /*
     * GET ID OF THE PAGE 
     * 
     */
    private static final String XML_START_TAG_ID = "<id>";
    private static final String XML_END_TAG_ID = "</id>";
    public final String getId(String s) {
	// parse out the document id
	int start = s.indexOf(XML_START_TAG_ID);
	int end = s.indexOf(XML_END_TAG_ID);
	return (start == -1 || end == -1 || start > end) ? "0" : s.substring(start + 4, end);
    }

    /*
     * GET TEXT OF THE PAGE 
     * 
     */
    private static final String XML_START_TAG_TEXT = "<text xml:space=\"preserve\"";
    private static final String XML_END_TAG_TEXT = "</text>";
    public String getWikiMarkup(String s) {
	// parse out actual text of article
	int textStart = s.indexOf(XML_START_TAG_TEXT);
	int textEnd = s.indexOf(XML_END_TAG_TEXT, textStart);
	if (textStart == -1 || textStart + 27 > textEnd) {
	    // Returning empty string is preferable to returning null to prevent NPE.
	    return "";
	}
	return s.substring(textStart + 27, textEnd);
    }

    /**
     * 
     * @param page
     * @return
     */
    public WikiArticle retrieveArticle(String page){
	String wikid = getTitle(page).replaceAll(" ", "_");
	String id = getId(page);
	String content = cleanContent(page);
	if (!content.startsWith("#REDIRECT") && !content.startsWith("#redirect")){
	    WikiArticle article = new WikiArticle(wikid, id, content);
	    if (content.length() >= ALIAS_SPAN)
		article.setAliases(getAlias(content.substring(0, ALIAS_SPAN)));
	    else
		article.setAliases(getAlias(content));
	    article.setContent(removeEmphasis(content));
	    return article;
	}else{
	    return null;
	}
    }

    /**
     * 
     * @param page
     * @return
     */
    private String cleanContent(String page) {
	String content = getWikiMarkup(page);
	if (!withFooter) {
	    content = removeFooter(content);
	}
	content = removeRefs(content);
	content = removeInterWikiLinks(content);
	content = removeParentheticals(content);
	content = fixUnitConversion(content);
	content = ImageCaptionsRemover.remove(content);
	content = DoubleBracesRemover.remove(content);
	content = removeHtmlComments(content);
	content = removeHeadings(content);
	content = removeCategoryLinks(content);
	//content = removeLinks(content);
	content = removeMath(content);
	content = removeGallery(content);
	content = removeNoToc(content);
	content = removeIndentation(content);
	content = TableRemover.remove(content);

	// For some reason, some HTML entities are doubly encoded.
	content = StringEscapeUtils.unescapeHtml4(StringEscapeUtils.unescapeHtml4(content));
	content = removeHtmlTags(content);

	// Finally, fold multiple newlines.
	content = compressMultipleNewlines(content);

	return content.trim();
    }

    /*
     * FIX UNIT CONVERSION 
     * 
     */
    private static final Pattern UNIT_CONVERSION1 = Pattern
	    .compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\}\\}");
    private static final Pattern UNIT_CONVERSION2 = Pattern
	    .compile("\\{\\{convert\\|(\\d+)\\|([^|]+)\\|[^}]+\\}\\}");
    protected String fixUnitConversion(String s) {
	String t = UNIT_CONVERSION1.matcher(s).replaceAll("$1 $2");
	return UNIT_CONVERSION2.matcher(t).replaceAll("$1 $2");
    }

    /*
     * REMOVE HTML TAGS 
     * 
     */
    private static final Pattern HTML_TAGS = Pattern.compile("<[^>]+>");
    protected String removeHtmlTags(String s) {
	return HTML_TAGS.matcher(s).replaceAll("");
    }

    /*
     * REMOVE GALLERY 
     * 
     */
    private static final Pattern GALLERY = Pattern.compile("&lt;gallery&gt;.*?&lt;/gallery&gt;",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    protected String removeGallery(String s) {
	return GALLERY.matcher(s).replaceAll("");
    }

    /*
     * REMOVE NOTOC 
     * 
     */
    private static final Pattern NO_TOC = Pattern.compile("__NOTOC__");
    protected String removeNoToc(String s) {
	return NO_TOC.matcher(s).replaceAll("");
    }

    /*
     * REMOVE INDENTATION 
     * 
     */
    private static final Pattern INDENTATION = Pattern.compile("[\\n\\r]:\\s*");
    protected String removeIndentation(String s) {
	return INDENTATION.matcher(s).replaceAll("\n");
    }

    /*
     * REMOVE MATH
     * 
     */
    private static final Pattern MATH = Pattern.compile("&lt;math&gt;.*?&lt;/math&gt;",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    protected String removeMath(String s) {
	return MATH.matcher(s).replaceAll("");
    }


    /*
     * REMOVE IPA parenthetical --> '''Albedo''' ({{IPAc-en|icon|æ|l|ˈ|b|iː|d|oʊ}})
     * IPA parenthetical may be enclosed either with parentheses or brackets (de articles).
     */
    private static final Pattern IPA1 = Pattern.compile(" (\\(|\\[)\\{\\{IPA[^\\}]+\\}\\}(\\)|\\])");
    private static final Pattern IPA2 = Pattern.compile(" \\{\\{IPA[^\\}]+\\}\\}");
    protected String removeParentheticals(String s) {
	// Take care of things like: id 36
	// '''Albedo''' ({{IPAc-en|icon|æ|l|ˈ|b|iː|d|oʊ}}), or ''reflection coefficient'' ...
	//
	// Note that we shouldn't just leave to the double-curly remover, since that would leave
	// the dangling empty parens.
	s = IPA1.matcher(s).replaceAll("");
	// Straight-up IPA, with no parenthetical.
	s = IPA2.matcher(s).replaceAll("");
	return s;
    }

    /*
     * REMOVE Multiple LINES 
     * 
     */
    private static final Pattern MULTIPLE_NEWLINES = Pattern.compile("[\\n\\r][\\n\\r]+");
    protected String compressMultipleNewlines(String s) {
	return MULTIPLE_NEWLINES.matcher(s).replaceAll("\n\n");
    }

    /*
     * REMOVE FOOTER english 
     * 
     */
    private static final Pattern FOOTER_EN1 = Pattern.compile("==\\s*See also\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_EN2 = Pattern.compile("==\\s*References\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_EN3 = Pattern.compile("==\\s*Further reading\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_EN4 = Pattern.compile("==\\s*External Links\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_EN5 = Pattern.compile("==\\s*Related pages\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /*
     * REMOVE FOOTER deutschland 
     * 
     */
    private static final Pattern FOOTER_DE1 = Pattern.compile("==\\s*Referenzen\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_DE2 = Pattern.compile("==\\s*Weblinks\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_DE3 = Pattern.compile("==\\s*Literatur\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_DE4 = Pattern.compile("==\\s*Einzelnachweise\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_DE5 = Pattern.compile("==\\s*Siehe auch\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_DE6 = Pattern.compile("==\\s*Quellen\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /*
     * REMOVE FOOTER zh 
     * 
     */
    private static final Pattern FOOTER_ZH1 = Pattern.compile("==\\s*参见\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_ZH2 = Pattern.compile("==\\s*参考书目\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern FOOTER_ZH3 = Pattern.compile("==\\s*参考网址\\s*==.*",
	    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /*--------------
     * REMOVE FOOTER 
     * -------------
     */
    protected String removeFooter(String s) {
	if (lang.equals(WikiLanguage.EN)) {
	    s = FOOTER_EN1.matcher(s).replaceAll("");
	    s = FOOTER_EN2.matcher(s).replaceAll("");
	    s = FOOTER_EN3.matcher(s).replaceAll("");
	    s = FOOTER_EN4.matcher(s).replaceAll("");
	    s = FOOTER_EN5.matcher(s).replaceAll("");
	} else if (lang.equals(WikiLanguage.DE)) {
	    s = FOOTER_DE1.matcher(s).replaceAll("");
	    s = FOOTER_DE2.matcher(s).replaceAll("");
	    s = FOOTER_DE3.matcher(s).replaceAll("");
	    s = FOOTER_DE4.matcher(s).replaceAll("");
	    s = FOOTER_DE5.matcher(s).replaceAll("");
	    s = FOOTER_DE6.matcher(s).replaceAll("");
	} else if (lang.equals(WikiLanguage.ZH)) {
	    s = FOOTER_ZH1.matcher(s).replaceAll("");
	    s = FOOTER_ZH2.matcher(s).replaceAll("");
	    s = FOOTER_ZH3.matcher(s).replaceAll("");
	}
	return s;
    }

    /*
     * REMOVE CATEGORIES
     * 
     */
    private static final Pattern CATEGORY_LINKS_EN = Pattern
	    .compile("\\[\\[Category:([^\\]]+)\\]\\]");
    private static final Pattern CATEGORY_LINKS_DE = Pattern
	    .compile("\\[\\[Kategorie:([^\\]]+)\\]\\]");
    
    protected String removeCategoryLinks(String s) {
	if (lang.equals(WikiLanguage.EN))
	    return CATEGORY_LINKS_EN.matcher(s).replaceAll("");
	if (lang.equals(WikiLanguage.DE))
	    return CATEGORY_LINKS_DE.matcher(s).replaceAll("");
	if(lang.equals(WikiLanguage.ZH))
	    return CATEGORY_LINKS_EN.matcher(s).replaceAll(""); //ZH use the same category tag as EN
	return s;
    }

    /*
     * REMOVE LINKS
     * 
     */
    private static final Pattern LINKS1 = Pattern.compile("\\[\\[[^\\]]+\\|([^\\]]+)\\]\\]");
    private static final Pattern LINKS2 = Pattern.compile("(\\[\\[|\\]\\])");
    protected String removeLinks(String s) {
	return LINKS2.matcher(LINKS1.matcher(s).replaceAll("$1")).replaceAll("");
    }

    /*
     * REMOVE HEADINGS
     * 
     */
    private static final Pattern HEADINGS = Pattern.compile("=+\\s?(.*?)=+");
    protected String removeHeadings(String s) {
	// Make sure there's an extra newline after headings.
	return HEADINGS.matcher(s).replaceAll("");
    }

    /*
     * GET ALIASES
     * 
     */
    private static final Pattern ALIASES = Pattern.compile("('''|''''')(.*?)('''|''''')");
    protected List<String> getAlias(String s) {
	List<String> aliases = new LinkedList<String>();
	Matcher m = ALIASES.matcher(s);
	while(m.find()){
	    String alias = m.group(2).replaceAll("(\\[|\\]|'')*", "").trim();
	    if (!alias.isEmpty())
		aliases.add(alias);
	}
	return aliases;
    }

    /*
     * REMOVE EMPHASIS
     * this method removes all the emphasis skipping the first sentence (i.e. 200 characters).
     */
    private static final Pattern EMPHASIS = Pattern.compile("('''|'')");
    protected String removeEmphasis(String s) {
	return EMPHASIS.matcher(s).replaceAll("");
    }

    /*
     * REMOVE HTML_COMMENT
     * 
     */
    private static final Pattern HTML_COMMENT = Pattern.compile(
	    "(<|&lt;|&#60;)!--.*?--(>|&gt;|&#62;)", Pattern.DOTALL);
    protected String removeHtmlComments(String s) {
	return HTML_COMMENT.matcher(s).replaceAll("");
    }

    /*
     * REMOVE REFS
     * 
     */
    private static final Pattern BR = Pattern.compile("&lt;br */&gt;");
    private static final Pattern REF1 = Pattern.compile("&lt;ref[^/]+/&gt;", Pattern.DOTALL);
    private static final Pattern REF2 = Pattern.compile("&lt;ref.*?&lt;/ref&gt;", Pattern.DOTALL);
    protected String removeRefs(String s) {
	s = BR.matcher(s).replaceAll(""); // See test case for why we do this.
	s = REF1.matcher(s).replaceAll("");
	s = REF2.matcher(s).replaceAll("");
	return s;
    }

    /*
     * REMOVE INTER_WIKI_LINKS
     * Note that WiktionaryLinks have the form [[wikt:anarchism|anarchism]], which is easily confused
     * with inter-wikilinks. The distinguishing characteristic is the lack of pipe (|).
     */
    private static final Pattern INTER_WIKI_LINKS = Pattern.compile("\\[\\[[a-z\\-]+:[^|\\]]+\\]\\]");
    protected String removeInterWikiLinks(String s) {
	return INTER_WIKI_LINKS.matcher(s).replaceAll(" ");
    }

    /*
     * REMOVE IMAGE CAPTIONS
     * 
     */
    protected static final class ImageCaptionsRemover {
	private static final int DEFAULT_NO_BRACKET = 0;
	private static final int STATE_1CLOSE_BRACKET = 1;
	private static final int STATE_1OPEN_BRACKET = 2;
	protected static String remove(String s) {
	    String[] labels = { "[[File:", "[[Image:", "[[Datei" // We see this in de wikipedia.
	    };
	    for (String label : labels) {
		s = removeLabel(s, label);
	    }
	    return s;
	}

	// This method encodes a finite state machine to handle links in caption, which result in
	// nested [[ ... [[foo]] ... ]] constructs.
	protected static String removeLabel(String s, String label) {
	    int i = s.indexOf(label);
	    while (i != -1) {
		int state = DEFAULT_NO_BRACKET;
		int level = 1;
		int cur = i + label.length();

		while (cur < s.length()) {
		    if (state == STATE_1OPEN_BRACKET && s.charAt(cur) == '[') {
			level++;
			state = DEFAULT_NO_BRACKET;
		    }
		    // If there's only one close, move back to default state.
		    if (state == STATE_1OPEN_BRACKET) {
			state = DEFAULT_NO_BRACKET;
		    }
		    if (s.charAt(cur) == '[') {
			state = STATE_1OPEN_BRACKET;
		    }

		    if (state == STATE_1CLOSE_BRACKET && s.charAt(cur) == ']') {
			level--;
			if (level == 0) {
			    break;
			}
			state = DEFAULT_NO_BRACKET;
		    } else {
			// If there's only one close, move back to default state.
			if (state == STATE_1CLOSE_BRACKET) {
			    state = DEFAULT_NO_BRACKET;
			}
			if (s.charAt(cur) == ']') {
			    state = STATE_1CLOSE_BRACKET;
			}
		    }
		    cur++;
		}
		if (cur == s.length()) {
		    return s.substring(0, i);
		}
		s = s.substring(0, i) + s.substring(cur + 1, s.length());
		i = s.indexOf(label, i);
	    }
	    return s;
	}
    }

    protected static final class DoubleBracesRemover {
	private static final int DEFAULT_NO_BRACE = 0;
	private static final int STATE_1CLOSE_BRACE = 1;
	private static final int STATE_1OPEN_BRACE = 2;

	// This method encodes a finite state machine to handle nested double braces (e.g., in
	// infoboxes).
	protected static String remove(String s) {
	    int i = s.indexOf("{{");
	    while (i != -1) {
		int state = DEFAULT_NO_BRACE;
		int level = 1;
		int cur = i + 2;

		while (cur < s.length()) {
		    if (state == STATE_1OPEN_BRACE && s.charAt(cur) == '{') {
			level++;
			state = DEFAULT_NO_BRACE;
		    }
		    // If there's only one close, move back to default state.
		    if (state == STATE_1OPEN_BRACE) {
			state = DEFAULT_NO_BRACE;
		    }
		    if (s.charAt(cur) == '{') {
			state = STATE_1OPEN_BRACE;
		    }

		    if (state == STATE_1CLOSE_BRACE && s.charAt(cur) == '}') {
			level--;
			if (level == 0) {
			    break;
			}
			state = DEFAULT_NO_BRACE;
		    } else {
			// If there's only one close, move back to default state.
			if (state == STATE_1CLOSE_BRACE) {
			    state = DEFAULT_NO_BRACE;
			}
			if (s.charAt(cur) == '}') {
			    state = STATE_1CLOSE_BRACE;
			}
		    }
		    cur++;
		}

		if (cur == s.length()) {
		    return s.substring(0, i);
		}

		s = s.substring(0, i) + s.substring(cur + 1, s.length());
		i = s.indexOf("{{", i);
	    }

	    return s;
	}
    }

    protected static final class TableRemover {
	private static final int DEFAULT = 0;
	private static final int STATE_PIPE = 1;
	private static final int STATE_1OPEN_BRACE = 2;

	protected static String remove(String s) {
	    int i = s.indexOf("{|");
	    while (i != -1) {
		int state = DEFAULT;
		int level = 1;
		int cur = i + 2;

		while (cur < s.length()) {
		    if (state == STATE_1OPEN_BRACE && s.charAt(cur) == '|') {
			level++;
			state = DEFAULT;
		    }
		    // If there's only one close, move back to default state.
		    if (state == STATE_1OPEN_BRACE) {
			state = DEFAULT;
		    }
		    if (s.charAt(cur) == '{') {
			state = STATE_1OPEN_BRACE;
		    }

		    if (state == STATE_PIPE && s.charAt(cur) == '}') {
			level--;
			if (level == 0) {
			    break;
			}
			state = DEFAULT;
		    } else {
			// If there's a pipe but no close brace, move back to default state.
			if (state == STATE_PIPE) {
			    state = DEFAULT;
			}
			if (s.charAt(cur) == '|') {
			    state = STATE_PIPE;
			}
		    }
		    cur++;
		}

		if (cur == s.length()) {
		    return s.substring(0, i);
		}

		s = s.substring(0, i) + s.substring(cur + 1, s.length());
		i = s.indexOf("{|", i);
	    }

	    return s;
	}
    }

    /**
     * @param aLIAS_SPAN the aLIAS_SPAN to set
     */
    protected void setALIAS_SPAN(int span) {
        ALIAS_SPAN = span;
    }
}
