package it.uniroma3.pipeline.triplesextractor.placeholders;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author matteo
 */
public class PlaceholderFilterGerman extends PlaceholderFilter {

    /**
     * 
     */
    public PlaceholderFilterGerman() {
        super();
    }

    @Override
    public List<Pattern> fillPositions() {
        return Arrays.asList(
            Pattern.compile("\\b("
                + "süd(en)?(-)?westen|"
                + "süd(en)?(-)?osten|"
                + "nord(en)?(-)?osten|"
                + "nord(en)?(-)?westen|"
                + "süd(en)?(-)?zentral|"
                + "nord(en)?(-)?zentral|"
                + "westen(-)?zentral|"
                + "süd(en)?(-)?zentral|"
                + "zentral(-)?nord(en)?|"
                + "zentral(-)?süd(en)?|"
                + "zentral(-)?osten?|"
                + "zentral(-)?westen"
                + ")\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b("
                + "norden|"
                + "süden|"
                + "westen|"
                + "osten"
                + ")\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b("
                + "nord|"
                + "süd|"
                + "westen|"
                + "osten)\\b", Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public List<Pattern> fillLengths() {
        return Arrays.asList(
            Pattern.compile("#YEAR#\\s?(km|kilometre)(s?)\\b"),
            Pattern.compile("\\d+(\\s\\d+)*\\s?(km|kilometer)\\b")
        );
    }

    @Override
    public List<Pattern> fillMonths() {
        return Arrays.asList(
            Pattern.compile("\\b(januar|februar|märz|april|mai|juni|juli|august|"
                + "september|oktober|november|dezember)\\b", Pattern.CASE_INSENSITIVE)
        );
    }

    @Override
    public List<Pattern> fillOrdinals() {
        return Arrays.asList(
            Pattern.compile("\\b\\d*1st\\b"),
            Pattern.compile("\\b\\d*2nd\\b"),
            Pattern.compile("\\b\\d*3rd\\b"),
            Pattern.compile("\\b(\\d)*\\dth\\b"),
            Pattern.compile("\\b(erste|zweite|dritte|vierte|fünfte)\\b")
        );
    }

}
