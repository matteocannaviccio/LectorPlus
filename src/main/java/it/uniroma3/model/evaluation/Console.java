package it.uniroma3.model.evaluation;
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
import java.util.Set;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.model.db.CRUD;
import it.uniroma3.model.db.DBModel;
import it.uniroma3.util.CounterMap;
import it.uniroma3.util.Ranking;

public class Console{
    // commands
    enum Command {q, h, p, pc, rc, rp, ptc, ptlf, ptuf};
    
    // usage
    private static String usage = 
	    String.format("%-20s %-65s %s\n", "[HELP]", "/h", "prints this help message").toString() +
	    String.format("%-20s %-65s %s\n", "[QUIT]", "/q", "quits").toString() +
	    String.format("%-20s %-65s %s\n", "[prediction]", "/p/<[typeSubject]>/<phrase>/<[typeObject]>", "compute naive bayes prediction").toString() +
	    String.format("%-20s %-65s %s\n", "[relation count]", "/rc/<relation>", "return the COUNT of the relation").toString() +
	    String.format("%-20s %-65s %s\n", "[relation prior]", "/rp/<relation>", "return the PRIOR of the relation").toString() +
	    String.format("%-20s %-65s %s\n", "[phrase/rel count]", "/ptc/<[typeSubject]>/<phrase>/<[typeObject]>", "return the count of typed phrase with each relation").toString();

    /**
     * 
     * @param marks
     * @return
     */
    protected static int calculateSum(CounterMap<String> marks) {
	int sum = 0;
	for (String entry : marks.keySet()){
	    sum += marks.get(entry);
	}
	return sum;
    }
    
    // entry point
    public static void main(String[] args){
	Configuration.init(new String[0]);
	Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	Configuration.updateParameter("language", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	DBModel dbmodel = new DBModel(Configuration.getDBModel());
	CRUD crud = new CRUD(dbmodel);
	
	/*****************************************************
	 * initialize console - retrieve all data from the db
	 ****************************************************/
	//ModelNB model = new ModelNB(dbmodel, "labeled_triples", 5);
	Set<String> availablePhrases = crud.getAvailablePhrases(1).keySet();
	CounterMap<String> relations = crud.getR_count(availablePhrases, true);
	Map<String, CounterMap<String>> phrase2relationCount = crud.getPtoCountedR_LT(availablePhrases, true);
	Map<String, CounterMap<String>> phrasetyped2relationCount = crud.getPTtoCountedR_LT(availablePhrases, true);
	Map<String, CounterMap<String>> phrase2typesAndRelationCountLT = crud.getPTtoTypes_LT(availablePhrases, true);
	Map<String, CounterMap<String>> phrase2typesCountUT = crud.getPTtoTypes_UT(availablePhrases, true);
	
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
		    
		case rc: //RELATION COUNT
		    String relation = token[2];
		    System.out.println(relation + "=" + relations.get(relation));
		    break;
		    
		case rp: //RELATION PRIOR
		    relation = token[2];
		    System.out.println(relation +"="+ (double)relations.get(relation)/Console.calculateSum(relations));
		    break;
		    
		case pc: //PHRASE-RELATION COUNT
		    String phrase = token[2];
		    for (Map.Entry<String, Integer> rel : Ranking.getRanking(phrase2relationCount.get(phrase)).entrySet()){
			System.out.printf("\t%-12s %-30s %s\n", rel.getValue(), rel.getKey(), relations.get(rel.getKey()));
		    }
		    break;
		    
		case ptc: //TYPED-PHRASE RELATION COUNT
		    String typeSubject = token[2];
		    phrase = token[3];
		    String typeObject = token[4];
		    CounterMap<String> res = phrasetyped2relationCount.get(typeSubject + "\t" + phrase + "\t" + typeObject);
		    System.out.println("Found: "+ res.size() + " with a total of " + calculateSum(res));
		    for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 10).entrySet()){
			System.out.printf("\t%-12s %-30s %s\n", rel.getValue(), rel.getKey());
		    }
		    break;
		    
		case ptlf: //PHRASE -> TYPED RELATION COUNT
		    phrase = token[2];
		    res = phrase2typesAndRelationCountLT.get(phrase);
		    System.out.println("Found: "+ res.size() + " with a total of " + calculateSum(res));
		    for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 10).entrySet()){
			String subjectType = rel.getKey().split("\t")[0];
			relation = rel.getKey().split("\t")[1];
			String objectType = rel.getKey().split("\t")[2];
			System.out.printf("\t%-12s %-30s %-30s %s\n", rel.getValue(), subjectType, relation, objectType);
		    }
		    break;
		    
		case ptuf: //PHRASE -> TYPES COUNT (UNLABELED)
		    phrase = token[2];
		    res = phrase2typesCountUT.get(phrase);
		    System.out.println("Found: "+ res.size() + " with a total of " + calculateSum(res));
		    for (Map.Entry<String, Integer> rel : Ranking.getTopKRanking(res, 10).entrySet()){
			String subjectType = rel.getKey().split("\t")[0];
			String objectType = rel.getKey().split("\t")[1];
			System.out.printf("\t%-12s %-30s %s\n", rel.getValue(), subjectType, objectType);
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
