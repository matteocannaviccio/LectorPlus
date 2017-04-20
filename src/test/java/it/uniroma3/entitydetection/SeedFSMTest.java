package it.uniroma3.entitydetection;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.entitydetection.SeedFSM;
import it.uniroma3.util.nlp.OpenNLP;

public class SeedFSMTest {
    
    private static SeedFSM fsm;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	fsm = new SeedFSM(new OpenNLP());
    }
    
    @Test
    public void findSeedTest_1() {
        String sentence = "Actresses is a 1997 Catalan language Spanish drama film.";
        assertEquals(fsm.findSeed(sentence).get(0), "film");
    }
    
    @Test
    public void findSeedTest_2() {
        String sentence = "Alan Mathison Turing OBE FRS (/ˈtjʊərɪŋ/; 23 June 1912 – 7 June 1954) was an English computer "
        	+ "scientist, mathematician, logician, cryptanalyst and theoretical biologist.";
        List<String> natClass = fsm.findSeed(sentence);
        assertEquals(natClass.size(), 5);
        assertTrue(natClass.contains("scientist"));
        assertTrue(natClass.contains("mathematician"));
        assertTrue(natClass.contains("logician"));
        assertTrue(natClass.contains("cryptanalyst"));
        assertTrue(natClass.contains("biologist"));
    }
    

}
