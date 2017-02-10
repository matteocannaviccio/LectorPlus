package it.uniroma3.entitydetection;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
/**
 * 
 * @author matteo
 *
 */
public class FSM{
    
    public final String START = "-";
    public final String ACCEPT = "*1";
    public Map<String, String> transitions;
    public Set<String> states;

    /**
     * Creates a new FSM.
     * 
     */
    public FSM(){
	transitions = new TreeMap<String, String>();
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
	return states.contains(ACCEPT);
    }

    /**
     * Changes the states of the FSM given the symbol in input.
     * 
     * @param symbol
     */
    public void transition(String symbol){
	Set<String> newState = new TreeSet<String>();
	for (String s1 : states){
	    String t = transitions.get(s1+" + "+symbol);
	    if (t != null)
		newState.add(t);
	}
	states = newState;
    }
}