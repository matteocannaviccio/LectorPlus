package it.uniroma3.triples;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiArticle;
import it.uniroma3.model.WikiLanguage;

public class TriplifierTest {


    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }

    @Test
    public void createTriplesTest_1(){
	System.out.println("Test_1");
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve, which <PE-PRON<Paul_Desmond>> continued while at <SE-ORG<San_Francisco_Polytechnic_High_School>>.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	triples.stream().forEach(System.out::println);
	System.out.println("---");
    }
    
    @Test
    public void createTriplesTest_2(){
	System.out.println("Test_2");
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve, which <PE-PRON<Paul_Desmond>> 's siter.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	triples.stream().forEach(System.out::println);
	System.out.println("---");
    }
    
    
    @Test
    public void createTriplesTest_3(){
	System.out.println("Test_3");
	String input = "<PE-SUBTITLE<Paul_Desmond>> began to study clarinet at the age of twelve that <PE-PRON<Paul_Desmond>> continued while at <SE-ORG<San_Francisco_Polytechnic_High_School>>.";
	List<WikiTriple> triples = Lector.getTriplifier().createTriples(WikiArticle.makeDummyArticle(), input);
	triples.stream().forEach(System.out::println);
	System.out.println("---");
    }
}
