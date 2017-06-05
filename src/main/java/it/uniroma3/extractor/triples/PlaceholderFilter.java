package it.uniroma3.extractor.triples;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.configuration.Lector;
import it.uniroma3.extractor.util.reader.TSVReader;
/**
 * 
 * @author matteo
 *
 */
public class PlaceholderFilter {

    private static Set<String> nationalities;

    /*
    private static Set<String> badPhrases = new HashSet<String>(Arrays.asList(new String[]{
	    "and", ", and", ",and" , "' and '", "' and", ", and a", ", and in", ", and then", 
	    ", and other", "and the", ",and the", ", and the", "n and", "s and", "'s and", "s, and"
	    , ", the", "or", ", or"}));
     */

    /**
     * 
     */
    public PlaceholderFilter(){
	nationalities = TSVReader.getLines2Set(Configuration.getNationalitiesList());
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceNationalities(String phrase){
	for (String nat : nationalities){
	    nat = nat.replaceAll("_", " ");
	    Pattern NAT = Pattern.compile("\\b"+nat+"\\b", Pattern.CASE_INSENSITIVE);
	    phrase = NAT.matcher(phrase).replaceAll("#NAT#");
	}
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replacePosition(String phrase){
	Pattern POS1 = Pattern.compile("\\b("
		+ "south(ern)?(-)?west(ern)?|"
		+ "south(ern)?(-)?east(ern)?|"
		+ "north(ern)?(-)?east(ern)?|"
		+ "north(ern)?(-)?west(ern)?|"
		+ "south(ern)?(-)?central|"
		+ "north(ern)?(-)?central|"
		+ "west(ern)?(-)?central|"
		+ "south(ern)?(-)?central|"
		+ "central(-)?north(ern)?|"
		+ "central(-)?south(ern)?|"
		+ "central(-)?east(ern)?|"
		+ "central(-)?west(ern)?"
		+ ")\\b", Pattern.CASE_INSENSITIVE);
	Pattern POS2 = Pattern.compile("\\b("
		+ "northern|"
		+ "southern|"
		+ "western|"
		+ "eastern"
		+ ")\\b", Pattern.CASE_INSENSITIVE);
	Pattern POS3 = Pattern.compile("\\b("
		+ "north|"
		+ "south|"
		+ "west|"
		+ "east)\\b", Pattern.CASE_INSENSITIVE);
	phrase = POS1.matcher(phrase).replaceAll("#POS#");
	phrase = POS2.matcher(phrase).replaceAll("#POS#");
	phrase = POS3.matcher(phrase).replaceAll("#POS#");
	return phrase;
    }

    /**
     * \b(\d+(\s\d+)*)\s?(km|kilometer|mi|ft|yd|m)(s?)\b
     * @param phrase
     * @return
     */
    private String replaceLength(String phrase){
	Pattern LEN1 = Pattern.compile("#YEAR#\\s?(km|kilometer(s)?|mi|ft|yd|m)(s?)\\b");
	Pattern LEN2 = Pattern.compile("\\d+(\\s\\d+)*\\s?(km|kilometer(s)?|mi|ft|yd|m)(s?)\\b");
	phrase = LEN1.matcher(phrase).replaceAll("#LEN#");
	phrase = LEN2.matcher(phrase).replaceAll("#LEN#");
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceDate(String phrase){
	Pattern DATE3 = Pattern.compile("#YEAR#(\\s|,\\s)#DAY#");
	Pattern DATE4 = Pattern.compile("#DAY#(\\s|,\\s)#YEAR#");
	phrase = DATE3.matcher(phrase).replaceAll("#DATE#");
	phrase = DATE4.matcher(phrase).replaceAll("#DATE#");
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceDay(String phrase){
	Pattern DATE1 = Pattern.compile("([0-3]?[0-9]–)?[0-3]?[0-9]\\s#MONTH#");
	Pattern DATE2 = Pattern.compile("#MONTH#\\s([0-3]?[0-9]–)?[0-3]?[0-9]");
	phrase = DATE1.matcher(phrase).replaceAll("#DAY#");
	phrase = DATE2.matcher(phrase).replaceAll("#DAY#");
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceMonths(String phrase){
	Pattern MONTH = Pattern.compile("\\b(january|february|march|april|may|june|july|august|"
		+ "september|october|november|december)\\b", Pattern.CASE_INSENSITIVE);
	phrase = MONTH.matcher(phrase).replaceAll("#MONTH#");
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceYears(String phrase){
	Pattern YEAR = Pattern.compile("\\b((1|2)\\d\\d\\d)\\b");
	phrase = YEAR.matcher(phrase).replaceAll("#YEAR#");
	Pattern ERA1 = Pattern.compile("\\b#YEAR#s\\b");
	Pattern ERA2 = Pattern.compile("\\b#[0-9]0s\\b");
	phrase = ERA1.matcher(phrase).replaceAll("#ERA#");
	phrase = ERA2.matcher(phrase).replaceAll("#ERA#");
	return phrase;
    }

    /**
     * 
     * @param phrase
     * @return
     */
    private String replaceOrdinals(String phrase){
	Pattern ORD1 = Pattern.compile("\\b\\d*1st\\b");
	Pattern ORD2 = Pattern.compile("\\b\\d*2nd\\b");
	Pattern ORD3 = Pattern.compile("\\b\\d*3rd\\b");
	Pattern ORD4 = Pattern.compile("\\b(\\d)*\\dth\\b");
	Pattern ORD5 = Pattern.compile("\\b(first|second|third|fourth|fifth)\\b");
	phrase = ORD1.matcher(phrase).replaceAll("#ORD#");
	phrase = ORD2.matcher(phrase).replaceAll("#ORD#");
	phrase = ORD3.matcher(phrase).replaceAll("#ORD#");
	phrase = ORD4.matcher(phrase).replaceAll("#ORD#");
	phrase = ORD5.matcher(phrase).replaceAll("#ORD#");
	return phrase;
    }

    /**
     * Eliminate parethesis.
     * 
     * @param sentence
     * @return
     */
    public String preprocess(String phrase){
	phrase = Lector.getTextParser().removeParenthesis(phrase);
	phrase = phrase.toLowerCase();
	phrase = replaceOrdinals(replaceLength(replaceNationalities(replacePosition(replaceDate(replaceDay(replaceMonths(replaceYears(phrase))))))));
	phrase = phrase.replaceAll("''", "");
	phrase = phrase.replaceAll("\"", "");

	/* remove possible special characters at the beginning or at the end.
	 * for example, we want to remove phrases that begin with ", ', -
	 * but not with 's. For this reason we check the space after the character.
	 */
	if (phrase.startsWith("' ") || phrase.startsWith("\" ") || phrase.startsWith("- ") || phrase.startsWith(": "))
	    phrase = phrase.substring(1).trim();
	if (phrase.endsWith(" '") || phrase.endsWith(" \"") || phrase.endsWith(" -") || phrase.endsWith(" :"))
	    phrase = phrase.substring(0, phrase.length()-1).trim();

	Pattern pattern = Pattern.compile("([A-Za-z0-9,'´#\\.\\- ]+)");
	Matcher matcher = pattern.matcher(phrase);
	if(!matcher.matches())
	    phrase = "";
	else{
	    pattern = Pattern.compile("([,'´#\\.\\- ]+)");
	    matcher = pattern.matcher(phrase);
	    if(matcher.matches())
		phrase = "";
	}

	/*
	 * Some nationalities are cutted, e.g. [Canad]ian or [French]ese
	 */
	String[] initialNatCutted = new String[]{"n ", "ese ", "ian "};
	for (String inc : initialNatCutted){
	    if (phrase.startsWith(inc))
		phrase = phrase.substring(inc.length());
	}

	return phrase = phrase.trim();
    }
}
