package it.uniroma3.extractor.util.reader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import it.uniroma3.extractor.bean.Lector;
import it.uniroma3.extractor.bean.WikiLanguage.Lang;
/**
 * A simplified NTriples serializer, used for writing output NTriples file.
 */
public class NTriplesWriterWrapper {
    private Writer out;
    private static String RESOURCE;
    private static String PROPERTY;

    /**
     * Using a stream so we can control the encoding.
     * 
     * @param out
     */
    public NTriplesWriterWrapper(String path) {
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
	if (!Lector.getWikiLang().getLang().equals(Lang.en))
	    RESOURCE = "http://" + Lector.getWikiLang().getLang().name() + ".dbpedia.org/resource/";
	else
	    RESOURCE = "http://dbpedia.org/resource/";
	PROPERTY = "http://dbpedia.org/ontology/";
    }

    /**
     * 
     * @param subject
     * @param property
     * @param object
     * @param literal
     */
    public void statement(String subject, String predicate, String object, boolean literal) {
	subject = RESOURCE + subject;
	predicate = PROPERTY + predicate;
	object = RESOURCE + object;
	try {
	    if (subject.startsWith("_:"))
		out.write(subject + " ");
	    else
		out.write("<" + subject + "> ");
	    out.write("<" + predicate + "> ");

	    if (literal)
		out.write('"' + escape(object) + '"' + ' ');
	    else if (object.startsWith("_:"))
		out.write(object + " ");
	    else
		out.write("<" + object + "> ");

	    out.write(".\n");
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
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
     * @param str
     * @return
     */
    private String escape(String str) {
	// longest possible escape sequence for a character is 10 chars
	int pos = 0;
	char buf[] = new char[str.length() * 10];

	for (int ix = 0; ix < str.length(); ix++) {
	    char ch = str.charAt(ix);
	    if (ch == 0x0020 || ch == 0x0021 ||
		    (ch >= 0x0023 && ch <= 0x005B) ||
		    (ch >= 0x005D && ch <= 0x007E))
		buf[pos++] = ch;
	    else {
		buf[pos++] = '\\'; // all the cases below need escaping
		if (ch < 0x0008 ||
			ch == 0x000B || ch == 0x000C ||
			(ch >= 0x000E && ch <= 0x001F) ||
			(ch >= 0x007F && ch < 0xFFFF)) {
		    // this doesn't handle non-BMP characters correctly. we'll deal with
		    // that if they ever show up.
		    buf[pos++] = 'u';
		    buf[pos++] = hex(ch >> 12);
		    buf[pos++] = hex((ch >> 8) & 0x000F);
		    buf[pos++] = hex((ch >> 4) & 0x000F);
		    buf[pos++] = hex(ch & 0x000F);
		} else if (ch == 0x0009)
		    buf[pos++] = 't';
		else if (ch == 0x000A)
		    buf[pos++] = 'n';
		else if (ch == 0x000D)
		    buf[pos++] = 'r';
		else if (ch == 0x0022)
		    buf[pos++] = '"';
		else if (ch == 0x005C)
		    buf[pos++] = '\\';
	    }
	}

	return new String(buf, 0, pos);
    }

    /**
     * 
     * @param ch
     * @return
     */
    private char hex(int ch) {
	if (ch < 0x000A)
	    return (char) ('0' + (char) ch);
	else
	    return (char) ('A' + (char) (ch - 10));
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
