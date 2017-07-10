package it.uniroma3.extractor.main.pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.uniroma3.config.Configuration;
import it.uniroma3.extractor.bean.WikiArticle;
import it.uniroma3.extractor.bean.WikiArticle.ArticleType;
/**
 * 
 * @author matteo
 *
 */
public class Statistics {

    private Map<ArticleType, List<String>> countTypes;

    public Statistics(){
	this.countTypes = new ConcurrentHashMap<ArticleType, List<String>>(15);
    }

    /**
     * 
     * @param a
     */
    public synchronized WikiArticle addArticleToStats(WikiArticle a){
	ArticleType type = a.getType();
	if(!countTypes.containsKey(type))
	    countTypes.put(type, new LinkedList<String>());
	countTypes.get(type).add(a.getWikid() + "\t" + a.getNamespace());
	return a;
    }

    /**
     * 
     * @return
     */
    public void printStats() {
	System.out.println("\nStats");
	System.out.println("-----");
	for (Map.Entry<ArticleType, List<String>> entry : this.countTypes.entrySet())
	    System.out.printf("\t%-20s %s\n", entry.getKey(), entry.getValue().size());
    }

    /**
     * 
     * @return
     */
    public void writeDetailsFile() {
	try {
	    BufferedWriter bw = new BufferedWriter(new FileWriter(new File(Configuration.getDetailArticlesFile())));
	    for (Map.Entry<ArticleType, List<String>> entry : this.countTypes.entrySet()){
		for(String wikid : entry.getValue()){
		    bw.write(entry.getKey().toString() + "\t" + wikid + "\n");
		}
	    }
	    bw.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }


}
