package it.uniroma3.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    private static final List<String> J_LIST = Arrays.asList("JJ", "JJR", "JJS", "VBG");
    
    private static final List<String> N_LIST = Arrays.asList("NN");
    private static final List<String> NS_LIST = Arrays.asList("NNS");
    private static final List<String> NP_LIST = Arrays.asList("NNP");
    
    private static final List<String> POS_LIST = Arrays.asList("POS");
    private static final List<String> CC_LIST = Arrays.asList("CC", ",");
    private static final List<String> CD_LIST = Arrays.asList("CD");
    private static final List<String> IN_LIST = Arrays.asList("IN");
    private static final List<String> FINAL_LIST = Arrays.asList("VB", "VBN", "VBD", "VBZ", "WP", "WDT", "WRB", "TO", "IN", ".");
    
    private FSM finiteStateMachine;
    private ExpertNLP expert;
    
    /**
     * 
     * @param expert
     */
    public SeedFSM(ExpertNLP expert){
	this.finiteStateMachine = createFSM();
	this.expert = expert;
    }
    
    
    /**
     * Method to implement a finite state machine to capture the seeds from the first sentence.
     * 
     * @return
     */
    private FSM createFSM(){
	FSM fsm = new FSM();
	for (String symbol : R_LIST){
	    fsm.addTransition("S2", symbol, "S1");
	    fsm.addTransition("S4", symbol, "S1");
	    fsm.addTransition("S3", symbol, fsm.ACCEPT);
	}
	for (String symbol : CD_LIST){
	    fsm.addTransition(fsm.START, symbol, "S8");
	    fsm.addTransition("S2", symbol, "S2");
	}
	for (String symbol : IN_LIST){
	    fsm.addTransition("S8", symbol, fsm.START);
	}
	for (String symbol : DT_LIST){
	    fsm.addTransition(fsm.START, symbol, "S2");
	    fsm.addTransition("S1", symbol, "S2");
	    fsm.addTransition("S5", symbol, "S2");
	}
	for (String symbol : J_LIST){
	    fsm.addTransition(fsm.START, symbol, "S4");
	    fsm.addTransition("S1", symbol, "S4");
	    fsm.addTransition("S2", symbol, "S4");
	    fsm.addTransition("S3", symbol, "S4");
	    fsm.addTransition("S5", symbol, "S4");
	    fsm.addTransition("S4", symbol, "S4");
	    fsm.addTransition("S6", symbol, "S4");
	    fsm.addTransition("S7", symbol, "S4");
	}
	for (String symbol : N_LIST){
	    fsm.addTransition(fsm.START, symbol, "S3");
	    fsm.addTransition("S4", symbol, "S3");
	    fsm.addTransition("S2", symbol, "S3");
	    fsm.addTransition("S3", symbol, "S3");
	    fsm.addTransition("S5", symbol, "S3");
	    fsm.addTransition("S6", symbol, "S3");
	    fsm.addTransition("S7", symbol, "S3");
	    fsm.addTransition("S9", symbol, "S3");
	}
	for (String symbol : NS_LIST){
	    fsm.addTransition("S2", symbol, "S9");
	    fsm.addTransition("S4", symbol, "S9");
	    fsm.addTransition("S5", symbol, "S9");
	    fsm.addTransition("S3", symbol, "S9");
	    fsm.addTransition("S6", symbol, "S9");
	    fsm.addTransition("S7", symbol, "S9");
	    fsm.addTransition("S9", symbol, "S9");
	}
	for (String symbol : NP_LIST){
	    fsm.addTransition(fsm.START, symbol, "S7");
	    fsm.addTransition("S5", symbol, "S7");
	    fsm.addTransition("S2", symbol, "S7");
	    fsm.addTransition("S4", symbol, "S7");
	    fsm.addTransition("S7", symbol, "S7");
	    
	}
	for (String symbol : POS_LIST){
	    fsm.addTransition("S4", symbol, "S6");
	    fsm.addTransition("S3", symbol, "S6");
	    fsm.addTransition("S7", symbol, "S6");
	    fsm.addTransition("S9", symbol, "S6");
	}
	for (String symbol : CC_LIST){
	    fsm.addTransition("S3", symbol, "S5");
	    fsm.addTransition("S3", symbol, fsm.ACCEPT);
	    fsm.addTransition("S4", symbol, "S5");
	    fsm.addTransition("S5", symbol, "S5");
	    fsm.addTransition("S9", symbol, "S5");

	}
	for (String symbol : FINAL_LIST){
	    fsm.addTransition("S1", symbol, "S4");
	    fsm.addTransition("S3", symbol, fsm.ACCEPT);
	    fsm.addTransition("S9", symbol, fsm.ACCEPT);
	}
	return fsm;
    }
    

    /**
     * Return a list of seed given the sentence.
     * 
     * @param sentence --> the first sentence of an article.
     * @return
     */
    public List<String> findSeed(String sentence){
	this.finiteStateMachine.reset();
	List<String> seeds = new LinkedList<String>();
	List<Pair<String, String>> tags = cutOutFirstPart(expert.tagSentence(sentence));
	String tmpToken = "-";
	
	for(Pair<String, String> tag : tags){
	    this.finiteStateMachine.transition(tag.value);
	    if(this.finiteStateMachine.accepts())
		seeds.add(tmpToken);
	    if(NS_LIST.contains(tag.value))
		tmpToken = expert.getSingular(tag.key, tag.value);
	    else
		tmpToken = tag.key;
	}
	
	return seeds;
    }
    

    /**
     * Remove the "subject" part of the sentence. 
     * It considers the occurrence of one of "is-a" verb in the text.
     * 
     * @param tags --> full pos-tags of the first sentence.
     * @return
     */
    private static List<Pair<String, String>> cutOutFirstPart(List<Pair<String, String>> tags){
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
