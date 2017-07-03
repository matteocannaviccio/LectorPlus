package it.uniroma3.extractor._triplesextractor.placeholders;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author matteo
 */
public class PlaceholderFilterFrench extends PlaceholderFilter {

    /**
     * 
     */
    public PlaceholderFilterFrench() {
        super();
    }

    @Override
    public List<Pattern> fillPositions() {
        return Arrays.asList(
            Pattern.compile("\\b("
                + "sud(-)?ouest|"
                + "sud(-)?est|"
                + "nord(-)?est|"
                + "nord(-)?ouest|"
                + "sud(-)?central|"
                + "nord(-)?central|"
                + "ouest(-)?central|"
                + "sud(-)?central|"
                + "central(-)?nord|"
                + "central(-)?sud|"
                + "central(-)?est|"
                + "central(-)?ouest"
                + ")\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b("
                + "nord|"
                + "sud|"
                + "ouest|"
                + "est)\\b", Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public List<Pattern> fillLengths() {
        return Arrays.asList(
            Pattern.compile("#YEAR#\\s?(km|kilomètre)\\b"),
            Pattern.compile("\\d+(\\s\\d+)*\\s?(km|kilomètre)\\b")
        );
    }

    @Override
    public List<Pattern> fillMonths() {
        return Arrays.asList(
            Pattern.compile("\\b(janvier|février|mars|avril|mai|juin|juillet|août|"
                + "septembre|octobre|novembre|décembre)\\b", Pattern.CASE_INSENSITIVE)
        );
    }


    @Override
    public List<Pattern> fillOrdinals() {
        return Arrays.asList(
            Pattern.compile("\\b\\d*1st\\b"),
            Pattern.compile("\\b\\d*2nd\\b"),
            Pattern.compile("\\b\\d*3rd\\b"),
            Pattern.compile("\\b(\\d)*\\dth\\b"),
            Pattern.compile("\\b(premier|première|deuxième|second|seconde|troisième|quatrième|cinquième)\\b")
        );
    }

}
