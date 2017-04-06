package it.uniroma3.pipeline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiArticle.ArticleType;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;
import it.uniroma3.reader.XMLReader;

/**
 * This is the main entry point of the parser.
 * 
 * It reads a configuration file where is specified the path of a Wikipedia XML
 * dump and start to read it, parsing the articles one by one, and creating a
 * pipeline of processment that acts over a "chunck" of articles (size in config
 * file).
 * 
 * The pipeline is composed by:
 * 
 * - an XMLReder: responsible to extract the wikimarkup of the articles from the
 * XML dump;
 * 
 * - a WikiParser: responsible to extract a structured version ev of the article
 * (i.e. WikiArticle);
 * 
 * - an EntityAugmenter: implementation of the Edntity Detection module that
 * allows to increase the instances of wikilinks.
 * 
 * 
 * 
 * @author matteo
 *
 */
public class ArticleParser {

    private Statistics stats;
    private XMLReader inputReader;
    private WikiParser parser;
    private PrintStream outputWriter;

    /**
     * 
     * @param configFile
     */
    public ArticleParser(String configFile){
	Configuration.init(configFile);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	this.stats = new Statistics();
	this.inputReader = new XMLReader(Configuration.getOriginalArticlesFile(), true);
	this.parser = new WikiParser(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));

	try {

	    this.outputWriter = new PrintStream(new FileOutputStream(Configuration.getParsedArticlesFile()), false, "UTF-8");

	} catch (UnsupportedEncodingException | FileNotFoundException e) {
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param lines
     * @param parser
     * @param repFinder
     * @param outputWriter
     */
    private void process(List<String> lines){
	System.out.print("Parsing: " + lines.size() + " articles.\t");
	long start_time = System.currentTimeMillis();

	lines.parallelStream().map(s -> parser.createArticleFromXml(s))
	.map(s -> stats.addArticleToStats(s))
	.filter(s -> s.getType() == ArticleType.ARTICLE)
	.forEach(s -> outputWriter.println(s.toJson()));

	long end_time = System.currentTimeMillis();
	System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	System.out.println("Reading next batch.");
	lines.clear();
    }

    /**
     * Entry point!
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {

	String configFile;
	if (args.length == 0){
	    configFile = "/Users/matteo/Desktop/data/config.properties";
	}else{
	    configFile = args[0];
	}

	ArticleParser parser = new ArticleParser(configFile);
	
	int countArticles = 0;
	int totArticles = Configuration.getNumArticlesToProcess();
	if (totArticles == -1)
	    totArticles = 100000000;

	/******************************************************************/
	List<String> lines;
	while (!(lines = parser.inputReader.nextChunk(Configuration.getChunkSize())).isEmpty()) {
	    countArticles += lines.size();
	    if (countArticles > totArticles){
		parser.process(lines.subList(0, lines.size() - (countArticles - Configuration.getNumArticlesToProcess())));
		break;
	    }
	    parser.process(lines);
	}
	System.out.println("************\nProcessed articles:\n" + parser.stats.printStats());
	parser.stats.writeDetailsFile();
	parser.inputReader.closeBuffer();
	parser.outputWriter.close();
	/******************************************************************/

    }

}
