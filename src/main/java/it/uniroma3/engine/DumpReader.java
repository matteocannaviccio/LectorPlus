package it.uniroma3.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import it.uniroma3.lectorplus.WikiArticle;
import it.uniroma3.lectorplus.WikiArticle.ArticleType;
import it.uniroma3.lectorplus.WikiLanguage;
import it.uniroma3.parser.WikiParser;

/**
 * 
 * @author matteo
 *
 */
public class DumpReader {

    // public static String dump_path =
    // "/Users/matteo/Work/wikipedia-parsing/dump/en/enwiki-20161220-first.bz2";
    // public static String dump_path =
    // "/Users/matteo/Work/wikipedia-parsing/dump/test/apple.bz2";

    public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/it/itwiki-20170120-pages-articles.xml.bz2";
    //public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/es/eswiki-20161220-pages-articles.xml.bz2";

    public static String language = "it";
    
    public static String article_list_filter = "/Users/matteo/Desktop/es_gt_complete.tsv";
    
    /**
     * 
     * @return
     * @throws IOException
     */
    public static Map<String, String> getGt() throws IOException{
	Map<String, String> gt = new HashMap<String, String>();
	BufferedReader br = new BufferedReader(new FileReader(new File(article_list_filter)));
	String line;
	while((line = br.readLine()) != null){
	    gt.put(line.split("\t")[0], line.split("\t")[1]);
	}
	br.close();
	return gt;
    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
	int count_articles = 0;
	long start_time = System.currentTimeMillis();

	PrintStream out = new PrintStream(
		new FileOutputStream("/Users/matteo/Work/wikipedia-parsing/output/it_dump.json"), false, "UTF-8");
	WikipediaBz2DumpReader reader = new WikipediaBz2DumpReader(dump_path);

	WikiParser parser = new WikiParser(new WikiLanguage(language));
	
	Map<String, String> gt = getGt();

	String page;
	while (((page = reader.nextArticle()) != null) && count_articles < 1000) {
	    WikiArticle article = null;
	    try {
		
		article = parser.createArticleFromXml(page);
		
	    } catch (Exception e) {
		System.out.println(page);
		System.exit(1);
	    }

	    if (article.getType().equals(ArticleType.ARTICLE) && gt.containsKey(article.getWikid())) {
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
