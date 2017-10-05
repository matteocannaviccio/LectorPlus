package it.uniroma3.main.util.inout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import it.uniroma3.config.Lector;

public class ResultsWriterWrapper {
  private StringBuffer buffer = new StringBuffer();
  private Writer out;
  private static String WIKIPEDIA_URL;

  public ResultsWriterWrapper(String path) {
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
  private void setURILang() {
    WIKIPEDIA_URL = "https://" + Lector.getWikiLang().getLang().name() + ".wikipedia.org/wiki/";
  }

  /**
   * 
   * @throws IOException
   */
  public void done() throws IOException {
    out.write(buffer.toString());
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
  public void provenance(String wikid, String section, String phrase, String sentence,
      String lectorSubect, String subject, String predicate, String lectorObject, String object) {
    if (this.buffer.length() > 1000000) {
      try {
        out.write(buffer.toString());
        out.flush();
        buffer = buffer.delete(0, buffer.length());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    String line = WIKIPEDIA_URL + wikid + "\t" + section + "\t" + predicate + "\t" + lectorSubect
        + "\t" + subject + "\t" + lectorObject + "\t" + object + "\t" + phrase + "\t" + sentence;
    buffer.append(line.replace("\n", " ") + "\n");
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
