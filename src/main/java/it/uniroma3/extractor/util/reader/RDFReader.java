package it.uniroma3.extractor.util.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotReader;
import org.apache.tools.bzip2.CBZip2InputStream;

import com.hp.hpl.jena.graph.Triple;
/**
 * 
 * @author matteo
 *
 */
public class RDFReader{

    private InputStream is;

    public RDFReader(String path, boolean isBzip2){
	if(isBzip2)
	    this.is = getInputStreamBZip2(path);
	else
	    this.is = getInputStream(path);
    }

    /**
     * 
     * @param path
     * @return
     */
    private InputStream getInputStream(String path){
	InputStream is = null;
	try {
	    is = new FileInputStream(path);
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return is;
    }
    
    /**
     * 
     * @param path
     * @return
     */
    private InputStream getInputStreamBZip2(String path){
	InputStream is = null;
	try {
	    FileInputStream fis = new FileInputStream(path);
	    byte[] ignoreBytes = new byte[2];
	    fis.read(ignoreBytes); // "B", "Z" bytes from commandline tools
	    is = new CBZip2InputStream((fis));
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return is;
    }

    /**
     * 
     * @param path
     * @return
     */
    public Iterator<Triple> readTTLFile(){
	return RiotReader.createIteratorTriples(is, Lang.TTL, null);
    }
    
    /**
     * 
     * @param path
     * @return
     */
    public Iterator<Triple> readNTFile(){
	return RiotReader.createIteratorTriples(is, Lang.NT, null);
    }

    /**
     * 
     */
    public void closeReader(){
	try {
	    this.is.close();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

}
