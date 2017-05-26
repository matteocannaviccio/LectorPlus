package it.uniroma3.extractor.util.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.bzip2.CBZip2InputStream;
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
    public XMLReader(String file, boolean isBzip2) {
	if(isBzip2)
	    this.br = getReaderBZip2(file);
	else
	    this.br = getReaderXML(file);
    }
    
    /**
     * 
     * @param path
     * @return
     */
    public BufferedReader getReaderBZip2(String path){
	System.out.println("Creating reader from: " + path);
	BufferedReader br = null;
	try {
	    FileInputStream fis = new FileInputStream(path);
	    byte[] ignoreBytes = new byte[2];
	    fis.read(ignoreBytes); // "B", "Z" bytes from commandline tools
	    br = new BufferedReader(new InputStreamReader(new CBZip2InputStream(fis), "UTF8"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return br;
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
	while(chunk > 0){
	    StringBuffer sb = new StringBuffer();
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
    public void closeBuffer(){
	try {
	    this.br.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
