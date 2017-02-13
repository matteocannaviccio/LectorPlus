package it.uniroma3.parser;

import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.reader.XMLReader;

public class ParserTest {
    
    public static String input_file = "/Users/matteo/Work/Repository/ualberta/lectorplus/src/main/java/resources/testing/wrongFirst_Test.xml.bz2";
    public static String language = "en";
    
    public static void main(String[] args) {
	
	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(input_file);

	// parser
	WikiParser parser = new WikiParser(new WikiLanguage(language));
	
	/* ------ EXECUTION ------ */
	String page = reader.getArticle();
	//System.out.println(page);
	
	WikiArticle article = parser.createArticleFromXml(page);
	System.out.println(article);
	
	

    }
    

}
