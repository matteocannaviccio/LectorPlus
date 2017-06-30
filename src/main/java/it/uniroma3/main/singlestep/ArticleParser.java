package it.uniroma3.main.singlestep;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
import it.uniroma3.extractor.util.io.XMLReader;
import it.uniroma3.main.Statistics;

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
    private PrintStream outputWriter;
    
    /**
     * 
     * @param configFile
     */
    public ArticleParser(String inputFile, String outputFile){
	System.out.println("\n**** ARTICLE PARSER ****");
	this.stats = new Statistics();
	this.inputReader = new XMLReader(inputFile);

	if (outputFile != null){
	    try {
		File output = new File(outputFile);
		output.getParentFile().mkdirs();
		this.outputWriter = new PrintStream(new FileOutputStream(output.getAbsolutePath()), false, "UTF-8");
	    } catch (UnsupportedEncodingException | FileNotFoundException e) {
		e.printStackTrace();
	    }
	}
    }


    /**
     * 
     * @param lines
     * @return
     */
    public void pipelinedProcess(){
	List<String> lines;
	List<WikiArticle> processedArticles = new LinkedList<WikiArticle>();
	int cont = 0;

	while (!(lines = inputReader.nextChunk(Configuration.getChunkSize())).isEmpty()
		&& cont < Configuration.getNumArticlesToProcess()) {
	    System.out.print("\nParsing: " + lines.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    cont += lines.size();

	    processedArticles = lines.parallelStream()
	    .map(s -> Lector.getWikiParser().createArticleFromXml(s))
	    .map(s -> stats.addArticleToStats(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .collect(Collectors.toList());

	    processedArticles.parallelStream().forEach(s -> outputWriter.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.printf("%-20s %s\n", "Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.", "Reading next batch.");
	    processedArticles.clear();
	    lines.clear();
	}

	stats.printStats();
	stats.writeDetailsFile();
	inputReader.closeBuffer();
	outputWriter.close();
	
    }

}
