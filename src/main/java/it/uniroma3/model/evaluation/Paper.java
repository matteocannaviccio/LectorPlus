package it.uniroma3.model.evaluation;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiLanguage;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.kg.DBPedia;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.io.Compressed;
import it.uniroma3.model.db.CRUD;
import it.uniroma3.model.db.DBModel;

public class Paper {

    private CRUD crud;

    /**
     * 
     * @param db
     */
    public Paper(DBModel db){
	this.crud = new CRUD(db);
    }

    /**
     * 
     */
    private void printLabeledInfo(){
	List<Pair<WikiTriple, String>> allLabeled = crud.selectAllLabeled();

	// get all the triples by relation
	CounterMap<String> relation2counts = new CounterMap<String>();
	for (Pair<WikiTriple, String> pair : allLabeled){
	    String label = pair.value.replace("(-1)", "");
	    relation2counts.add(label);
	}

	// get all the entities by pattern method detection
	CounterMap<String> metEntDet = new CounterMap<String>();
	for (Pair<WikiTriple, String> pair : allLabeled){
	    String subjectEntity = pair.key.getSubject();
	    String objectEntity = pair.key.getObject();
	    metEntDet.add(extractMethodFromEntity(subjectEntity));
	    metEntDet.add(extractMethodFromEntity(objectEntity));
	}

	System.out.printf("\t%-35s %s\n", "Total Labeled: ", allLabeled.size());
	System.out.printf("\t%-35s %s\n", "Top-10 Relations labeled: ", Ranking.getTopKRanking(relation2counts, 10));
	System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ", Ranking.getRanking(metEntDet));

    }

    /**
     * 
     */
    private void printUnlabeledInfo(){
	CounterMap<String> allUnlabeled = crud.selectAllUnlabeled();
	System.out.printf("\t%-35s %s\n", "Total Unlabeled: ", calculateSum(allUnlabeled)/2);
	System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ", Ranking.getRanking(allUnlabeled));
    }

    /**
     * 
     */
    private void printMVLInfo(){
	List<String> allMVL = crud.selectAllMVL();
	/*
	for (String mvl : allMVL){
	    String[] fields = mvl.split("\t");
	    String wikid = fields[0];
	    String section = fields[1];
	    String list = fields[2];
	    String wikidType = t.getType(wikid);

	    System.out.print(wikid + "\t" + wikidType + "\t" + section + "\t");
	    for (String en : list.split("(?<=\\>),(?=\\<)")){
		en = extractNameFromEntity(en);
		System.out.print(en + t.getType(en)  + "\t");
	    }
	    System.out.print("\n");
	}
	 */
	System.out.printf("\t%-35s %s\n", "Total MVL: ", allMVL.size());
    }

    /**
     * 
     */
    private void printOtherInfo(){
	List<WikiTriple> allOther = crud.selectAllOther();

	// get all the entities by pattern method detection
	CounterMap<String> metEntDet = new CounterMap<String>();
	for (WikiTriple t : allOther){
	    String subjectEntity = t.getSubject();
	    String objectEntity = t.getObject();
	    metEntDet.add(extractMethodFromEntity(subjectEntity));
	    metEntDet.add(extractMethodFromEntity(objectEntity));
	}
	System.out.printf("\t%-35s %s\n", "Total Other: ", allOther.size());
	System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ", Ranking.getRanking(metEntDet));
    }

    /**
     * 
     * @param entity
     * @return
     */
    private String extractMethodFromEntity(String entity){
	Pattern ENMETDET = Pattern.compile("^<([^<]*?)<([^>]*?)>>$");
	Matcher m = ENMETDET.matcher(entity);
	String method = null;
	if(m.find()){
	    method = m.group(1);
	}
	return method;
    }

    /**
     * 
     * @param entity
     * @return
     */
    private String extractNameFromEntity(String entity){
	Pattern ENMETDET = Pattern.compile("^<([^<]*?)<([^>]*?)>>$");
	Matcher m = ENMETDET.matcher(entity);
	String method = null;
	if(m.find()){
	    method = m.group(2);
	}
	return method;
    }

    /**
     * 
     * @param marks
     * @return
     */
    protected int calculateSum(CounterMap<String> marks) {
	int sum = 0;
	for (String entry : marks.keySet()){
	    sum += marks.get(entry);
	}
	return sum;
    }

    /**
     * 
     * @param entity
     * @return
     */
    private String extractPESEFromEntity(String entity){
	Pattern ENMETDET = Pattern.compile("^<(PE|SE)-([^<]*?)<([^>]*?)>>$");
	Matcher m = ENMETDET.matcher(entity);
	String method = null;
	if(m.find()){
	    method = m.group(1);
	}
	return method;
    }

    /**
     * 
     */
    private void printExtractedFactsInfo(){
	CounterMap<String> wikidWithFacts = new CounterMap<String>();
	int abstractSection = 0;
	int otherSection = 0;
	CounterMap<String> relation2counts = new CounterMap<String>();
	CounterMap<String> metEntDet = new CounterMap<String>();
	CounterMap<String> kindPairs = new CounterMap<String>();

	try {
	    BufferedReader reader = Compressed.getBufferedReaderForCompressedFile(Configuration.getProvenanceFile());
	    String line;
	    while((line = reader.readLine())!= null){
		try{
		    String[] fields = line.split("\t");
		    String wikid = fields[0];
		    String section = fields[1];
		    String relation = fields[2];
		    String kindsubject = extractPESEFromEntity(fields[3]);
		    String kindobject = extractPESEFromEntity(fields[5]);
		    String subjectEntityMethod = extractMethodFromEntity(fields[3]);
		    String objectEntityMethod = extractMethodFromEntity(fields[5]);
		    //String sentence = fields[7];
		    if (section.equals("#Abstract"))
			abstractSection +=1;
		    else
			otherSection +=1;
		    wikidWithFacts.add(wikid);
		    relation2counts.add(relation);
		    metEntDet.add(subjectEntityMethod);
		    metEntDet.add(objectEntityMethod);
		    kindPairs.add(kindsubject+"-"+kindobject);
		}catch(Exception e){
		    continue;
		}
	    }
	    reader.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	System.out.printf("\t%-35s %s\n", "-------", "");
	System.out.printf("\t%-35s %s\n", "Total Facts: ", abstractSection + otherSection);
	System.out.printf("\t%-35s %s\n", "in #Abstract: ", abstractSection);
	System.out.printf("\t%-35s %s\n", "elsewhere: ", otherSection);
	System.out.printf("\t%-35s %s\n", "Different relations: ", relation2counts.size());
	System.out.printf("\t%-35s %s\n", "Top-10 Relations with Facts: ", Ranking.getTopKRanking(relation2counts, 10));
	System.out.printf("\t%-35s %s\n", "Count Entity Detection Methods: ", Ranking.getRanking(metEntDet));
	System.out.printf("\t%-35s %s\n", "Kind of Pairs: ", kindPairs);
    }



    /**
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException{
	Configuration.init(args);
	//Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");
	for (String lang : Configuration.getLanguages()){
	    Configuration.updateParameter("language", lang);
	    Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()), 
		    new HashSet<String>(Arrays.asList(new String[]{"FE"})));
	    Paper paper = new Paper(new DBModel(Configuration.getDBModel()));
	    System.out.println(Configuration.getDBModel());
	    System.out.println("Stats for language : " + Configuration.getLanguageCode());
	    System.out.println("------------------");
	    paper.printLabeledInfo();
	    paper.printUnlabeledInfo();
	    //paper.printOtherInfo();
	    paper.printMVLInfo();
	    paper.printExtractedFactsInfo();
	    System.out.println("----------");
	    Lector.close();
	}

    }
}
