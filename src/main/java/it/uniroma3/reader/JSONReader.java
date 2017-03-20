package it.uniroma3.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import it.uniroma3.model.WikiArticle;

public class JSONReader {

    private BufferedReader br;
    private Gson gson;

    /**
     * 
     * @param file
     */
    public JSONReader(String file){
	try {
	    this.br = new BufferedReader(new FileReader(new File(file)));
	    this.gson = new Gson();
	    
	} catch (FileNotFoundException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    /**
     * Returns the next chunck of WikiArticles.
     * 
     * @return
     */
    public List<WikiArticle> nextChunk(int chunk){
	List<WikiArticle> s = new ArrayList<WikiArticle>(chunk);
	try {
	    String article;
	    while((article=br.readLine())!=null && chunk > 0){
		s.add(gson.fromJson(article, WikiArticle.class));
		chunk--;
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return s;
    }

    /**
     * 
     */
    public void closeBuffer(){
	try {
	    this.br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
