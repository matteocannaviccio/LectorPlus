package it.uniroma3.entitydetection.experiment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.ReplacementsFinder;
import it.uniroma3.entitydetection.experiment.tool.DBPediaSpotlight;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.parser.WikiParser;
import it.uniroma3.reader.XMLReader;
import it.uniroma3.util.Pair;
/**
 * 
 * 
 * @author matteo
 *
 */
public class RunExperiment {

    /**
     * Entry point!
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

	/*
	 * Read the config file and instantiate a Configuration object.
	 */
	ReplacementsFinder repFinder = new ReplacementsFinder();
	
	/*
	 * RECALL, in order to run this experiment you shild include the parser
	 * to extract clean text from the articles. Now removed.
	 */
	Configuration.init("/Users/matteo/Work/Repository/java/lectorplus/config.properties");

	/* ********************************
	 * *******  STEP PIPELINE  ********
	 * ********************************/
	boolean writeJson = true;
	boolean writeLectorED = true;
	boolean writeDBPedia = false;
	boolean writeStats = true;

	/* ********************************
	 * *****  PIPELINE COMPONENTS *****
	 * ********************************/

	/*
	 * Wikipedia article parser.
	 * From a span of xml text to a WikiArticle object.
	 */
	WikiParser parser = new WikiParser(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
	String experimentFolder = Configuration.getExperimentFolder();
	XMLReader reader = null;
	String output_lectorED = experimentFolder + "lector_results.tsv";
	String output_dbsp_file = experimentFolder + "dbpedia_results.tsv";

	/*
	 * OUTPUT EVALUATION FILES
	 */
	String header = "wikid\ttool\tdomain\tpe_abs\tpe_body\tpe_full\tse_abs\tse_body\tse_full\n";
	String precision_file = experimentFolder + "precision.tsv";
	PrintStream precWriter = new PrintStream(new FileOutputStream(precision_file), false, "UTF-8");
	precWriter.print(header);
	String recall_file = experimentFolder + "recall.tsv";
	PrintStream recallWriter = new PrintStream(new FileOutputStream(recall_file), false, "UTF-8");
	recallWriter.print(header);


	/* ********************************
	 * *********** EXECUTION **********
	 * ********************************/

	/*
	 * Dump reader from XML dump. 
	 * "false" because is already in .xml format
	 */
	if (writeJson){
	    System.out.println("Running for backup json files ... ");
	    String output_json_file = experimentFolder + "articles_result.json";
	    PrintStream json = new PrintStream(new FileOutputStream(output_json_file), false, "UTF-8");
	    reader = new XMLReader(Configuration.getInputDump50Articles(), false);
	    reader.nextChunk(Configuration.getChunkSize())
	    .stream()
	    .map(s -> parser.createArticleFromXml(s))
	    .map(s -> repFinder.increaseEvidence(s))
	    .forEach(s -> json.println(s.toJson()));
	    json.close();
	    reader.closeBuffer();
	}

	/*
	 * Dump reader from XML dump. 
	 * "false" because is already in .xml format
	 */
	if(writeLectorED){
	    System.out.println("Running for lectorED evaluation ... ");
	    PrintStream led = new PrintStream(new FileOutputStream(output_lectorED), false, "UTF-8");
	    reader = new XMLReader(Configuration.getInputDump50Articles(), false);
	    reader.nextChunk(Configuration.getChunkSize())
	    .stream()
	    .map(s -> parser.createArticleFromXml(s))
	    .map(s -> repFinder.increaseEvidence(s))
	    .map(s -> Calculator.obtainStats(s))
	    .forEach(s -> led.println(s));
	    led.close();
	    reader.closeBuffer();
	}

	/*
	 * Dump reader from XML dump. 
	 * "false" because is already in .xml format
	 */
	if(writeDBPedia){
	    System.out.println("Running for DBPedia Spotlight evaluation ... ");
	    PrintStream dbs = new PrintStream(new FileOutputStream(output_dbsp_file), false, "UTF-8");
	    reader = new XMLReader(Configuration.getInputDump50Articles(), false);
	    reader.nextChunk(Configuration.getChunkSize())
	    .stream()
	    .map(s -> parser.createArticleFromXml(s))
	    .map(s -> DBPediaSpotlight.getCompleteStats(s))
	    .forEach(s -> dbs.println(s));
	    dbs.close();
	    reader.closeBuffer();
	}

	/*
	 * EVALUATION for lectorED (=LED), DBPediaSpoltlight (=DPS)
	 * 
	 * PE
	 * precision = num(retrieved_PE) / num(gt_PE)
	 * recall = num(retrieved_PE) / 
	 * 
	 * SE
	 * 
	 * 
	 */
	if(writeStats){
	    System.out.println("Calculating and writing results ... ");
	    String gt_file = Configuration.getExperimentFolder() + "gt.tsv";
	    GT gt = new GT(gt_file);
	    Outcome lectorEDOutcome = new Outcome(output_lectorED, "LectorED");
	    Outcome dpsOutcome = new Outcome(output_dbsp_file, "DBPediaSpotlight");

	    Pair<List<String>, List<String>> lectorOutput = calcOutput(gt, lectorEDOutcome);
	    Pair<List<String>, List<String>> dpsOutput = calcOutput(gt, dpsOutcome);

	    for (String entry : lectorOutput.key){
		precWriter.print(entry+"\n");
	    }

	    for (String entry : dpsOutput.key){
		precWriter.print(entry+"\n");
	    }

	    for (String entry : lectorOutput.value){
		recallWriter.print(entry+"\n");
	    }

	    for (String entry : dpsOutput.value){
		recallWriter.print(entry+"\n");
	    }
	    recallWriter.close();
	    precWriter.close();
	}
    }

    /**
     * 
     * @param gt
     * @param outcome
     * @return
     */
    private static Pair<List<String>,List<String>> calcOutput(GT gt, Outcome outcome){
	List<String> output_precision = new ArrayList<String>();
	List<String> output_recall = new ArrayList<String>();

	for (String wikid : outcome.getWikid2peAbstractCount().keySet()){
	    /*
	     * PRIMARY ENTITY
	     */
	    // ABSTRACT
	    int retrievedPEAbstract = outcome.getWikid2peAbstractCount().get(wikid);
	    int gtPEAbstract = gt.getWikid2peAbstractCount().get(wikid);
	    Pair<Double, Double> PRPEAbstract = calcPR(retrievedPEAbstract, gtPEAbstract);

	    // BODY
	    int retrievedPEBody = outcome.getWikid2peBodyCount().get(wikid);
	    int gtPEBody = gt.getWikid2peBodyCount().get(wikid);
	    Pair<Double, Double> PRPEBody = calcPR(retrievedPEBody, gtPEBody);

	    // COMPLETE
	    int retrievedPEComplete = outcome.getWikid2peCompleteCount().get(wikid);
	    int gtPEComplete = gt.getWikid2peCompleteCount().get(wikid);
	    Pair<Double, Double> PRPEComplete = calcPR(retrievedPEComplete, gtPEComplete);

	    /*
	     * SECONDARY ENTITIES
	     */
	    // ABSTRACT
	    List<String> retrievedSEAbstract = outcome.getWikid2seAbstractList().get(wikid);
	    List<String> gtSEAbstract = gt.getWikid2seAbstractList().get(wikid);
	    Pair<Double, Double> PRSEAbstract = calcPR(retrievedSEAbstract, gtSEAbstract);

	    // BODY
	    List<String> retrievedSEBody = outcome.getWikid2seBodyList().get(wikid);
	    List<String> gtSEBody = gt.getWikid2seBodyList().get(wikid);
	    Pair<Double, Double> PRSEBody = calcPR(retrievedSEBody, gtSEBody);

	    // COMPLETE
	    List<String> retrievedSEComplete = outcome.getWikid2seCompleteList().get(wikid);
	    List<String> gtSEComplete = gt.getWikid2seCompleteList().get(wikid);
	    Pair<Double, Double> PRSEComplete = calcPR(retrievedSEComplete, gtSEComplete);

	    output_precision.add(wikid + "\t" + outcome.getName() + "\t" + gt.getWikid2domain().get(wikid) + 
		    "\t" + PRPEAbstract.key + "\t" + PRPEBody.key + "\t" + PRPEComplete.key
		    + "\t" + PRSEAbstract.key + "\t" + PRSEBody.key + "\t" + PRSEComplete.key);

	    output_recall.add(wikid + "\t" + outcome.getName() + "\t" + gt.getWikid2domain().get(wikid) + 
		    "\t" + PRPEAbstract.value + "\t" + PRPEBody.value + "\t" + PRPEComplete.value
		    + "\t" + PRSEAbstract.value + "\t" + PRSEBody.value + "\t" + PRSEComplete.value);

	}

	return Pair.make(output_precision, output_recall);
    }


    /**
     * 
     * @param retrievedPE
     * @param gtPE
     * @return
     */
    private static Pair<Double, Double> calcPR(int retrievedPE, int gtPE){
	double precision = 0.0;
	double recall = 0.0;
	if (retrievedPE != 0){
	    if (retrievedPE == gtPE){
		precision = 1.0;
		recall = 1.0;
	    }else if (retrievedPE > gtPE){
		precision = (double) gtPE / retrievedPE;
		recall = 1.0;
	    }else{
		precision = 1.0;
		recall = (double) retrievedPE / gtPE;
	    }
	}
	//System.out.println("Retrieved: " + retrievedPE + "\tGT: " + gtPE + "\tPrec: " + precision + "\tRecall: " + recall);
	return Pair.make(precision, recall);
    }

    /**
     * 
     * @param retrievedSE
     * @param gtSE
     * @return
     */
    private static Pair<Double, Double> calcPR(List<String> retrievedSE, List<String> gtSE){
	double precision = 0.0;
	double recall = 0.0;
	List<String> intersection = intersection(retrievedSE, gtSE);
	precision = (double)intersection.size() / retrievedSE.size();
	recall = (double)intersection.size() / gtSE.size();
	return Pair.make(precision, recall);
    }

    /**
     * 
     * @param listA
     * @param listB
     * @return
     */
    private static List<String> intersection(List<String> listA, List<String> listB){
	List<String> tmp = new ArrayList<String>(listA);
	List<String> result = new ArrayList<String>();
	for (String entry : listB) {
	    if(tmp.contains(entry)) {
		result.add(entry);
		tmp.remove(entry);
	    }
	}
	return result;
    }

}
