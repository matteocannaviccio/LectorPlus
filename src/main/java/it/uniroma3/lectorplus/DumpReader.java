package it.uniroma3.lectorplus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.FileUtils;

import it.uniroma3.lectorplus.WikiClean.WikiLanguage;



/**
 * Hello world!
 *
 */
public class DumpReader{
    
    public static String dump_path = "/Users/matteo/Work/wikipedia-parsing/test/enwiki-20161220-first.bz2";
    public static String encoding = "UTF-8";
    public static WikiLanguage language = WikiLanguage.EN;
    public static boolean withTitle = false;
    public static boolean withFooter = false;
    
    
    public static void main( String[] args ) throws IOException{
	WikiClean cleaner =
		new WikiCleanBuilder()
		.withLanguage(language)
		.withTitle(withTitle)
		.withFooter(withFooter)
		.withAliasSpan(300).build();

	PrintStream out = new PrintStream(System.out, true, "UTF-8");
	WikipediaBz2DumpReader reader = new WikipediaBz2DumpReader(dump_path);
	
	String page;
	while ((page = reader.nextArticle()) != null) {
	    if ( page.contains("<ns>") && !page.contains("<ns>0</ns>")) {
		continue;
	    }
	    
	    WikiArticle article = cleaner.retrieveArticle(page);
	    if (article != null){
		out.println(article);
		out.println("\n\n#################################\n");
	    }

	}
	out.close();
	
    }
}
