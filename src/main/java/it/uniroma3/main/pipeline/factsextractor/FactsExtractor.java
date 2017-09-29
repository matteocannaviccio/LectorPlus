package it.uniroma3.main.pipeline.factsextractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.uniroma3.config.Configuration;
import it.uniroma3.config.Lector;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.bean.WikiTriple.TType;
import it.uniroma3.main.pipeline.triplesextractor.placeholders.PlaceholderFilter;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.Ranking;
import it.uniroma3.main.util.inout.ResultsWriterWrapper;
import it.uniroma3.main.util.inout.ntriples.NTriplesWriter;
import it.uniroma3.model.model.Model;
/**
 * 
 * @author matteo
 *
 */
public class FactsExtractor {

    private Model model;
    private int extracted_facts;

    /* WRITER */
    private NTriplesWriter writer_facts;
    private ResultsWriterWrapper writer_provenance;

    /* WRITER Stats*/
    private BufferedWriter relations_stats_writer;
    private BufferedWriter typed_phrases_stats_writer;
    private BufferedWriter phrases_stats_writer;

    /* STATS*/
    private Map<String, Integer> relations_stats;
    private Map<String, Integer> phrases_stats;
    private Map<String, Integer> typed_phrases_stats;
    
    private PlaceholderFilter placeholderFilter;

    /**
     * 
     * @param model
     * @param db_write
     */
    public FactsExtractor(Model model){
	this.model = model;

	this.relations_stats = new ConcurrentHashMap<String, Integer>();
	this.phrases_stats = new ConcurrentHashMap<String, Integer>();
	this.typed_phrases_stats = new ConcurrentHashMap<String, Integer>();

	this.writer_facts = new NTriplesWriter(Configuration.getOutputFactsFile(model.getName()));
	this.writer_provenance = new ResultsWriterWrapper(Configuration.getProvenanceFile(model.getName()));
	
	placeholderFilter = PlaceholderFilter.getPlaceholderFilter();
    }


    /**
     * Process the triple to label.
     * It can not have the same entities as subject and object. 
     * Return a true value if we can extract a new facts, false otherwise.
     * 
     * @param t
     * @return
     */
    private void processRecord(WikiTriple t){
	Pair<String, Double> prediction = model.predict(t.getSubjectType(), t.getPhrasePlaceholders(), t.getObjectType());

	// assign a relation
	String relation = prediction.key;
	
	// if there is ...
	if (Model.isPositivePrediction(relation)){
	    
	    if (!relations_stats.containsKey(relation))
		relations_stats.put(relation, 0);
	    relations_stats.put(relation, relations_stats.get(relation)+1);

	    if (!phrases_stats.containsKey(t.getPhrasePlaceholders()))
		phrases_stats.put(t.getPhrasePlaceholders(), 0);
	    phrases_stats.put(t.getPhrasePlaceholders(), phrases_stats.get(t.getPhrasePlaceholders())+1);

	    String tyPRLine = t.getSubjectType() +"\t"+ t.getPhrasePlaceholders() +"\t"+ t.getObjectType() + "\t" + relation;
	    if (!typed_phrases_stats.containsKey(tyPRLine))
		typed_phrases_stats.put(tyPRLine, 0);
	    typed_phrases_stats.put(tyPRLine, typed_phrases_stats.get(tyPRLine)+1);

	    this.extracted_facts+=1;
	    writeToFile(t, relation);
	}
    }

    /**
     * 
     * @param t
     * @param relation
     */
    private void writeToFile(WikiTriple t, String relation){
	String typedphrase = t.getSubjectType() +" "+ t.getPhrasePlaceholders() +" "+ t.getObjectType();
	if (!Lector.getDBPedia().getRelations(t.getWikiSubject(), t.getWikiObject()).equals(relation)){
	    writer_provenance.provenance(t.getWikid(), t.getSection(), typedphrase, t.getWholeSentence(), t.getSubject(), t.getWikiSubject(), relation, t.getObject(), t.getWikiObject());
	    if (relation.contains("(-1)")){
		writer_facts.write(t.getWikiObject(), relation.replace("(-1)", ""), t.getWikiSubject());
	    }else{
		writer_facts.write(t.getWikiSubject(), relation, t.getWikiObject());
	    }
	}
    }

    /**
     * 
     * @return
     */
    public void runExtractionFacts(int max){
	int cont = 0;
	String allUnlabeledTriplesQuery = "SELECT * FROM unlabeled_triples";
	try (PreparedStatement stmt = Lector.getDbmodel(false).getConnection().prepareStatement(allUnlabeledTriplesQuery)){
	    try (ResultSet rs = stmt.executeQuery()){
		while(rs.next() && cont < max){
		    cont +=1;
		    String wikid = rs.getString(1);
		    String section = rs.getString(2);
		    String sentence = rs.getString(3);
		    String phrase_original = rs.getString(4);
		    String phrase_placeholder = rs.getString(5);
		    String pre = rs.getString(6);
		    String post = rs.getString(7);
		    String subject = rs.getString(8);
		    String subject_type = rs.getString(10);
		    String object = rs.getString(11);
		    String object_type = rs.getString(13);

		    WikiTriple t = new WikiTriple(wikid, section, sentence, phrase_original, phrase_placeholder, pre, post, 
			    subject, object, subject_type, object_type, TType.JOINABLE.name());
		    if (!t.getWikiSubject().equals(t.getWikiObject()) && !placeholderFilter.replace(phrase_original).equals("CONJUNCTION"))
			processRecord(t);
		}
	    }
	}catch(SQLException e){
	    e.printStackTrace();
	}
    }

    /**
     * 
     * @param toWrite
     * @param writer
     */
    private void writeStats(Map<String, Integer> toWrite, BufferedWriter writer) {
	StringBuffer sb = new StringBuffer();
	for (Map.Entry<String, Integer> entry : Ranking.getRanking(toWrite).entrySet()){
	    sb.append(entry.getValue() + "\t" + entry.getKey() + "\n");
	}
	try {
	    writer.write(sb.toString());
	    writer.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


    /**
     * 
     */
    public void runExtractOnFile(int limit){
	runExtractionFacts(limit);
	
	// write stats ...
	try {
	    this.relations_stats_writer = new BufferedWriter(new FileWriter(new File(Configuration.getOutputRelationsStatsFile(model.getName()))));
	    this.phrases_stats_writer = new BufferedWriter(new FileWriter(new File(Configuration.getOutputPhrasesStatsFile(model.getName()))));
	    this.typed_phrases_stats_writer = new BufferedWriter(new FileWriter(new File(Configuration.getOutputTypedPhrasesStatsFile(model.getName()))));
	    writeStats(relations_stats, relations_stats_writer);
	    writeStats(phrases_stats, phrases_stats_writer);
	    writeStats(typed_phrases_stats, typed_phrases_stats_writer);
	} catch (IOException e) {
	    e.printStackTrace();
	}

	// close the output stream
	try {
	    writer_facts.done();
	    writer_provenance.done();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	System.out.println("\nFacts extraction");
	System.out.println("-----------------");
	System.out.printf("\t%-20s %-20s %-20s %-20s %s\n", "MODEL: " + model.getName(), " - FACTS: " + this.extracted_facts, " - RELATIONS: " + relations_stats.size(), " - PHRASES: " +phrases_stats.size(), " - TYPED-PHRASES: " +typed_phrases_stats.size());
    }


}
