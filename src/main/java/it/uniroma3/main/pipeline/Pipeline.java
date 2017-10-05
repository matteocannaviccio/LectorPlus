package it.uniroma3.main.pipeline;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiArticle;
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
public class Pipeline {

  private Statistics stats;
  private XMLReader dumpFileReader;
  // private PrintStream parsedDumpWriter;
  private PrintStream augmentedDumpWriter;


  /**
   * 
   * @param configFile
   */
  public Pipeline(String dumpFile, String parsedDump, String augmentedDump) {
    System.out.println("\nComplete Pipeline (it takes many hours on whole dump)");
    System.out.println("-----------------------------------------------------");
    this.stats = new Statistics();
    this.dumpFileReader = new XMLReader(dumpFile);

    /*
     * if (parsedDump != null){ try { File outputParsed = new File(parsedDump);
     * outputParsed.getParentFile().mkdirs(); this.parsedDumpWriter = new PrintStream(new
     * FileOutputStream(outputParsed.getAbsolutePath()), false, "UTF-8"); } catch
     * (UnsupportedEncodingException | FileNotFoundException e) { e.printStackTrace(); } }
     */

    if (augmentedDump != null) {
      try {
        File outputAugmented = new File(augmentedDump);
        outputAugmented.getParentFile().mkdirs();
        this.augmentedDumpWriter = new PrintStream(
            new FileOutputStream(outputAugmented.getAbsolutePath()), false, "UTF-8");
      } catch (UnsupportedEncodingException | FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 
   * @param totArticle
   * @param chunckSize
   * @param pipelineSteps
   */
  public void runPipeline(int totArticle, int chunckSize, String pipelineSteps) {
    List<String> lines;
    int cont = 0;
    int contChunk = 0;
    // change it, if we need to process the whole dump
    if (totArticle == -1)
      totArticle = Integer.MAX_VALUE;

    long total_start_time = System.currentTimeMillis();

    while (!(lines = dumpFileReader.nextChunk(chunckSize)).isEmpty() && cont < totArticle) {
      contChunk += 1;
      System.out.print("\t" + contChunk + ") Running next: " + lines.size() + " articles.");
      cont += lines.size();

      // article parser
      long start_pars_time = System.currentTimeMillis();
      List<WikiArticle> chunk =
          lines.parallelStream().map(s -> Lector.getWikiParser().createArticleFromXml(s))
              .map(s -> stats.addArticleToStats(s)).filter(s -> s.getType() == ArticleType.ARTICLE)
              .collect(Collectors.toList());
      long end_pars_time = System.currentTimeMillis();
      long pars_time = TimeUnit.MILLISECONDS.toSeconds(end_pars_time - start_pars_time);
      System.out.print("\tParsed in: " + pars_time + " sec.");

      // write parsed articles
      /*
       * long start_pars_wrt_time = System.currentTimeMillis(); chunk.parallelStream() .forEach(s ->
       * parsedDumpWriter.println(s.toJson())); long end_pars_wrt_time = System.currentTimeMillis();
       * long pars_wrt_time = TimeUnit.MILLISECONDS.toSeconds(end_pars_wrt_time -
       * start_pars_wrt_time); System.out.print("\tStored in: " + pars_wrt_time + " sec.\t");
       */

      // detect entities in articles
      long start_aug_time = System.currentTimeMillis();
      chunk.parallelStream().map(s -> Lector.getEntitiesFinder().increaseEvidence(s))
          .map(s -> Lector.getEntitiesTagger().augmentEvidence(s)).collect(Collectors.toList());
      long end_aug_time = System.currentTimeMillis();
      long aug_time = TimeUnit.MILLISECONDS.toSeconds(end_aug_time - start_aug_time);
      System.out.print("\tAugmented in: " + aug_time + " sec.");

      // write articles with entities
      if (!Configuration.inMemoryProcess()) {
        long start_aug_wrt_time = System.currentTimeMillis();
        chunk.parallelStream().forEach(s -> augmentedDumpWriter.println(s.toJson()));
        long end_aug_wrt_time = System.currentTimeMillis();
        long aug_wrt_time = TimeUnit.MILLISECONDS.toSeconds(end_aug_wrt_time - start_aug_wrt_time);
        System.out.print("\tStored in: " + aug_wrt_time + " sec.");
      }

      // extract triples from articles and write to db
      long start_tripl_time = System.currentTimeMillis();
      chunk.parallelStream().forEach(s -> Lector.getTriplifier().extractTriples(s));
      Lector.getTriplifier().updateBlock(); // write to db
      long end_tripl_time = System.currentTimeMillis();
      long tripl_time = TimeUnit.MILLISECONDS.toSeconds(end_tripl_time - start_tripl_time);
      System.out.print("\tTriplified in: " + tripl_time + " sec.\t");

      System.out.println("\tReading next batch.");
      lines.clear();
    }

    System.out.println("\nExecution time");
    System.out.println("---------------");
    long total_end_time = System.currentTimeMillis();
    long total_duration = total_end_time - total_start_time;
    System.out.println(TimeUnit.MILLISECONDS.toMinutes(total_duration) + " minutes --> "
        + TimeUnit.MILLISECONDS.toHours(total_duration) + " hours\n");

    stats.printStats();
    stats.writeDetailsFile();
    Lector.getTriplifier().printStats();

    dumpFileReader.close();
    // parsedDumpWriter.close();
    augmentedDumpWriter.close();

  }

  /**
   * 
   */
  public void extractNovelFacts() {
    System.out.println("\nModel creation");
    System.out.println("-----------------");
    // here we derive model table
    Lector.getDbmodel(false).deriveModelTable();
    Model model =
        Model.getNewModel(Lector.getDbmodel(false), "model_triples", Configuration.getMinF(),
            Configuration.getPercUnl(), ModelType.NaiveBayes, Configuration.getMajThr());
    FactsExtractor extractor = new FactsExtractor(model);
    extractor.runExtractOnFile(Integer.MAX_VALUE);
  }

}
