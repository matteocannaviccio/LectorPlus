package it.uniroma3.parser;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.EntityAugmenter;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.reader.XMLReader;

public class ParserTest {


    public static void main(String[] args) {

	Configuration.setConfigFile("/Users/matteo/Work/Repository/java/lectorplus/config.properties");

	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(Configuration.getTestWikipediaDump(), false);

	// parser
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageUsed()));

	/* ------ EXECUTION ------ */
	String page = reader.getArticle();

	WikiArticle article = parser.createArticleFromXml(page);
	//System.out.println(article);

	article = EntityAugmenter.augmentEntities(article);
	System.out.println(article);
	
	
	
    }


}
