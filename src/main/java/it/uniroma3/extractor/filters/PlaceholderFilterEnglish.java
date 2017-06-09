package it.uniroma3.extractor.filters;

import com.sun.istack.internal.NotNull;
import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.util.reader.TSVReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author matteo
 */
public class PlaceholderFilterEnglish extends PlaceholderFilter {

    public PlaceholderFilterEnglish() {

        super();

    }

    @Override
    protected String[] setPatternApplicationOrder() {
        return new String[]{
            YEAR,
            MONTH,
            DAY,
            DATE,
            POSITION,
            NATIONALITIES,
            LENGHT,
            ORDINAL
        };
    }


    /**
     * Eliminate parethesis.
     *
     * @param phrase
     * @return
     */
    public String preProcess(String phrase) {
        phrase = Lector.getTextParser().removeParenthesis(phrase);
        phrase = phrase.toLowerCase();

        return phrase;
    }

    @Override
    public String postProcess(String phrase) {

        phrase = phrase.replaceAll("''", "");
        phrase = phrase.replaceAll("\"", "");

	/* remove possible special characters at the beginning or at the end.
     * for example, we want to remove phrases that begin with ", ', -
	 * but not with 's. For this reason we check the space after the character.
	 */
        if (phrase.startsWith("' ") || phrase.startsWith("\" ") || phrase.startsWith("- ") || phrase.startsWith(": "))
            phrase = phrase.substring(1).trim();
        if (phrase.endsWith(" '") || phrase.endsWith(" \"") || phrase.endsWith(" -") || phrase.endsWith(" :"))
            phrase = phrase.substring(0, phrase.length() - 1).trim();

        Pattern pattern = Pattern.compile("([A-Za-z0-9,'´#\\.\\- ]+)");
        Matcher matcher = pattern.matcher(phrase);
        if (!matcher.matches())
            phrase = "";
        else {
            pattern = Pattern.compile("([,'´#\\.\\- ]+)");
            matcher = pattern.matcher(phrase);
            if (matcher.matches())
                phrase = "";
        }

	/*
     * Some nationalities are cutted, e.g. [Canad]ian or [French]ese
	 */
        String[] initialNatCutted = new String[]{"n ", "ese ", "ian "};
        for (String inc : initialNatCutted) {
            if (phrase.startsWith(inc))
                phrase = phrase.substring(inc.length());
        }

        return phrase = phrase.trim();
    }

    @NotNull
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
    public List<Pattern> fillDates() {
        return Arrays.asList(
            Pattern.compile("#YEAR#(\\s|,\\s)#DAY#"),
            Pattern.compile("#DAY#(\\s|,\\s)#YEAR#")
        );
    }

    @Override
    public List<Pattern> fillDays() {
        return Arrays.asList(
            Pattern.compile("([0-3]?[0-9]–)?[0-3]?[0-9]\\s#MONTH#"),
            Pattern.compile("#MONTH#\\s([0-3]?[0-9]–)?[0-3]?[0-9]")
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
    public List<Pattern> fillYears() {
        return Arrays.asList(Pattern.compile("\\b((1|2)\\d\\d\\d)\\b"));
    }

    @Override
    public List<Pattern> fillEras() {
        return Arrays.asList(
            Pattern.compile("\\b#YEAR#s\\b"),
            Pattern.compile("\\b#[0-9]0s\\b")
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

    @Override
    public List<Pattern> fillNationalities() {

        Set<String> nationalities = TSVReader.getLines2Set(Configuration.getNationalitiesList());

        List<Pattern> natPat = new ArrayList<>();
        for (String nat : nationalities) {
            nat = nat.replaceAll("_", " ");
            Pattern NAT = Pattern.compile("\\b" + nat + "\\b", Pattern.CASE_INSENSITIVE);
            natPat.add(NAT);
        }

        return natPat;
    }

    public static void main(String[] args) {
/*
        PlaceholderFilter p = new PlaceholderFilterEnglish();

        String test1 = "The 1992 WAFU Club Championship was the 16th football club tournament season that took place for the runners-up or third place of each West African country's domestic league, the West African Club Championship. It was won by Mali's Stade Malien after defeating Guinea's Hafia FC in two legs.[1] A total of about 33 goals were scored, half than last season as three clubs fully forfeited the match and two, Liberté FC Niamey and Jeanne d'Arc of Dakar withdrew after the first leg. ASEC Nouadhbihou (now part of FC Nouadhibou) withdrew in a second match with Lobi Bank, one club Dawu Youngsters of Ghana were disqualified. Neither club from the Gambia nor Guinea-Bissau participated.";


        System.out.println(p.replace(test1));

*/
    }
}
