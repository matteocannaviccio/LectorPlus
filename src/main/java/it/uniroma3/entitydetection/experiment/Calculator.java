package it.uniroma3.entitydetection.experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import it.uniroma3.bean.WikiArticle;
import it.uniroma3.util.Pair;

public class Calculator {

    /**
     * Counts the instances of PE-..<> over all the blocks of the article.
     * It takes into account the difference between abstract and body.
     * @param article
     * @return
     */
    private static Pair<Integer, Integer> getAnnotatedPE(WikiArticle article){
	int countPeAbstract = 0;
	int countPeBody = 0;
	Pattern PE = Pattern.compile("PE-[^<]*<([^>]*)>");
	for (Entry<String, String> block : article.getBlocks().entrySet()){
	    if (block.getKey().equals("#Abstract")){
		Matcher m = PE.matcher(block.getValue());
		while(m.find()){
		    countPeAbstract++;
		}
	    }else{
		Matcher m = PE.matcher(block.getValue());
		while(m.find()){
		    countPeBody++;
		}
	    }
	}
	return Pair.make(countPeAbstract, countPeBody);
    }

    /**
     * Harvest all the instances of SE-..<> over all the blocks of the article.
     * It takes into account the difference between abstract and body.
     * @param article
     * @return
     */
    private static Pair<List<String>,List<String>> getAnnotatedSE(WikiArticle article){
	List<String> listSeAbstract = new ArrayList<String>();
	List<String> listSeBody = new ArrayList<String>();

	Pattern SE = Pattern.compile("SE-[^<]*<([^>]*)>");
	for (Entry<String, String> block : article.getBlocks().entrySet()){
	    if (block.getKey().equals("#Abstract")){
		Matcher m = SE.matcher(block.getValue());
		while(m.find()){
		    listSeAbstract.add("wiki/" + m.group(1));
		}
	    }else{
		Matcher m = SE.matcher(block.getValue());
		while(m.find()){
		    listSeBody.add("wiki/" + m.group(1));
		}
	    }
	}
	
	if (listSeAbstract.isEmpty())
	    listSeAbstract.add("-");
	if (listSeBody.isEmpty())
	    listSeBody.add("-");
	return Pair.make(listSeAbstract, listSeBody);
    }

    /**
     * 
     * @param article
     * @return
     */
    public static String obtainStats(WikiArticle article){
	Pair<Integer, Integer> primaries = getAnnotatedPE(article);
	Pair<List<String>,List<String>> secondaries = getAnnotatedSE(article);
	return 
	article.getWikid() + "\t" 
	+ primaries.key + "\t" 
	+ primaries.value + "\t" 
	+ StringUtils.join(secondaries.key, " ") + "\t"
	+ StringUtils.join(secondaries.value, " ");
    }

}
