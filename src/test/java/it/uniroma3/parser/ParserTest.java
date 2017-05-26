package it.uniroma3.parser;

import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.entitydetection.ReplFinder;
import it.uniroma3.extractor.parser.WikiParser;
import it.uniroma3.extractor.util.reader.XMLReader;

public class ParserTest {


    public static void main(String[] args) {

	Configuration.init(new String[0]);
	
	ReplFinder repFinder = new ReplFinder(); 

	/* ------ PIPELINE COMPONENTS ------ */
	// reader
	XMLReader reader = new XMLReader(Configuration.getOriginalArticlesFile(), false);

	// parser
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));

	/* ------ EXECUTION ------ */
	String page = reader.getArticle();

	WikiArticle article = parser.createArticleFromXml(page);
	//System.out.println(article);

	article = repFinder.increaseEvidence(article);
	System.out.println(article);
	
	
	
    }


}
