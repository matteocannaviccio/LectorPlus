package it.uniroma3.pipeline;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.bean.WikiArticle.ArticleType;
import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.util.reader.XMLReader;

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
    public ArticleParser(String inputFile, boolean bzip2, String outputFile){
	this.stats = new Statistics();
	this.inputReader = new XMLReader(inputFile, bzip2);

	if (outputFile != null){
	    try {
		this.outputWriter = new PrintStream(new FileOutputStream(outputFile), false, "UTF-8");
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
	int cont = 0;

	while (!(lines = inputReader.nextChunk(Configuration.getChunkSize())).isEmpty()
		&& cont < Configuration.getNumArticlesToProcess()) {
	    System.out.print("Parsing: " + lines.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    cont += lines.size();

	    lines.parallelStream()
	    .map(s -> Lector.getWikiParser().createArticleFromXml(s))
	    .map(s -> stats.addArticleToStats(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .forEach(s -> outputWriter.println(s.toJson()));

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    lines.clear();
	}

	System.out.println("************\nProcessed articles:\n" + stats.printStats());
	stats.writeDetailsFile();
	inputReader.closeBuffer();
	outputWriter.close();
    }

}
