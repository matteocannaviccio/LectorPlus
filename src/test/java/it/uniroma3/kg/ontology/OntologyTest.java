package it.uniroma3.kg.ontology;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.extractor.bean.Configuration;
import it.uniroma3.extractor.kg.tgpatterns.Ontology;
import it.uniroma3.extractor.kg.tgpatterns.TGPattern;

public class OntologyTest {
    
    private static Ontology ontology;

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	// force english
	Configuration.keyValue.put("languageUsed", "en");
	ontology = new Ontology();
    }
    
    @Test
    public void removeAllWikilinksTest(){
	TGPattern tg1 = ontology.getTGPattern("Person");
	TGPattern tg2 = ontology.getTGPattern("Guitarist");
	TGPattern tg3 = ontology.getTGPattern("MusicalArtist");
	TGPattern tg4 = ontology.getTGPattern("MusicalArtist");
	TGPattern tg5 = ontology.getTGPattern("Singer");
	TGPattern tg6 = ontology.getTGPattern("MusicDirector");
	TGPattern comb = tg1.combine(tg2).combine(tg3).combine(tg4).combine(tg5).combine(tg6);
	//System.out.println(comb);
	//System.out.println(comb.normalize());
	//System.out.println(comb.getMainPath(0.5));
    }

}
