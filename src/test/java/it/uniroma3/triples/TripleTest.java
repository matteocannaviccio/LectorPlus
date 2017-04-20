package it.uniroma3.triples;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;
import it.uniroma3.model.WikiLanguage;
import it.uniroma3.triples.WikiTriple.TType;

public class TripleTest {
    
    private static WikiTriple tripleNER1;
    private static WikiTriple tripleNER2;
    private static WikiTriple tripleNER3;
    private static WikiTriple tripleJoinable1;
    private static WikiTriple tripleJoinableWrong;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Before
    public void setup() {
        tripleNER1 = new WikiTriple("wiki", "some pre", "<PE-ORG<Italy>>", "a phrase", "<LOCATION<Bassano del Grappa>>", "and a post.");
        tripleNER2 = new WikiTriple("wiki", "some pre", "<PERSON<Franco Battiato>>", "a phrase", "<LOCATION<Italy>>", "and a post.");
        tripleNER3 = new WikiTriple("wiki", "some pre", "<PERSON<Franco Battiato>>", "a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinable1 = new WikiTriple("wiki", "some pre", "<PE-ORG<Franco_Battiato>>", "a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinableWrong = new WikiTriple("wiki", "some pre", "<PE-ORG<Franco_Battiato>>", "a phrase", "<SE-AUG<Italy>>", "'s author.");
    }
    
    @Test
    public void isTypeTripleTest() {
	assertEquals(tripleNER1.getType(), TType.OBJNER);
	assertEquals(tripleNER2.getType(), TType.BOTHNER);
	assertEquals(tripleNER3.getType(), TType.SBJNER);
	assertEquals(tripleJoinable1.getType(), TType.JOINABLE);
	assertEquals(tripleJoinableWrong.getType(), TType.JOINABLE);
    }

}
