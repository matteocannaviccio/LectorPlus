package it.uniroma3.parser;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import it.uniroma3.bean.WikiArticle;
import it.uniroma3.bean.WikiLanguage;
import it.uniroma3.configuration.Configuration;
import it.uniroma3.configuration.Lector;

public class MarkupParserTest {

    @BeforeClass
    public static void runOnceBeforeClass() {
	Configuration.init(new String[0]);
	Lector.init(new WikiLanguage(Configuration.getLanguageCode(), Configuration.getLanguageProperties()));
    }

    @Test
    public void removeAllWikilinksTest(){
	String raw = "The history of the <PE-ALIAS<Apache_Software_Foundation>> is linked to the <SE-AUG<Apache_HTTP_Server>>, "
		+ "development beginning in February 1993";
	String clean = "The history of the Apache Software Foundation is linked to the Apache HTTP Server, "
		+ "development beginning in February 1993";
	assertEquals(Lector.getMarkupParser().removeAllWikilinks(raw), clean);
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
	System.out.println(Lector.getMarkupParser().cleanEmptyTemplateWikilinks(raw));
	assertEquals(Lector.getMarkupParser().cleanEmptyTemplateWikilinks(raw), clean);
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
	assertEquals(Lector.getMarkupParser().cleanAllWikilinks(raw), clean);
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
	assertEquals(Lector.getMarkupParser().removeCommonSenseWikilinks(raw), clean);
    }

    @Test
    public void removeWikilinksTest_2(){
	WikiArticle article = WikiArticle.makeDummyArticle();
	String raw = "Berners-Lee was born in [[London]], England, United Kingdom, one of four children born to [[Mary Lee Woods]] and [[Conway Berners-Lee]]. "
		+ "His parents worked on the first commercially-built computer, the [[Ferranti Mark 1]]. He attended Sheen Mount Primary School, "
		+ "and then went on to attend south west London's [[Emanuel School]] from 1969 to 1973, at the time a [[direct grant grammar school]], "
		+ "which became an [[independent school]] in 1975. A keen [[trainspotter]] as a child, he learnt about electronics from tinkering with a"
		+ " model railway. He studied at [[The Queen's College, Oxford]] from 1973 to 1976, where he received a "
		+ "[[British undergraduate degree classification#First-class honours|first-class]] [[bachelor of arts]] degree in physics.";

	System.out.println(Lector.getMarkupParser().harvestAllWikilinks(Lector.getMarkupParser().removeCommonSenseWikilinks(raw), article));
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
	assertEquals(Lector.getMarkupParser().harvestAllWikilinks(raw, article), clean);
	assertEquals(article.getWikilinks().size(), 11);
    }

    @Test
    public void harvestAllWikilinksBadformattedTest(){
	WikiArticle article = WikiArticle.makeDummyArticle();

	String raw = "test 1 - a [[strangewikilink|Jh]] molecule that is described.\n"
		+ "test 2 - a [[strangewikilink|@A]] molecule that is described.\n"
		+ "test 3 - a [[strangewikilink|Rend[ered]]] molecule that is described.\n"
		+ "test 4 - a [[[strangewikilink|Rendered]]] molecule that is described.\n"
		+ "test 5 - a [strangewikilink|[rend[ered]]]] molecule that is described.\n"
		+ "test 6 - a [[Wikilink))|Wikilik))]] molecule that is described.\n"
		+ "test 7 - a [[strangewikilink#specific|rendered]] molecule that is described.\n"
		+ "test 8 - a [[strangewikilink#specific|2001]] molecule that is described.";

	String clean = "test 1 - a <SE-ORG<Strangewikilink>> molecule that is described.\n"
		+ "test 2 - a <SE-ORG<Strangewikilink>> molecule that is described.\n"
		+ "test 3 - a <SE-ORG<Strangewikilink>> molecule that is described.\n"
		+ "test 4 - a <SE-ORG<Strangewikilink>> molecule that is described.\n"
		+ "test 5 - a [strangewikilink|[rend[ered]]]] molecule that is described.\n"
		+ "test 6 - a <SE-ORG<Wikilink))>> molecule that is described.\n"
		+ "test 7 - a <SE-ORG<Strangewikilink>> molecule that is described.\n"
		+ "test 8 - a 2001 molecule that is described.";

	System.out.println(Lector.getMarkupParser().harvestAllWikilinks(raw, article));
	assertEquals(Lector.getMarkupParser().harvestAllWikilinks(raw, article), clean);
	assertEquals(article.getWikilinks().size(), 6);
    }

    @Test
    public void blackListTest_1(){
	WikiArticle article = WikiArticle.makeDummyArticle();
	String raw = "[[American language|American]] football and [[Swedish language|Swedish]] wheather.";
	String clean = "American football and Swedish wheather.";
	assertEquals(Lector.getMarkupParser().harvestAllWikilinks(raw,article), clean);
    }


}
