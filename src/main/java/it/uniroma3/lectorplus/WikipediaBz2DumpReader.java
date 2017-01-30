package it.uniroma3.lectorplus;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.bzip2.CBZip2InputStream;
/**
 * 
 * @author matteo
 *
 */
public class WikipediaBz2DumpReader {
    private static final int DEFAULT_STRINGBUFFER_CAPACITY = 1024;
    private BufferedReader br;

   /**
    * Creates an input stream for reading Wikipedia articles from a bz2-compressed dump file.
    * @param file
    */
    public WikipediaBz2DumpReader(String file) {
	FileInputStream fis;
	try {
	    fis = new FileInputStream(file);
	    byte[] ignoreBytes = new byte[2];
	    fis.read(ignoreBytes); // "B", "Z" bytes from commandline tools
	    br = new BufferedReader(new InputStreamReader(new CBZip2InputStream(fis), "UTF8"));
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Returns the next article in the dump.
     * @return
     */
    public String nextArticle(){
	String s;
	StringBuffer sb = new StringBuffer(DEFAULT_STRINGBUFFER_CAPACITY);
	try {
	    // read untill the next article
	    while ((s = br.readLine()) != null) {
		if (s.endsWith("<page>"))
		    break;
	    }
	    // no articles found in the dump
	    if (s == null) {
		br.close();
		return null;
	    }else{ // extract an article
		sb.append(s + "\n");
		while ((s = br.readLine()) != null) {
		    sb.append(s + "\n");
		    if (s.endsWith("</page>"))
			break;
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return sb.toString();
    }
}
