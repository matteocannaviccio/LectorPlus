package it.uniroma3.main.util.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import it.uniroma3.config.Lector;

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
    public void provenance(String wikid, String section, String phrase, String sentence, String lectorSubect, String subject, String predicate, String lectorObject, String object) {
	try {
	    String line = WIKIPEDIA_URL + wikid + "\t" + section + "\t" + predicate + "\t" + lectorSubect + "\t" + subject + "\t" + lectorObject + "\t" + object + "\t" + phrase + "\t" + sentence;
	    out.write(line.replace("\n", " ") + "\n");
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
