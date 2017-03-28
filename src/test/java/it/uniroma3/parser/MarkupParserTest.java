package it.uniroma3.parser;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.configuration.Configuration;
import it.uniroma3.model.WikiArticle;

public class MarkupParserTest {
    
    private static MarkupParser mpar;
    
    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init("/Users/matteo/Desktop/data/config.properties");
	mpar = new MarkupParser();
    }
   
    @Test
    public void removeAllWikilinksTest(){
	String raw = "The history of the <PE-ALIAS<Apache_Software_Foundation>> is linked to the <SE-AUG<Apache_HTTP_Server>>, "
		+ "development beginning in February 1993";
	String clean = "The history of the Apache Software Foundation is linked to the Apache HTTP Server, "
		+ "development beginning in February 1993";
	assertEquals(mpar.removeAllWikilinks(raw), clean);
    }
    
    @Test
    public void cleanEmptyTemplateWikilinksTest(){
	String raw = "Barcelona's players have won a record number of [[Ballon d'Or (1956–2009)|]] awards (11), "
		+ "as well as a record number of [[FIFA World Player of the Year|]]. [[Xavi|]] in 2010, together with two "
		+ "other players who came through the club's youth academy ([[Lionel Messi]], [[Andrés Iniesta]]) "
		+ "were chosen as the three best players in the world";
	String clean = "Barcelona's players have won a record number of Ballon d'Or (1956–2009) awards (11), "
		+ "as well as a record number of FIFA World Player of the Year. Xavi in 2010, together with two "
		+ "other players who came through the club's youth academy ([[Lionel Messi]], [[Andrés Iniesta]]) "
		+ "were chosen as the three best players in the world";
	assertEquals(mpar.cleanEmptyTemplateWikilinks(raw), clean);
    }
    
    @Test
    public void cleanAllWikilinksTest(){
	String raw = "American football evolved in the United States, originating from the sports of ''[[association football]]'' "
		+ "and ''[[rugby football]]''. The first game of American football was played on [[1869 New Jersey vs. Rutgers football game|November 6, 1869]], "
		+ "between two college teams, [[Rutgers Scarlet Knights football|Rutgers]] and [[Princeton Tigers football|Princeton]], "
		+ "under rules based on the [[''association football'']] rules of the time.";
	String clean = "American football evolved in the United States, originating from the sports of ''association football'' "
		+ "and ''rugby football''. The first game of American football was played on November 6, 1869, between two college teams, "
		+ "Rutgers and Princeton, under rules based on the ''association football'' rules of the time.";
	assertEquals(mpar.cleanAllWikilinks(raw), clean);
    }
    
    @Test
    public void removeCommonSenseWikilinksTest(){
	String raw = "American football evolved in the United States, originating from the sports of ''[[association football]]'' "
		+ "and ''[[rugby football]]''. The first game of American football was played on [[1869 New Jersey vs. Rutgers football game|November 6, 1869]], "
		+ "between two college teams, [[Rutgers Scarlet Knights football|Rutgers]] and [[Princeton Tigers football|Princeton]], "
		+ "under rules based on the [[''association football'']] rules of the time.";
	String clean = "American football evolved in the United States, originating from the sports of ''association football'' "
		+ "and ''rugby football''. The first game of American football was played on [[1869 New Jersey vs. Rutgers football game|November 6, 1869]], "
		+ "between two college teams, [[Rutgers Scarlet Knights football|Rutgers]] and [[Princeton Tigers football|Princeton]], "
		+ "under rules based on the ''association football'' rules of the time.";
	assertEquals(mpar.removeCommonSenseWikilinks(raw), clean);
    }
    
    @Test
    public void harvestAllWikilinksTest(){
	WikiArticle article = WikiArticle.makeDummyArticle();
	String raw = "[[Entrapment_(film)|Entrapment]] filming locations include [[Scottish]] places such as [[Blenheim Palace]], [[Savoy Hotel|Savoy Hotel London]],"
		+ " [[Lloyd's of London]], [[Borough Market]], London, [[Duart Castle London]] on the [[Isle of Mull]] in [[Scotland]], the [[Petronas Towers]] in [[Kuala Lumpur]]."
		+ " It raise up [[Canadian dollar|CAD]]20000 in the first week confirmed by [[Jon Amiel]], the [[Film Producer]].";
	String clean = "<SE-ORG<Entrapment_(film)>> filming locations include Scottish places such as <SE-ORG<Blenheim_Palace>>, <SE-ORG<Savoy_Hotel>>, <SE-ORG<Lloyd's_of_London>>, "
		+ "<SE-ORG<Borough_Market>>, London, <SE-ORG<Duart_Castle_London>> on the <SE-ORG<Isle_of_Mull>> in <SE-ORG<Scotland>>, the <SE-ORG<Petronas_Towers>> "
		+ "in <SE-ORG<Kuala_Lumpur>>. It raise up CAD20000 in the first week confirmed by <SE-ORG<Jon_Amiel>>, the Film Producer.";
	assertEquals(mpar.harvestAllWikilinks(raw, article), clean);
	assertEquals(article.getWikilinks().size(), 11);
    }
    
    @Test
    public void harvestAllWikilinksBadformattedTest(){
	WikiArticle article = WikiArticle.makeDummyArticle();
	
	String raw = "test 1 - a [[wikilink|Jh]] molecule that is described.\n"
		+ "test 2 - a [[wikilink|@A]] molecule that is described.\n"
		+ "test 3 - a [[wikilink|Rend[ered]]] molecule that is described.\n"
		+ "test 4 - a [[[wikilink|Rendered]]] molecule that is described.\n"
		+ "test 5 - a [wikilink|[rend[ered]]]] molecule that is described.\n"
		+ "test 6 - a [[Wikilink))|Wikilik))]] molecule that is described.\n"
		+ "test 7 - a [[wikilink#specific|rendered]] molecule that is described.\n"
		+ "test 8 - a [[wikilink#specific|2001]] molecule that is described.";
	
	String clean = "test 1 - a <SE-ORG<wikilink>> molecule that is described.\n"
		+ "test 2 - a <SE-ORG<wikilink>> molecule that is described.\n"
		+ "test 3 - a <SE-ORG<wikilink>> molecule that is described.\n"
		+ "test 4 - a <SE-ORG<wikilink>> molecule that is described.\n"
		+ "test 5 - a [wikilink|[rend[ered]]]] molecule that is described.\n"
		+ "test 6 - a <SE-ORG<Wikilink))>> molecule that is described.\n"
		+ "test 7 - a <SE-ORG<wikilink>> molecule that is described.\n"
		+ "test 8 - a 2001 molecule that is described.";
	
	assertEquals(mpar.harvestAllWikilinks(raw, article), clean);
	assertEquals(article.getWikilinks().size(), 6);
    }
    
    @Test
    public void blackListTest(){
	WikiArticle article = WikiArticle.makeDummyArticle();
	String raw = "[[American language|American]] football and [[Swedish language|Swedish]] wheather.";
	String clean = "American football and Swedish wheather.";
	System.out.println(mpar.harvestAllWikilinks(raw,article));
	assertEquals(mpar.harvestAllWikilinks(raw,article), clean);
    }

}
