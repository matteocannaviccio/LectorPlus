package it.uniroma3.entitydetection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.MultiValueMap;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.util.ExpertNLP;
import it.uniroma3.util.Reader;
import it.uniroma3.util.Token;
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
    private static final List<String> NP_LIST = Arrays.asList("NNP", "NNPS");

    private static final List<String> POS_LIST = Arrays.asList("POS");
    private static final List<String> CC_LIST = Arrays.asList("CC", ",");
    private static final List<String> CD_LIST = Arrays.asList("CD");
    private static final List<String> IN_LIST = Arrays.asList("IN");
    private static final List<String> FINAL_LIST = Arrays.asList("VBG", "JJ","CC", ",", "VB", "VBN", "VBD", "VBZ", "WP", "WDT", "WRB", "TO", "IN", ".");

    private FSM finiteStateMachine;
    private ExpertNLP expert;
    private Set<String> stopwords; // we need them only for a post-processing filetering

    /**
     * 
     * @param expert
     */
    public SeedFSM(ExpertNLP expert){
	this.finiteStateMachine = createFSM();
	this.expert = expert;
	this.stopwords = Reader.getLines(Configuration.getStopwordsList());
    }


    /**
     * Method to implement a finite state machine to capture the seeds from the first sentence.
     * S1 -> RR state
     * S2 -> numerical state
     * S3 -> noun state
     * S4 -> adjective state
     * S5 -> conjunction state
     * S6 -> pos state
     * S7 -> proper noun state
     * S8 -> in state
     * S9 -> plural nouns
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
	    fsm.addTransition("S2", symbol, "S3");
	    fsm.addTransition("S3", symbol, "S3");
	    fsm.addTransition("S4", symbol, "S3");
	    fsm.addTransition("S5", symbol, "S3");
	    fsm.addTransition("S6", symbol, "S3");
	    fsm.addTransition("S7", symbol, "S3");
	    fsm.addTransition("S9", symbol, "S3");
	}
	for (String symbol : NS_LIST){
	    fsm.addTransition(fsm.START, symbol, "S9");
	    fsm.addTransition("S2", symbol, "S9");
	    fsm.addTransition("S3", symbol, "S9");
	    fsm.addTransition("S4", symbol, "S9");
	    fsm.addTransition("S5", symbol, "S9");
	    fsm.addTransition("S6", symbol, "S9");
	    fsm.addTransition("S7", symbol, "S9");
	    fsm.addTransition("S9", symbol, "S9");
	}
	for (String symbol : NP_LIST){
	    fsm.addTransition(fsm.START, symbol, "S7");
	    fsm.addTransition("S2", symbol, "S7");
	    fsm.addTransition("S3", symbol, "S7");
	    fsm.addTransition("S4", symbol, "S7");
	    fsm.addTransition("S5", symbol, "S7");
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
	Token[] tokens = cutOutFirstPart(expert.tagFirstSentence(sentence));
	String tmpToken = "-";
	for(Token token : tokens){
	    this.finiteStateMachine.transition(token.getPOS());
	    if(this.finiteStateMachine.accepts())
		seeds.add(tmpToken);
	    if(NS_LIST.contains(token.getPOS()))
		tmpToken = expert.getSingular(token.getRenderedToken(), token.getPOS());
	    else
		tmpToken = token.getRenderedToken();
	}
	return cleanSeeds(seeds);
    }

    /**
     * Remove the "subject" part of the sentence. 
     * It considers the occurrence of one of "is-a" verb in the text.
     * 
     * @param tags --> full pos-tags of the first sentence.
     * @return
     */
    private static Token[] cutOutFirstPart(Token[] tokens){
	int initialToken=0;
	for(int i = 0; i<tokens.length; i++){
	    String word = tokens[i].getRenderedToken();
	    if (word.equals("is") || word.equals("are") || word.equals("was") || word.equals("were")){
		initialToken = i+1;
		break;
	    }
	}
	return Arrays.copyOfRange(tokens, initialToken, tokens.length);
    }
    
    /**
     * Clean the list of retrieved seeds from (improbable) stopwords.
     * @param seeds
     * @return
     */
    private List<String> cleanSeeds(List<String> seeds){
	List<String> filteredSeeds = new ArrayList<String>(seeds.size());
	for (String seed : seeds){
	    if (!stopwords.contains(seed))
		filteredSeeds.add(seed);
	}
	return filteredSeeds;
    }

    /**
     * 
     * @author matteo
     *
     */
    public class FSM{
	public final String START = "-";
	public final String ACCEPT = "*1";
	public MultiValueMap transitions; // this is a map that allows for repeated keys
	public Set<String> states;

	/**
	 * Creates a new FSM.
	 * 
	 */
	public FSM(){
	    transitions = new MultiValueMap();
	    states = new TreeSet<String>();
	    reset();
	}

	/**
	 * Initializes an empty FSM.
	 * 
	 */
	public void reset(){
	    states.clear();
	    states.add(START);
	}

	/**
	 * Adds a transition (arrow) to our FSM.
	 * 
	 * @param from
	 * @param symbol
	 * @param to
	 */
	public void addTransition(String from, String symbol, String to){
	    transitions.put(from + " + " + symbol, to);
	}

	/**
	 * The FSM is complete if states contains a terminal state.
	 * 
	 * @return
	 */
	public boolean accepts(){
	    boolean isInAcceptanceState = states.contains(ACCEPT);
	    if(isInAcceptanceState)
		states.remove(ACCEPT);
	    return isInAcceptanceState;
	}

	/**
	 * Changes the states of the FSM given the symbol in input.
	 * 
	 * @param symbol
	 */
	public void transition(String symbol){
	    Set<String> newState = new TreeSet<String>();
	    for (String s1 : states){
		@SuppressWarnings("unchecked")
		List<String> t = (List<String>) transitions.get(s1+" + "+symbol);
		if (t != null)
		    newState.addAll(t);
	    }
	    states = newState;
	}
    }
  

}
