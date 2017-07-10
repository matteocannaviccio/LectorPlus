package it.uniroma3.model.model;
/**
 * 
 * @author matteo
 *
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.util.Ranking;

public class Console{
    // commands
    enum Command {q, h, p, rc, rp, ptc};

    // usage
    private static String usage = 
	    String.format("%-20s %-65s %s\n", "[HELP]", "/h", "prints this help message").toString() +
	    String.format("%-20s %-65s %s\n", "[QUIT]", "/q", "quits").toString() +
	    String.format("%-20s %-65s %s\n", "[prediction]", "/p/<[typeSubject]>/<phrase>/<[typeObject]>", "compute naive bayes prediction").toString() +
	    String.format("%-20s %-65s %s\n", "[relation count]", "/rc/<relation>", "return the COUNT of the relation").toString() +
	    String.format("%-20s %-65s %s\n", "[relation prior]", "/rp/<relation>", "return the PRIOR of the relation").toString() +
	    String.format("%-20s %-65s %s\n", "[phrase/rel count]", "/ptc/<[typeSubject]>/<phrase>/<[typeObject]>", "return the count of typed phrase with each relation").toString();

    // entry point
    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));

	DBModel dbmodel = new DBModel(Configuration.getDBModel());
	ModelNB model = new ModelNB(dbmodel, "labeled_triples", 5);
	boolean running = true;
	String[] token = null;

	System.out.println("type /h for help; /q to quit\n");
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	while (running){
	    try {
		System.out.print("> ");
		String line = br.readLine();

		token = line.split("/");

		Command cmd = Command.valueOf(token[1]);

		switch(cmd){
		case q: // QUIT
		    running = false;
		    break;

		case h: // HELP
		    System.out.println(usage);
		    break;

		case p: //PREDICTION
		    String typeSubject = token[2];
		    String phrase = token[3];
		    String typeObject = token[4];
		    model.predictRelationList(typeSubject, phrase, typeObject);
		    break;
		    
		case rc: //RELATION COUNT
		    String relation = token[2];
		    System.out.println(model.getRCount(relation));
		    break;
		    
		case rp: //RELATION PRIOR
		    relation = token[2];
		    System.out.println(model.calculatePrior(relation));
		    break;
		    
		case ptc: //PHRASE RELATION COUNT
		    typeSubject = token[2];
		    phrase = token[3];
		    typeObject = token[4];
		    for (Map.Entry<String, Integer> rel : Ranking.getRanking(model.getPTtoRCount(phrase, typeSubject, typeObject)).entrySet()){
			System.out.printf("\t%-10s %-12s %-10s %s\n", rel.getValue(), rel.getKey(), model.getRCount(rel.getKey()), "-->" + (double) rel.getValue()/model.getRCount(rel.getKey()));
		    }
		    break;
		   
		
		default:
		    System.out.println("unrecognized command /'" + cmd + "'/. try again.");
		}
	    }

	    catch (java.lang.IllegalArgumentException e){
		e.printStackTrace();
		System.out.println("unrecognized command /'" + token[1] + "'/. try again.");
	    }
	    catch (Exception e) {
		e.printStackTrace();
		System.out.println("ERROR! try again.");
	    }		
	}
    }

}
