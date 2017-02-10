package it.uniroma3.entitydetection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.uniroma3.util.ExpertNLP;
import it.uniroma3.util.Pair;
/**
 * 
 * @author matteo
 *
 */
public class SeedFSM {

    private static final List<String> R_LIST = Arrays.asList("RB", "RBR", "RBS");
    private static final List<String> DT_LIST = Arrays.asList("DT");
    private static final List<String> J_LIST = Arrays.asList("JJ", "JJR", "JJS");
    private static final List<String> N_LIST = Arrays.asList("NN", "NNS");
    private static final List<String> NP_LIST = Arrays.asList("NNP");
    private static final List<String> POS_LIST = Arrays.asList("POS");
    private static final List<String> CC_LIST = Arrays.asList("CC");
    private static final List<String> FINAL_LIST = Arrays.asList(",","VB", "VBN", "VBD", "VBG", "VBZ", "WP", "WDT", "WRB", "TO", "IN", ".");
    
    private FSM finiteStateMachine;
    private ExpertNLP expert;
    
    /**
     * 
     */
    public SeedFSM(ExpertNLP expert){
	this.finiteStateMachine = createFSM();
	this.expert = expert;
    }
    
    
    /**
     * 
     * 
     * @return
     */
    private FSM createFSM(){
	FSM fsm = new FSM();
	for (String symbol : R_LIST){
	    fsm.addTransition(fsm.START, symbol, "S1");
	}
	for (String symbol : DT_LIST){
	    fsm.addTransition(fsm.START, symbol, "S2");
	    fsm.addTransition("S1", symbol, "S2");
	    fsm.addTransition("S5", symbol, "S2");
	}
	for (String symbol : J_LIST){
	    fsm.addTransition("S2", symbol, "S4");
	    fsm.addTransition("S3", symbol, "S4");
	    fsm.addTransition("S4", symbol, "S4");
	    fsm.addTransition("S7", symbol, "S4");
	}
	for (String symbol : N_LIST){
	    fsm.addTransition("S4", symbol, "S3");
	    fsm.addTransition("S2", symbol, "S3");
	    fsm.addTransition("S3", symbol, "S3");
	    fsm.addTransition("S5", symbol, "S3");
	    fsm.addTransition("S6", symbol, "S3");
	    fsm.addTransition("S7", symbol, "S3");
	}
	for (String symbol : NP_LIST){
	    fsm.addTransition("S5", symbol, "S7");
	    fsm.addTransition("S2", symbol, "S7");
	    fsm.addTransition("S4", symbol, "S7");
	    fsm.addTransition("S7", symbol, "S7");
	}
	for (String symbol : POS_LIST){
	    fsm.addTransition("S4", symbol, "S6");
	    fsm.addTransition("S3", symbol, "S6");
	}
	for (String symbol : CC_LIST){
	    fsm.addTransition("S3", symbol, "S5");
	    fsm.addTransition("S5", symbol, "S5");
	}
	for (String symbol : FINAL_LIST){
	    fsm.addTransition("S3", symbol, fsm.ACCEPT);
	}
	return fsm;
    }
    

    /**
     * 
     * @param tags
     * @return
     */
    public String findSeed(String sentence){
	this.finiteStateMachine.reset();
	List<Pair<String, String>> tags = cutSequence(expert.tagSentence(sentence));
	System.out.println(tags);
	String seed = "-";
	String tmpToken = "-";
	
	for(Pair<String, String> tag : tags){
	    this.finiteStateMachine.transition(tag.value);
	    if(this.finiteStateMachine.accepts())
		seed = tmpToken;
	    tmpToken = tag.key;
	}
	
	return seed;
    }
    

    /**
     * 
     * @param tags
     * @return
     */
    private static List<Pair<String, String>> cutSequence(List<Pair<String, String>> tags){
	List<Pair<String, String>> subtags = new ArrayList<Pair<String, String>>(tags);
	for(Pair<String, String> tag : tags){
	    if (!tag.key.equals("is") && !tag.key.equals("are") && !tag.key.equals("was") && !tag.key.equals("were")){
		subtags.remove(tag);
	    }else{
		subtags.remove(tag);
		break;
	    }
	}
	return subtags;
    }
    
  

}
