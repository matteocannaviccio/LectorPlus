package it.uniroma3.extractor.util.reader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * 
 * @author matteo
 *
 */
public class LectorRDFReader{
    public enum Encoding {tsv, gz, bzip2};

    private InputStream is;

    /**
     * 
     * @param path
     * @param encoding
     */
    public LectorRDFReader(String path, Encoding encoding){
	switch(encoding){
	case tsv:
	    this.is = getInputStream(path);
	    break;

	case gz:
	    this.is = getInputStreamGZ(path);
	    break;

	case bzip2:
	    this.is = getInputStreamBZip2(path);
	    break;
	}
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
	}
	return is;
    }

    /**
     * 
     * @param path
     * @return
     */
    private InputStream getInputStreamGZ(String path){
	InputStream is = null;
	try {
	    FileInputStream fis = new FileInputStream(path);
	    is = new GZIPInputStream((fis));
	} catch (IOException e) {
	}
	return is;
    }

    /**
     * 
     * @param path
     * @return
     */
    public Iterator<Triple> readTTLFile(){
	return RDFDataMgr.createIteratorTriples(is, Lang.TTL, null);
    }

    /**
     * 
     * @param path
     * @return
     */
    public Iterator<Triple> readNTFile(){
	return RDFDataMgr.createIteratorTriples(is, Lang.NTRIPLES, null);
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
