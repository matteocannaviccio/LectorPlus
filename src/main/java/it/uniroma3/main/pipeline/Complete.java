package it.uniroma3.main.pipeline;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
import it.uniroma3.extractor.util.io.XMLReader;
import it.uniroma3.main.Statistics;
import it.uniroma3.model.model.Model.ModelType;
import it.uniroma3.model.model.Model.PhraseType;

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
	
	while (!(lines = inputReader.nextChunk(chunckSize)).isEmpty()
		&& cont < totArticle) {	    
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
		
	stats.printStats();
	inputReader.closeBuffer();
    }
    
    /**
     * 
     */
    public void extractNovelFacts(){
	FactsExtractor extractor = new FactsExtractor();
	extractor.setModelForEvaluation(
		ModelType.valueOf(Configuration.getLectorModelName()),
		"labeled_triples", 
		Configuration.getMinF(), 
		Configuration.getTopK(), 
		Configuration.getCutOff(),
		PhraseType.TYPED_PHRASES);
	extractor.run();
    }

}
