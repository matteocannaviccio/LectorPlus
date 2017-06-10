 package it.uniroma3.extractor.triples.filters;

import it.uniroma3.extractor.bean.WikiLanguage;

import java.util.*;
import java.util.regex.Pattern;

public abstract class PlaceholderFilter {


    protected Map<String, List<Pattern>> placeholder2patterns;

    //possibili replacements
    protected final static String POSITION = "#POS#";
    protected final static String LENGHT = "#LEN#";
    protected final static String DATE = "#DATE#";
    protected final static String DAY = "#DAY#";
    protected final static String MONTH = "#MONTH#";
    protected final static String YEAR = "#YEAR#";
    protected final static String ERA = "#ERA#";
    protected final static String ORDINAL = "#ORD#";
    protected final static String NATIONALITIES = "#NAT#";

    //ordine di applicazione, il primo in lista e' il primo applicato
    protected final String[] order;

    public PlaceholderFilter() {

        placeholder2patterns = new HashMap<>();

        placeholder2patterns.put(POSITION, fillPositions());
        placeholder2patterns.put(LENGHT, fillLengths());
        placeholder2patterns.put(DATE, fillDates());
        placeholder2patterns.put(DAY, fillDays());
        placeholder2patterns.put(MONTH, fillMonths());
        placeholder2patterns.put(YEAR, fillYears());
        placeholder2patterns.put(ERA, fillEras());
        placeholder2patterns.put(ORDINAL, fillOrdinals());
        placeholder2patterns.put(NATIONALITIES, fillNationalities());
        
        this.order = setPatternApplicationOrder();


    }


    /**
     * Applies the regexps following the order expressed for the outside type (@see
     * and for the inside regexps
     *
     * @param phrase
     * @return
     */
    public String replace(String phrase) {

        phrase = preProcess(phrase);

        //il replace segue l'ordine impostato nella variabile 'order'
        for (int k = 0; k < order.length; k++) {

            String placeholder = order[k];

            //per una certa lingua potrebbe essere stato omesso un placholder
            if (!Objects.isNull(placeholder2patterns.get(placeholder))) {

                List<Pattern> placeholderRegexps = placeholder2patterns.get(placeholder);

                //mantiene l'ordinamento
                for (int i = 0; i < placeholderRegexps.size(); i++) {

                    phrase = placeholderRegexps
                        .get(i) //in ordine
                        .matcher(phrase)
                        .replaceAll(placeholder);

                }
            }


        }

        phrase = postProcess(phrase);

        return phrase;

    }

    protected abstract String preProcess(String phrase);
    protected abstract String postProcess(String phrase);

    protected abstract String[] setPatternApplicationOrder();

    protected abstract List<Pattern> fillPositions();
    protected abstract List<Pattern> fillLengths();
    protected abstract List<Pattern> fillDates();
    protected abstract List<Pattern> fillDays();
    protected abstract List<Pattern> fillMonths();
    protected abstract List<Pattern> fillYears();
    protected abstract List<Pattern> fillEras();
    protected abstract List<Pattern> fillOrdinals();
    protected abstract List<Pattern> fillNationalities();


    public static PlaceholderFilter getPlaceholderFilter(WikiLanguage.Lang lang) {
        switch (lang){
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
}
