package it.uniroma3.main.util.inout.ntriples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
/**
 * 
 * @author matteo
 *
 */
public class NTriplesWriter {
    private Writer out;
    private StringBuffer buffer;

    /**
     * Using a stream so we can control the encoding.
     * 
     * @param out
     */
    public NTriplesWriter(String path) {
	try {
	    this.buffer = new StringBuffer();
	    this.out = new OutputStreamWriter(getOutputStreamBZip2(path), "utf-8");
	} catch (java.io.UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Write an NTriple in the file (after convert it from the three fields string).
     * 
     * @param subject
     * @param predicate
     * @param object
     */
    public void write(String subject, String predicate, String object){
	this.buffer.append(NTriplesConverter.convertString2NTriple(subject, predicate, object));	
    }

    /**
     * Open the stream.
     * 
     * @param path
     * @return
     */
    @SuppressWarnings("resource")
    private OutputStream getOutputStreamBZip2(String path) {
	OutputStream out = null;
	try {
	    out = new FileOutputStream(path);
	    out = new CompressorStreamFactory().createCompressorOutputStream("bzip2", out); 
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (CompressorException e) {
	    e.printStackTrace();
	}
	return out;
    }

    /**
     * Close the stream.
     * 
     * @throws IOException
     */
    public void done() throws IOException {
	out.write(buffer.toString());
	out.flush();
	out.close();
    }

}
