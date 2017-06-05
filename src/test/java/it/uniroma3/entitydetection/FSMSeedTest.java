package it.uniroma3.entitydetection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.extractor.configuration.Configuration;
import it.uniroma3.extractor.entitydetection.FSMSeed;
import it.uniroma3.extractor.util.nlp.OpenNLP;

public class FSMSeedTest {
    
    private static FSMSeed fsm;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	fsm = new FSMSeed(new OpenNLP());
    }
    
    @Test
    public void findSeedTest_1() {
        String sentence = "Actresses is a 1997 Catalan language Spanish drama film.";
        assertEquals(fsm.findNationalityAndSeed(sentence).value.get(0), "film");
    }
    
    @Test
    public void findSeedTest_2() {
        String sentence = "Alan Mathison Turing OBE FRS (/ˈtjʊərɪŋ/; 23 June 1912 – 7 June 1954) was an English computer "
        	+ "scientist, mathematician, logician, cryptanalyst and theoretical biologist.";
        List<String> natClass = fsm.findNationalityAndSeed(sentence).value;
        assertEquals(natClass.size(), 5);
        assertTrue(natClass.contains("scientist"));
        assertTrue(natClass.contains("mathematician"));
        assertTrue(natClass.contains("logician"));
        assertTrue(natClass.contains("cryptanalyst"));
        assertTrue(natClass.contains("biologist"));
    }
    

}
