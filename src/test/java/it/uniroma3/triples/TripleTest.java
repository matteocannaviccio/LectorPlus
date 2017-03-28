package it.uniroma3.triples;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;

public class TripleTest {
    
    private static Triple tripleNER1;
    private static Triple tripleNER2;
    private static Triple tripleNER3;
    private static Triple tripleJoinable1;
    private static Triple tripleJoinableWrong;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init("/Users/matteo/Desktop/data/config.properties");
    }
    
    @Before
    public void setup() {
        tripleNER1 = new Triple("some pre", "<PE-ORG<Italy>>", "a phrase", "<LOCATION<Bassano del Grappa>>", "and a post.");
        tripleNER2 = new Triple("some pre", "<PERSON<Franco Battiato>>", "a phrase", "<LOCATION<Italy>>", "and a post.");
        tripleNER3 = new Triple("some pre", "<PERSON<Franco Battiato>>", "a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinable1 = new Triple("some pre", "<PE-ORG<Franco_Battiato>>", "a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinableWrong = new Triple("some pre", "<PE-ORG<Franco_Battiato>>", "a phrase", "<SE-AUG<Italy>>", "'s author.");
    }
    
    @Test
    public void isTypeTripleTest() {
	assertTrue(tripleNER1.isNERTriple());
	assertFalse(tripleNER1.isJoinableTriple());
	assertFalse(tripleNER2.isMVTriple());
	assertFalse(tripleNER3.isJoinableTriple());
	assertTrue(tripleJoinable1.isJoinableTriple());
	assertTrue(tripleJoinableWrong.isJoinableTriple());
    }

}
