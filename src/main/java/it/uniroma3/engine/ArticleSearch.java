package it.uniroma3.engine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import it.uniroma3.parser.XMLParser;

public class ArticleSearch {

    public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/dump/it/itwiki-20170120-pages-articles.xml.bz2";
    public static String articleToSearch = "Codici ICAO K";
    
    
    public static void main( String[] args ){
	int count_pages = 0;
	long start_time = System.currentTimeMillis();

	PrintStream out = null;
	try {
	    out = new PrintStream(new FileOutputStream("/Users/matteo/Work/wikipedia-parsing/output/it_dump.txt"), false, "UTF-8");
	    WikipediaBz2DumpReader reader = new WikipediaBz2DumpReader(dump_path);

	    String page;
	    while ((page = reader.nextArticle()) != null) {
		count_pages+=1;
		String name = XMLParser.getFieldFromXml(page, "title");
		if (name.equals(articleToSearch))
		    System.out.println(page);
	    }

	} catch (UnsupportedEncodingException | FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	long end_time = System.currentTimeMillis();

	System.out.println("Pages processed: " + count_pages);
	System.out.println("Time: " + (end_time - start_time) + " ms.");

	out.close();

    }

}
