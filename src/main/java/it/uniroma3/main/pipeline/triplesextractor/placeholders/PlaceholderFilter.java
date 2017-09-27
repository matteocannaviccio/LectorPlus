package it.uniroma3.main.pipeline.triplesextractor.placeholders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.util.inout.TSVReader;
/**
 * 
 * @author matteo
 *
 */
public abstract class PlaceholderFilter {

    //ordine di applicazione, il primo in lista e' il primo applicato
    protected Map<String, List<Pattern>> placeholder2patterns;
    protected Set<String> conjunctions;


    /**
     * The map keep the patterns (filled by each language)
     * and the specific order of replacement.
     */
    public PlaceholderFilter() {
	placeholder2patterns = new LinkedHashMap<>();
	placeholder2patterns.put("#YEAR#", fillYears());
	placeholder2patterns.put("#ERA#", fillEras());
	placeholder2patterns.put("#MONTH#", fillMonths());
	placeholder2patterns.put("#DAY#", fillDays());
	placeholder2patterns.put("#DATE#", fillDates());
	placeholder2patterns.put("#POS#", fillPositions());
	placeholder2patterns.put("#NAT#", fillNationalities());
	placeholder2patterns.put("#LEN#", fillLengths());
	placeholder2patterns.put("#ORD#", fillOrdinals());
	placeholder2patterns.put("#SCORE#", fillScore());
	placeholder2patterns.put("#NUM#", fillNumbers());
	conjunctions = fillConjuctions();
    }

    /**
     * 
     * @param lang
     * @return
     */
    public static PlaceholderFilter getPlaceholderFilter() {
	switch (Lector.getWikiLang().getLang()){
	case en:
	    return new PlaceholderFilterEnglish();
	case es:
	    return new PlaceholderFilterSpanish();
	case fr:
	    return new PlaceholderFilterFrench();
	case de:
	    return new PlaceholderFilterGerman();
	case it:
	    return new PlaceholderFilterItalian();
	default:
	    return new PlaceholderFilterEnglish();
	}
    }


    /**
     * Eliminate parenthesis and put the whole phrase in lowercase.
     *
     * @param phrase
     * @return
     */
    public String preProcess(String phrase) {
	phrase = Lector.getTextParser().removeParenthesis(phrase); // removes parenthesis only when they are balanced
	phrase = phrase.replaceAll("\t", " ").replaceAll("\n", " ");
	phrase = phrase.toLowerCase();
	return phrase;
    }


    /**
     * Applies the reg-ex following the order expressed in the map
     * declared during the construction. 
     *
     * @param phrase
     * @return
     */
    public String replace(String phrase) {
	phrase = preProcess(phrase);

	for (Map.Entry<String, List<Pattern>> placeholderAndPatterns : this.placeholder2patterns.entrySet()){
	    List<Pattern> placeholderRegexps = placeholderAndPatterns.getValue();
	    // keeps the order
	    for (int i = 0; i < placeholderRegexps.size(); i++) {
		phrase = placeholderRegexps
			.get(i) // keeps the order
			.matcher(phrase)
			.replaceAll(placeholderAndPatterns.getKey());
	    }
	}

	phrase = postProcess(phrase);
	return phrase;
    }

    /**
     * This is not customized for the specific language.
     * (we read possible nationalities from an external source)
     * 
     * @return
     */
    public List<Pattern> fillNationalities() {
	Set<String> nationalities = TSVReader.getLines2Set(Configuration.getNationalitiesList());
	List<Pattern> natPat = new ArrayList<>();
	for (String nat : nationalities) {
	    Pattern NAT = Pattern.compile("\\b" + nat + "\\b", Pattern.CASE_INSENSITIVE);
	    natPat.add(NAT);
	}
	return natPat;
    }

    /**
     * This is not customized for the specific language.
     * 
     * @return
     */
    public List<Pattern> fillYears() {
	return Arrays.asList(
		Pattern.compile("\\b((1|2)\\d\\d\\d)-\\d\\d\\b"),
		Pattern.compile("\\b((1|2)\\d\\d\\d)\\b")
		);
    }


    /**
     * This is not customized for the specific language.
     *
     * @return
     */
    public List<Pattern> fillEras() {
	return Arrays.asList(
		Pattern.compile("\\b((1|2)\\d\\d\\d)s\\b"),
		Pattern.compile("\\b#[0-9]0s\\b")
		);
    }

    /**
     * This is not customized for the specific language.
     * 
     * @return
     */
    public List<Pattern> fillDays() {
	return Arrays.asList(
		Pattern.compile("([0-3]?[0-9]–)?[0-3]?[0-9]\\s#MONTH#"),
		Pattern.compile("#MONTH#\\s([0-3]?[0-9]–)?[0-3]?[0-9]")
		);
    }

    /**
     * This is not customized for the specific language.
     *
     * @return
     */
    public List<Pattern> fillDates() {
	return Arrays.asList(
		Pattern.compile("#YEAR#(\\s|,\\s)#DAY#"),
		Pattern.compile("#DAY#(\\s|,\\s)#YEAR#"),
		Pattern.compile("#DAY#(\\s|,\\s)#YEAR#"),
		Pattern.compile("#YEAR#(\\s|,\\s)#DAY#"),
		Pattern.compile("#DAY#(\\s|,\\s)#MONTH#"),
		Pattern.compile("#MONTH#(\\s|,\\s)#DAY#"),
		Pattern.compile("#MONTH#(\\s|,\\s)#YEAR#"),
		Pattern.compile("#YEAR#(\\s|,\\s)#MONTH#")
		);
    }
    
    /**
     * This is not customized for the specific language.
     *
     * @return
     */
    public List<Pattern> fillScore() {
	return Arrays.asList(
		Pattern.compile("\\b\\d(–|-)\\d\\b")
		);
    }

    /**
     * This is not customized for the specific language.
     * 
     * https://regex101.com/r/vLXx85/1
     *
     * @return
     */
    public List<Pattern> fillNumbers() {
	return Arrays.asList(
		Pattern.compile("\\b\\d+(\\.|,)?\\d+(\\.|,)?\\d*\\b")
		);
    }

    /**
     * 
     * @param phrase
     * @return
     */
    public String postProcess(String phrase) {

	phrase = phrase.replaceAll("''", "");
	phrase = phrase.replaceAll("\"", "");
	phrase = phrase.replaceAll("<<", "");
	phrase = phrase.replaceAll(">>", "");

	/* remove possible special characters at the beginning or at the end.
	 * for example, we want to remove phrases that begin with ", ', -
	 * but not with 's. For this reason we check the space after the character.
	 */
	if (phrase.startsWith("' ") || phrase.startsWith("\" ") || phrase.startsWith("- ") || phrase.startsWith(": ") )
	    phrase = phrase.substring(1).trim();
	if (phrase.endsWith(" '") || phrase.endsWith(" \"") || phrase.endsWith(" -") || phrase.endsWith(" :"))
	    phrase = phrase.substring(0, phrase.length() - 1).trim();
	
	// remove noisy phrases
	if (phrase.contains("|") ||
		phrase.contains("│") || 
		phrase.contains("{") || 
		phrase.contains("}") || 
		phrase.contains(";") || 
		phrase.contains("(") ||
		phrase.contains(":") || 
		phrase.contains(")"))
	    phrase = "SPECIALCHAR";

	/*
	 * Some nationalities are cutted, e.g. [Canad]ian or [French]ese
	 */
	String[] initialNatCutted = new String[]{"n ", "ese ", "ian "};
	for (String inc : initialNatCutted) {
	    if (phrase.startsWith(inc))
		phrase = phrase.substring(inc.length());
	}
	
	if (conjunctions.contains(phrase))
	    phrase = "CONJUNCTION";
	
	return phrase.trim();    
    }
    
    protected abstract List<Pattern> fillPositions();
    protected abstract List<Pattern> fillLengths();
    protected abstract List<Pattern> fillMonths();
    protected abstract List<Pattern> fillOrdinals();
    protected abstract Set<String> fillConjuctions();
    
    
    public static void main(String[] args){
	String text = "è un comune Italiano di 611 abitanti della provincia di Catania nato nel 1921 Marzo.";
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "it");
	
	Lector.init("");
	System.out.println(PlaceholderFilterEnglish.getPlaceholderFilter().replace(text));
    }

}
