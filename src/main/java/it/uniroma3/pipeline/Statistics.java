package it.uniroma3.pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.uniroma3.bean.WikiArticle;
import it.uniroma3.bean.WikiArticle.ArticleType;
import it.uniroma3.configuration.Configuration;
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
	countTypes.get(type).add(a.getWikid());
	return a;
    }

    /**
     * 
     * @return
     */
    public String printStats() {
	StringBuffer stats = new StringBuffer();
	for (Map.Entry<ArticleType, List<String>> entry : this.countTypes.entrySet())
	    stats.append("\t" + entry.getKey() + "\t-->\t" + entry.getValue().size() + "\n");
	return stats.toString();
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
