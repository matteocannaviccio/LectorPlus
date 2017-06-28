package it.uniroma3.extractor.triples.filters;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
/**
 * @author matteo
 */
public class PlaceholderFilterSpanish extends PlaceholderFilter {

    /**
     * 
     */
    public PlaceholderFilterSpanish() {
	super();
    }

    @Override
    public List<Pattern> fillPositions() {
	return Arrays.asList(
		Pattern.compile("\\b("
			+ "sur(-)?oeste|"
			+ "sur(-)?este|"
			+ "norte(-)?este|"
			+ "norte(-)?oeste|"
			+ "sur(-)?central|"
			+ "norte(-)?central|"
			+ "oeste(-)?central|"
			+ "sur(-)?central|"
			+ "central(-)?norte|"
			+ "central(-)?sur|"
			+ "central(-)?este|"
			+ "central(-)?oeste"
			+ ")\\b", Pattern.CASE_INSENSITIVE),
		Pattern.compile("\\b("
			+ "norte|"
			+ "sur|"
			+ "oeste|"
			+ "este)\\b", Pattern.CASE_INSENSITIVE)
		);
    }

    @Override
    public List<Pattern> fillLengths() {
	return Arrays.asList(
		Pattern.compile("#YEAR#\\s?(km|kilómetro)\\b"),
		Pattern.compile("\\d+(\\s\\d+)*\\s?(km|kilómetro)\\b")
		);
    }

    @Override
    public List<Pattern> fillMonths() {
	return Arrays.asList(
		Pattern.compile("\\b(enero|febrero|marzo|abril|mayo|junio|julio|agosto|"
			+ "septiembre|octubre|noviembre|diciembre)\\b", Pattern.CASE_INSENSITIVE)
		);
    }

    @Override
    public List<Pattern> fillOrdinals() {
	return Arrays.asList(
		Pattern.compile("\\b\\d*1st\\b"),
		Pattern.compile("\\b\\d*2nd\\b"),
		Pattern.compile("\\b\\d*3rd\\b"),
		Pattern.compile("\\b(\\d)*\\dth\\b"),
		Pattern.compile("\\b(primer(o|a)|segundo|tercer(o|a)|cuart(o|a)|quint(o|a))\\b")
		);
    }
    
    /**
     * This is a customization of DATE regexps.
     *
     * @return
     */
    public List<Pattern> fillDates() {
	return Arrays.asList(
		Pattern.compile("#YEAR#(\\s|,\\s)#DAY#"),
		Pattern.compile("#DAY#(\\s|,\\s)#YEAR#"), 
		Pattern.compile("\\b#MONTH# de #YEAR#\\b"),
		Pattern.compile("\\b#DAY# de #MONTH#\\b")
		);
    }
    
    public static void main(String[] args){
	Configuration.init(new String[0]);
	PlaceholderFilter pf = new PlaceholderFilterSpanish();
	
	String test1 = "'' y pintó cuadros costumbristas al estilo de";
	String test2 = ", en 1998-99;";
	String test3 = ", 5250 en";

	
	System.out.println(pf.replace(test1));
	System.out.println(pf.replace(test2));
	System.out.println(pf.replace(test3));

    }
}
