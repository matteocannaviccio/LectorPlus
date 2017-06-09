package it.uniroma3.extractor.triples.filters;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by khorda on 09/06/17.
 */
public class PlaceholderFilterItalian extends PlaceholderFilter {
    @Override
    public String preProcess(String phrase) {
        return null;
    }

    @Override
    public String postProcess(String phrase) {
        return null;
    }

    @Override
    protected String[] setPatternApplicationOrder() {
        return new String[0];
    }

    @Override
    public List<Pattern> fillPositions() {
        return null;
    }

    @Override
    public List<Pattern> fillLengths() {
        return null;
    }

    @Override
    public List<Pattern> fillDates() {
        return null;
    }

    @Override
    public List<Pattern> fillDays() {
        return null;
    }

    @Override
    public List<Pattern> fillMonths() {
        return null;
    }

    @Override
    public List<Pattern> fillYears() {
        return null;
    }

    @Override
    public List<Pattern> fillEras() {
        return null;
    }

    @Override
    public List<Pattern> fillOrdinals() {
        return null;
    }

    @Override
    public List<Pattern> fillNationalities() {
        return null;
    }
}
