package it.uniroma3.reader;

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
    
    public RDFReader(String path){
	this.is = getInputStreamBZip2(path);
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
    public Iterator<Triple> readTTLBzip2File(){
	return RiotReader.createIteratorTriples(is, Lang.TTL, null);
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
