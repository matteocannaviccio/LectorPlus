package it.uniroma3.main.util.inout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 
 * @author matteo
 *
 */
public class XMLReader{
    private BufferedReader br;

    /**
     * Creates an input stream for reading Wikipedia articles from a bz2-compressed dump file.
     * @param file
     */
    public XMLReader(String file) {
	if(file.endsWith("bz2"))
	    this.br = Compressed.getBufferedReaderForCompressedFile(file);
	else
	    this.br = getReaderXML(file);
    }

    /**
     * 
     * @param path
     * @return
     */
    public BufferedReader getReaderXML(String path){
	BufferedReader br = null;
	try {
	    br = new BufferedReader(new FileReader(new File(path)));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return br;
    }

    /**
     * 
     * @return
     */
    public String getArticle(){
	StringBuffer sb = new StringBuffer();
	String line;
	try {
	    // read until the next article
	    while ((line = br.readLine()) != null) {
		if (line.endsWith("<page>"))
		    break;
	    }
	    // no articles found in the dump
	    if (line == null) {
		br.close();
		return null;
	    }else{ // extract an article
		sb.append(line + "\n");
		while ((line = br.readLine()) != null) {
		    sb.append(line + "\n");
		    if (line.endsWith("</page>"))
			break;
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return sb.toString();
    }



    /**
     * Returns the next article in the dump.
     * 
     * @return
     */
    public List<String> nextChunk(int chunk){
	List<String> s = new ArrayList<String>(chunk);
	StringBuffer sb;
	while(chunk > 0){
	    sb = new StringBuffer();
	    String line;
	    try {
		// read untill the next article
		while ((line = br.readLine()) != null) {
		    if (line.endsWith("<page>"))
			break;
		}
		// no articles found in the dump
		if (line == null) {
		    break;
		}else{ // extract an article
		    sb.append(line + "\n");
		    while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
			if (line.endsWith("</page>"))
			    break;
		    }
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    s.add(sb.toString());
	    chunk-=1;
	}
	return s;
    }

    /**
     * 
     */
    public void close(){
	try {
	    this.br.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
