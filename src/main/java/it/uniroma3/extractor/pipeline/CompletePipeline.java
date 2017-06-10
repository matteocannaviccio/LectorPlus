package it.uniroma3.extractor.pipeline;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
import it.uniroma3.extractor.util.reader.XMLReader;
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
    public CompletePipeline(String inputFile, boolean bzip2){
	System.out.println("\n**** COMPLETE PIPELINE IN ONCE ****");
	this.stats = new Statistics();
	this.inputReader = new XMLReader(inputFile, bzip2);
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
	    .map(s -> Lector.getEntitiesFinder().increaseEvidence(s))
	    .map(s -> Lector.getEntitiesTagger().augmentEvidence(s))
	    .forEach(s -> Lector.getTriplifier().extractTriples(s));

	    Lector.getTriplifier().updateBlock();

	    long end_time = System.currentTimeMillis();
	    System.out.print("Done in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.\t");
	    System.out.println("Reading next batch.");
	    lines.clear();
	}

	System.out.println("************\nProcessed articles:\n" + stats.printStats());
	
	inputReader.closeBuffer();

	FactsExtractor extractor = new FactsExtractor();
	extractor.setModelForEvaluation(ModelType.LectorScore, "labeled_triples", 1, -1, PhraseType.TYPED_PHRASES);
	extractor.runExtraction();
    }

}
