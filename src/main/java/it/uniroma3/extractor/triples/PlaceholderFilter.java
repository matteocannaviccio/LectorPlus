package it.uniroma3.extractor.triples;

import java.util.*;
import java.util.regex.Pattern;

public abstract class PlaceholderFilter {

    public enum PlaceholderLang {ENG, GER}

    public static PlaceholderFilter getPlaceholderFilter(PlaceholderLang l) {

        String className = "PlaceholderFilter";

        switch (l) {
            case ENG:
                className += "English";
                break;
            case GER:
                className += "German";
                break;
            default:
                className += "English";
        }

        PlaceholderFilter filter = null;

        try {
            filter = (PlaceholderFilter) Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }


        return filter;
    }

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
    protected final String[] order = new String[]{
        YEAR,
        MONTH,
        DAY,
        DATE,
        POSITION,
        NATIONALITIES,
        LENGHT,
        ORDINAL
    };

    /**
     *
     */
    protected PlaceholderFilter() {

        placeholder2patterns = new HashMap<>();

        fetch();


    }

    /**
     * Links patterns in each lang to placeholders
     */
    protected abstract void fetch();

    /**
     * Applies the regexps following the order expressed for the outside type (@see
     * and for the inside regexps
     *
     * @param phrase
     * @return
     */
    protected String replace(String phrase) {

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

        return phrase;

    }


    /**
     * Eliminate parethesis.
     *
     * @param sentence
     * @return
     */
    public abstract String preprocess(String phrase);


}
