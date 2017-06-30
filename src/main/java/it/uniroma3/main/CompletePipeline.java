package it.uniroma3.main;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
import it.uniroma3.extractor.util.io.XMLReader;
import it.uniroma3.model.extraction.FactsExtractor;
import it.uniroma3.model.extraction.FactsExtractor.ModelType;
import it.uniroma3.model.model.Model.PhraseType;

public class CompletePipeline {

    private Statistics stats;
    private XMLReader inputReader;

    /**
     * 
     * @param configFile
     */
    public CompletePipeline(String inputFile){
	System.out.println("--------------------");
	System.out.println("COMPLETE PIPELINE");
	System.out.println("--------------------");
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

	while (!(lines = inputReader.nextChunk(chunckSize)).isEmpty()
		&& cont < totArticle) {	    
	    System.out.print("Running next: " + lines.size() + " articles.\t");
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
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    lines.clear();
	}
	System.out.println("--------------------");

	System.out.println("************\nProcessed articles:\n" + stats.printStats());
	inputReader.closeBuffer();
    }
    
    /**
     * 
     */
    public void extractNovelFacts(){
	FactsExtractor extractor = new FactsExtractor();
	extractor.setModelForEvaluation(
		ModelType.TextExtChallenge, 
		"labeled_triples", 
		Configuration.getMinF(), 
		Configuration.getTopK(), 
		Configuration.getCutOff(),
		PhraseType.TYPED_PHRASES);
	extractor.run();
    }

}
