package it.uniroma3.triples;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage;
import it.uniroma3.extractor.triples.WikiTriple;
import it.uniroma3.extractor.triples.WikiTriple.TType;

public class TripleTest {
    
    private static WikiTriple tripleNER1;
    private static WikiTriple tripleNER2;
    private static WikiTriple tripleNER3;
    private static WikiTriple tripleJoinable1;
    private static WikiTriple tripleJoinableWrong;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	// force english
	Configuration.keyValue.put("languageUsed", "en");
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }
    
    @Before
    public void setup() {
        tripleNER1 = new WikiTriple("wiki", "some pre", "<PE-ORG<Italy>>", "a phrase", "a phrase",  "<LOCATION<Bassano del Grappa>>", "and a post.");
        tripleNER2 = new WikiTriple("wiki", "some pre", "<PERSON<Franco Battiato>>", "a phrase", "a phrase", "<LOCATION<Italy>>", "and a post.");
        tripleNER3 = new WikiTriple("wiki", "some pre", "<PERSON<Franco Battiato>>", "a phrase","a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinable1 = new WikiTriple("wiki", "some pre", "<PE-ORG<Franco_Battiato>>", "a phrase","a phrase", "<SE-AUG<Italy>>", "and a post.");
        tripleJoinableWrong = new WikiTriple("wiki", "some pre", "<PE-ORG<Franco_Battiato>>", "a phrase", "a phrase", "<SE-AUG<Italy>>", "'s author.");
    }
    
    @Test
    public void isTypeTripleTest() {
	assertEquals(tripleNER1.getType(), TType.NER_OBJ);
	assertEquals(tripleNER2.getType(), TType.NER_BOTH);
	assertEquals(tripleNER3.getType(), TType.NER_SBJ);
	assertEquals(tripleJoinable1.getType(), TType.JOINABLE);
	assertEquals(tripleJoinableWrong.getType(), TType.JOINABLE);
    }

}
