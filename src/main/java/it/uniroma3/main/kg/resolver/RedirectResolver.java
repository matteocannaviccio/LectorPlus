package it.uniroma3.main.kg.resolver;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.util.KeyValueIndex;
import it.uniroma3.main.util.Pair;
import it.uniroma3.main.util.inout.TSVReader;

public class RedirectResolver {

  private KeyValueIndex indexRedirect;

  /**
   * 
   */
  public RedirectResolver() {
    if (!new File(Configuration.getRedirectIndex()).exists()) {
      this.indexRedirect =
          getIndexOrCreate(Configuration.getRedirectIndex(), Configuration.getRedirectFile());
    } else // we already have the index
      this.indexRedirect = new KeyValueIndex(Configuration.getRedirectIndex());

  }

  /**
   * Returns a KeyValueIndex given the path. If the exists does not exist it create it and then
   * return.
   * 
   * @param indexPath
   * @param sourcePath
   * @return
   */
  private KeyValueIndex getIndexOrCreate(String indexPath, String sourcePath) {
    KeyValueIndex index = null;
    if (!new File(indexPath).exists()) {
      System.out.printf("\t\t%-20s %-20s %s", "--> Read & Index:", new File(indexPath).getName(),
          "");

      long start_time = System.currentTimeMillis();
      List<Pair<String, String>> keyvalues = TSVReader.getLines2Pairs(sourcePath);
      index = new KeyValueIndex(keyvalues, indexPath);
      long end_time = System.currentTimeMillis();

      System.out.printf("%-20s %s\n", "lines: " + index.getIndexedLines(),
          "indexed in: " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time) + " sec.");
    } else // we already have the index
      index = new KeyValueIndex(indexPath);
    return index;
  }

  /**
   * 
   * @param possibleRedirect
   * @return
   */
  public String resolveRedirect(String possibleRedirect) {
    possibleRedirect = StringUtils.capitalize(possibleRedirect);
    String targetPage = possibleRedirect;
    Optional<String> target = indexRedirect.retrieveKeys(possibleRedirect).stream().findFirst();
    if (target.isPresent())
      if (!target.get().contains("WikiProjekt") && !target.get().contains("WikiProject")
          && !target.get().contains("Wikiportal:"))
        targetPage = target.get();
    return targetPage;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    Configuration.init(args);
    Configuration.updateParameter("language", "en");
    Configuration.updateParameter("dataFile", "/Users/matteo/Desktop/data");

    RedirectResolver t = new RedirectResolver();

    String entity = "Barack_Obama";

    System.out.println("\nRedirect of: ");
    System.out.println(t.resolveRedirect(entity));

  }
}
