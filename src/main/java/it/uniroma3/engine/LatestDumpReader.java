package it.uniroma3.engine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import it.uniroma3.lectorplus.WikiArticle;
import it.uniroma3.lectorplus.WikiArticle.ArticleType;
import it.uniroma3.lectorplus.WikiLanguage;
import it.uniroma3.parser.WikiParser;

/**
 * 
 * @author matteo
 *
 */
public class LatestDumpReader {

    /* --- EN ---- */
    public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/en/enwiki-20161220-first.bz2";
    //public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/en/enwiki-20161220-pages-articles.xml.bz2";
    public static String language = "en";


    /* --- ES ----
     * public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/es/eswiki-20161220-pages-articles.xml.bz2";
     * public static String language = "es";
     */

    /* --- IT ----
     * public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/it/itwiki-20170120-pages-articles.xml.bz2";
     * public static String language = "it";
     */


    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	int count_articles = 0;
	long start_time = System.currentTimeMillis();

	PrintStream out = new PrintStream(
		new FileOutputStream("/Users/matteo/Work/wikipedia-parsing/output/" + language + "_dump.json"), false, "UTF-8");
	WikipediaBz2DumpReader reader = new WikipediaBz2DumpReader(dump_path);

	WikiParser parser = new WikiParser(new WikiLanguage(language));

	String page;
	while (((page = reader.nextArticle()) != null)) {
	    WikiArticle article = null;
	    
	    try {

		article = parser.createArticleFromXml(page);

	    } catch (Exception e) {
		System.out.println(page);
		System.exit(1);
	    }

	    if (article.getType().equals(ArticleType.ARTICLE)) {
		count_articles += 1;
		System.out.println("Articles: " + count_articles);
		out.println(article.toJson());
	    }

	}
	long end_time = System.currentTimeMillis();

	System.out.println("Articles: " + count_articles);
	System.out.println("Time: " + (end_time - start_time) + " ms.");

	out.close();

    }
}
