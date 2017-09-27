package it.uniroma3.main.pipeline.triplesextractor.placeholders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author matteo
 */
public class PlaceholderFilterItalian extends PlaceholderFilter {

    /**
     * 
     */
    public PlaceholderFilterItalian() {
        super();
    }

    @Override
    public List<Pattern> fillPositions() {
        return Arrays.asList(
            Pattern.compile("\\b("
                + "sud(ern)?(-)?ovest(ern)?|"
                + "sud(ern)?(-)?est(ern)?|"
                + "nord(ern)?(-)?est(ern)?|"
                + "nord(ern)?(-)?ovest(ern)?|"
                + "sud(ern)?(-)?centrale|"
                + "nord(ern)?(-)?centrale|"
                + "ovest(ern)?(-)?centrale|"
                + "sud(ern)?(-)?centrale|"
                + "centrale(-)?nord(ern)?|"
                + "centrale(-)?sud(ern)?|"
                + "centrale(-)?est(ern)?|"
                + "centrale(-)?ovest(ern)?"
                + ")\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b("
                + "nord|"
                + "sud|"
                + "ovest|"
                + "est)\\b", Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public List<Pattern> fillLengths() {
        return Arrays.asList(
            Pattern.compile("#YEAR#\\s?(km|chilometr(o|i)?|m|cm|mm|dm)\\b"),
            Pattern.compile("\\d+(\\s\\d+)*\\s?(km|chilometr(o|i)?|m|dm|cm|mm)\\b")
        );
    }


    @Override
    public List<Pattern> fillMonths() {
        return Arrays.asList(
            Pattern.compile("\\b(gennaio|febbraio|marzo|aprile|maggio|giugno|luglio|agosto|"
                + "settembre|ottobre|novembre|dicembre)\\b", Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public List<Pattern> fillOrdinals() {
        return Arrays.asList(
            Pattern.compile("\\b\\d*1st\\b"),
            Pattern.compile("\\b\\d*2nd\\b"),
            Pattern.compile("\\b\\d*3rd\\b"),
            Pattern.compile("\\b(\\d)*\\dth\\b"),
            Pattern.compile("\\b(primo|secondo|terzo|quarto|quinto)\\b")
        );
    }
    
    @Override
    public Set<String> fillConjuctions() {
	return new HashSet<String>(Arrays.asList(
		"e",
		"a",
		"ed", 
		",",
		", e",
		", a",
		", ed",
		"e il", 
		"e a", 
		"e di"));
    }
}
