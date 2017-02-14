package it.uniroma3.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.uniroma3.entitydetection.SeedFSM;
import it.uniroma3.util.ExpertNLP;
/**
 * 
 * @author matteo
 *
 */
public class SeedFSMTest {
    
    /**
     * 
     * @return
     * @throws IOException
     */
    public static Map<String, String> getSentencesFromFile(String path) throws IOException{
	Map<String, String> sentences = new HashMap<String, String>();
	BufferedReader br = new BufferedReader(new FileReader(new File(path)));
	String line;
	while((line = br.readLine()) != null){
	    try{
	    sentences.put(line.split("\t")[0], line.split("\t")[1]);
	    }catch(Exception e){
		System.out.println(line);
	    }
	}
	br.close();
	return sentences;

    }
    
    
    public static void main(String[] args) throws IOException {
	Map<String, String> sentences = getSentencesFromFile("/Users/matteo/Work/Repository/ualberta/lectorplus/groundtruths/first_sentence/en_person.tsv");
	
	SeedFSM fsm = new SeedFSM(new ExpertNLP());

	for (Map.Entry<String, String> sentence : sentences.entrySet()){
	    System.out.println(fsm.findSeed(sentence.getValue()) + "\t" + sentence.getValue());
	}
 
   	
       }


}
