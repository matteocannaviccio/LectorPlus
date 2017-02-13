package it.uniroma3.entitydetection;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.map.MultiValueMap;
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