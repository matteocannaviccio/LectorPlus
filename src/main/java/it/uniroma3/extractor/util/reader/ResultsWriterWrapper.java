package it.uniroma3.extractor.util.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import it.uniroma3.extractor.bean.Lector;

public class ResultsWriterWrapper {
    private Writer out;
    private static String WIKIPEDIA_URL;

    public ResultsWriterWrapper(String path){
	setURILang();

	try {
	    this.out = new OutputStreamWriter(getOutputStreamBZip2(path), "utf-8");
	} catch (java.io.UnsupportedEncodingException e) {
	    throw new RuntimeException(e);
	}
    }

    /**
     * Use the right URIs depending on the language.
     * 
     */
    private void setURILang(){
	WIKIPEDIA_URL = "https://" + Lector.getWikiLang().getLang().name() +".wikipedia.org/wiki/";
    }
    
    /**
     * 
     * @throws IOException
     */
    public void done() throws IOException {
	out.flush();
	out.close();
    }
    
    /**
     * 
     * @param subject
     * @param property
     * @param object
     * @param literal
     */
    public void provenance(String wikid, String sentence, String subject, String predicate, String object) {
	try {
	    out.write("# " + WIKIPEDIA_URL  + wikid + "\n");
	    out.write(String.format("%-10s %-30s %-30s %s\n", "## ", subject, predicate, object));
	    out.write("### \"" + sentence+ "\n\n");
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
    }
    
    /**
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


}
