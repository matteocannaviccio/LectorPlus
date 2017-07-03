package it.uniroma3.extractor._triplesextractor.placeholders;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author matteo
 */
public class PlaceholderFilterEnglish extends PlaceholderFilter {

    /**
     * 
     */
    public PlaceholderFilterEnglish() {
	super();
    }

    @Override
    public List<Pattern> fillPositions() {
	return Arrays.asList(
		Pattern.compile("\\b("
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
			+ ")\\b", Pattern.CASE_INSENSITIVE),
		Pattern.compile("\\b("
			+ "northern|"
			+ "southern|"
			+ "western|"
			+ "eastern"
			+ ")\\b", Pattern.CASE_INSENSITIVE),
		Pattern.compile("\\b("
			+ "north|"
			+ "south|"
			+ "west|"
			+ "east)\\b", Pattern.CASE_INSENSITIVE)
		);
    }

    @Override
    public List<Pattern> fillLengths() {
	return Arrays.asList(
		Pattern.compile("#YEAR#\\s?(km|kilometer(s)?|mi|ft|yd|m)(s?)\\b"),
		Pattern.compile("\\d+(\\s\\d+)*\\s?(km|kilometer(s)?|mi|ft|yd|m)(s?)\\b")
		);
    }



    @Override
    public List<Pattern> fillMonths() {
	return Arrays.asList(
		Pattern.compile("\\b(january|february|march|april|may|june|july|august|"
			+ "september|october|november|december)\\b", Pattern.CASE_INSENSITIVE)
		);
    }


    @Override
    public List<Pattern> fillOrdinals() {
	return Arrays.asList(
		Pattern.compile("\\b\\d*1st\\b"),
		Pattern.compile("\\b\\d*2nd\\b"),
		Pattern.compile("\\b\\d*3rd\\b"),
		Pattern.compile("\\b(\\d)*\\dth\\b"),
		Pattern.compile("\\b(first|second|third|fourth|fifth)\\b")
		);
    }

}
