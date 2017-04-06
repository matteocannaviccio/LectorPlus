package it.uniroma3.triples;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import it.uniroma3.configuration.Configuration;
/**
 * 
 * @author matteo
 *
 */
public class WitnessCounter {

    private Map<String, Map<String, Integer>> witCntRelationsPhrases;
    private Map<String, Integer[]> witCntPhrases;

    /**
     * 
     */
    public WitnessCounter(){
	this.witCntRelationsPhrases = new HashMap<String, Map<String, Integer>>();
	this.witCntPhrases = new HashMap<String, Integer[]>();
    }

    /**
     * @return the witCntRelationsPhrases
     */
    public Map<String, Map<String, Integer>> getWitCntRelationsPhrases() {
	return witCntRelationsPhrases;
    }

    /**
     * @return the witCntPhrases
     */
    public Map<String, Integer[]> getWitCntPhrases() {
	return witCntPhrases;
    }

    /**
     * Increments the witness counts when we get a phrase alone.
     * 
     * witCntPhrases => ( P ; [count P with a R, count P without any R])
     * 
     * @param phrase
     */
    public void newPhraseAlone(String phrase){
	if (!this.witCntPhrases.containsKey(phrase))
	    this.witCntPhrases.put(phrase, new Integer[]{0,0});
	this.witCntPhrases.get(phrase)[1] += 1;
    }

    /**
     * Increments the witness counts when we get a phrase and a relation.
     * 
     * witCntPhrasesRelations => ( P ; ( R ; [count P with R]))
     * witCntRelationsPhrases => ( R ; ( P ; [count P with R]))
     * 
     * @param phrase
     * @param relation
     */
    public void newPhraseAndRelation(String phrase, String relation){

	if (!this.witCntPhrases.containsKey(phrase))
	    this.witCntPhrases.put(phrase, new Integer[]{0,0});

	if (!this.witCntRelationsPhrases.containsKey(relation))
	    this.witCntRelationsPhrases.put(relation, new HashMap<String, Integer>());
	if (!this.witCntRelationsPhrases.get(relation).containsKey(phrase))
	    this.witCntRelationsPhrases.get(relation).put(phrase, 0);

	this.witCntPhrases.get(phrase)[0] += 1;
	this.witCntRelationsPhrases.get(relation).put(phrase, this.witCntRelationsPhrases.get(relation).get(phrase) + 1);
    }

    /**
     * It will print the following file:
     * relation	\t phrase \t isRelational \t isNotRelational \t wtnCount \t wtnCountOtherP \t wtnCountOtherR
     * 
     * @throws IOException 
     */
    public void printStatistics() throws IOException {

	Map<String, Integer> relationCount = new HashMap<String, Integer>();
	for (Map.Entry<String, Map<String, Integer>> relation : this.witCntRelationsPhrases.entrySet()){
	    int count = 0;
	    for (Map.Entry<String, Integer> phraseAndCount : relation.getValue().entrySet())
		count += phraseAndCount.getValue();
	    relationCount.put(relation.getKey(), count);
	}

	//col[0] -> relation
	//col[1] -> phrase
	//col[2] -> number of relational instances
	//col[3] -> number of not-relational instances
	//col[4] -> number of positive instances
	//col[5] -> number of negative instances (by relation) 
	//col[6] -> number of negative instances (by phrase)

	BufferedWriter buff = new BufferedWriter(new FileWriter(Configuration.getStatisticsFile()));
	for (String relation : this.witCntRelationsPhrases.keySet()){
	    for (String phrase : this.witCntRelationsPhrases.get(relation).keySet()){
		
		int relInst = this.witCntPhrases.get(phrase)[0];
		int unrelInst = this.witCntPhrases.get(phrase)[1];
		int posInst = this.witCntRelationsPhrases.get(relation).get(phrase);
		int posInstRel = relationCount.get(relation) - posInst;
		int posInstPhrase = relInst - posInst;

		buff.write(relation + "\t" + 
			phrase + "\t" + 
			relInst + "\t" + 
			unrelInst+ "\t" + 
			posInst + "\t" +
			posInstRel + "\t" + 
			posInstPhrase);
		buff.write("\n");
	    }
	}
	buff.close();
    }



}
