package it.uniroma3.main.pipeline;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiArticle.ArticleType;
import it.uniroma3.main.pipeline.articleparser.Statistics;
import it.uniroma3.main.pipeline.factsextractor.FactsExtractor;
import it.uniroma3.main.util.inout.XMLReader;
import it.uniroma3.model.model.Model;
import it.uniroma3.model.model.Model.ModelType;
/**
 * 
 * @author matteo
 *
 */
public class Complete {

    private Statistics stats;
    private XMLReader inputReader;

    /**
     * 
     * @param configFile
     */
    public Complete(String inputFile){
	System.out.println("\nComplete Pipeline (it takes many hours on whole dump)");
	System.out.println("-----------------------------------------------------");
	this.stats = new Statistics();
	this.inputReader = new XMLReader(inputFile);
    }

    /**
     * 
     * @param totArticle
     * @param chunckSize
     */
    public void runPipeline(int totArticle, int chunckSize){
	List<String> lines;
	int cont = 0;

	// change it, if we need to process the whole dump
	if (totArticle == -1)
	    totArticle = Integer.MAX_VALUE;
	
	long first_start_time = System.currentTimeMillis();

	while (!(lines = inputReader.nextChunk(chunckSize)).isEmpty() && cont < totArticle) {	    
	    System.out.print("\tRunning next: " + lines.size() + " articles.\t");
	    long start_time = System.currentTimeMillis();
	    cont += lines.size();

	    lines.parallelStream()
	    .map(s -> Lector.getWikiParser().createArticleFromXml(s))
	    .map(s -> stats.addArticleToStats(s))
	    .filter(s -> s.getType() == ArticleType.ARTICLE)
	    .map(s -> Lector.getEntitiesFinder().increaseEvidence(s))
	    .map(s -> Lector.getEntitiesTagger().augmentEvidence(s))
	    .forEach(s -> Lector.getTriplifier().extractTriples(s));

	    Lector.getTriplifier().updateBlock();

	    long end_time = System.currentTimeMillis();
	    System.out.printf("%-20s %s\n", "Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.", "Reading next batch.");
	    lines.clear();
	}

	long first_end_time = System.currentTimeMillis();
	
	System.out.println("\nExecution time");
	System.out.println("---------------");
	long duration = first_end_time - first_start_time;
	System.out.println(TimeUnit.MILLISECONDS.toMinutes(duration) + " minutes --> " + TimeUnit.MILLISECONDS.toHours(duration) +" hours\n");
	
	Lector.getTriplifier().printStats();
	stats.printStats();
	inputReader.closeBuffer();
    }

    /**
     * 
     */
    public void extractNovelFacts(){
	System.out.println("\nModel creation");
	System.out.println("-----------------");
	// here we derive model table
	Lector.getDbmodel(false).deriveModelTable();
	Model model = Model.getNewModel(Lector.getDbmodel(false), "model_triples", 1, 25, ModelType.NaiveBayes, 0.4);
	FactsExtractor extractor = new FactsExtractor(model);
	extractor.runExtractOnFile(Integer.MAX_VALUE);
    }

}
