package it.uniroma3.triples;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.triples.WikiTriple;

public class TriplifierTest {


    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	// force english
	Configuration.keyValue.put("languageUsed", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }

    @Test
    public void createTriplesTest_1(){
	// correct pair of triples
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve, which <PE-PRON<Paul_Desmond>> continued while at <SE-ORG<San_Francisco_Polytechnic_High_School>>.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	assertEquals(triples.size(), 2);
    }
    
    @Test
    public void createTriplesTest_2(){
	// "'s" after the object --> no triple!
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve, which <PE-PRON<Paul_Desmond>> 's siter.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	assertEquals(triples.size(), 0);
    }
    
    
    @Test
    public void createTriplesTest_3(){
	// "that" before the object --> no triple!
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve that <PE-PRON<Paul_Desmond>> continued while at <SE-ORG<San_Francisco_Polytechnic_High_School>>.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	assertEquals(triples.size(), 1);
    }
}
