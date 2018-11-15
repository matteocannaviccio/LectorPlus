package it.uniroma3.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the configuration file and set all the parameters. This static class contains all the
 * paths, parameters and thresholds that are required over the whole system.
 * 
 * @author matteo
 *
 */
public class Configuration {

  /* a map contains everything */
  public static Map<String, String> keyValue = new TreeMap<String, String>();

  /**
   * Print the (interesting) details of the config file.
   */
  public static void printFullyDetails() {
    System.out.println("\nConfiguration");
    System.out.println("-------------");
    System.out.printf("\t%-30s %s\n", "Language:", Configuration.getLanguageCode());
    System.out.printf("\t%-30s %s\n", "---", "");
    System.out.printf("\t%-30s %s\n", "Data Folder:", Configuration.getDataFolder());
    System.out.printf("\t%-30s %s\n", "Input Wikipedia:", Configuration.getOriginalArticlesFile());
    System.out.printf("\t%-30s %s\n", "Input DBPedia:", Configuration.getDBPediaDumpFile());
    System.out.printf("\t%-30s %s\n", "Output Folder:", Configuration.getOutputFolder());
    System.out.printf("\t%-30s %s\n", "---", "");
    System.out.printf("\t%-30s %s\n", "Pipeline:", Configuration.getPipelineSteps().toString());
    System.out.printf("\t%-30s %s\n", "Tot. Articles:",
        (Configuration.getNumArticlesToProcess() == -1) ? "ALL"
            : Configuration.getNumArticlesToProcess());
    System.out.printf("\t%-30s %s\n", "by:", Configuration.getChunkSize());
    System.out.printf("\t%-30s %s\n", "In memory:",
        (Configuration.inMemoryProcess()) ? "YES" : "NO");
    System.out.printf("\t%-30s %s\n", "Solve Redirect:",
        (Configuration.solveRedirect()) ? "YES" : "NO");
    System.out.printf("\t%-30s %s\n", "All wikilinks of page:",
        (Configuration.getOnlyTextWikilinks()) ? "NO" : "YES");
    System.out.printf("\t%-30s %s\n", "---", "");
    System.out.printf("\t%-30s %s\n", "Extraction model:", Configuration.getModelCode());
  }

  /**
   * Update a parameter after loading the config file.
   * 
   * @param key
   * @param value
   */
  public static void updateParameter(String key, String value) {
    keyValue.put(key, value);
  }

  /**
   * Returns a map with the parameters obtained parsing the command line.
   * 
   * @param args
   * @return
   */
  private static Map<String, String> parseOptions(String[] args) {
    Map<String, String> options = new HashMap<String, String>();
    for (String arg : args) {
      if (arg.contains("="))
        options.put(arg.split("=")[0], arg.split("=")[1]);
    }
    return options;
  }

  /**
   * Reads the config file.
   *
   * @param args
   */
  public static void init(String[] args) {
    Map<String, String> optionsCommmandLine = new HashMap<String, String>();
    /*
     * get the config file
     */
    String configFile = null;
    if (args.length == 0) {
      configFile = "./config.properties";
    } else if (args.length >= 1) {
      configFile = args[0];
      optionsCommmandLine = parseOptions(args);
    } else {
      System.out.println("Error in parsing config file.");
      System.exit(1);
    }

    /*
     * remove the following instruction when we insert a logger we keep it for now to silence all
     * the loggers.
     */
    //BasicConfigurator.configure(new NullAppender());

    /*
     * start here
     */
    Pattern p = Pattern.compile("^([a-z,A-Z]+) *= *(.*)$");
    BufferedReader br = null;
    try {
      InputStream fis = new FileInputStream(configFile);
      br = new BufferedReader(new InputStreamReader(fis));
      String line;
      while ((line = br.readLine()) != null) {
        // ignore comments or lines that start with whitespace
        if (line.matches("^#.*") || line.trim().isEmpty()) {
          continue;
        }
        // match for value
        Matcher m = p.matcher(line);

        if (!m.find()) {
          System.out.println("Config format not recognized! Offending line:");
          System.out.println(line);
        }

        String key = m.group(1);
        String value = m.group(2);

        keyValue.put(key, value);
      }

      keyValue.putAll(optionsCommmandLine);

      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String getTask() {
    return keyValue.get("task");
  }

  /***********************************************************************/
  /************************* LANGUAGE *******************************/
  /***********************************************************************/
  public static Set<String> getLanguages() {
    return new HashSet<String>(Arrays.asList(keyValue.get("languages").split(",")));
  }

  public static String getLanguageCode() {
    return keyValue.get("language");
  }

  public static String getLanguageProperties() {
    return getDataFolder() + "/languages/" + getLanguageCode() + ".properties";
  }

  public static WikiLanguage getLanguage() {
    return new WikiLanguage(getLanguageCode(), getLanguageProperties());
  }

  public static String getPipelineSteps() {
    return keyValue.get("pipeline");
  }

  /***********************************************************************/
  /************************* MAIN FOLDERS ****************************/
  /***********************************************************************/
  public static String getDataFolder() {
    String dataFolder = null;
    if (keyValue.get("dataFile") == null) {
      System.out.println("Data Folder not selected. System exit.");
      System.exit(1);
    } else {
      dataFolder = keyValue.get("dataFile");
    }
    return dataFolder;
  }

  private static String getInputFolder() {
    String folderPath = getDataFolder() + "/" + keyValue.get("inputFolder");
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  public static String getLectorFolder() {
    String folderPath =
        getDataFolder() + "/" + keyValue.get("lectorFolder") + "/" + getLanguageCode();
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  public static String getOutputFolder() {
    String folderPath =
        getDataFolder() + "/" + keyValue.get("outputFolder") + "/" + getLanguageCode();
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getIndexesFolder(String langCode) {
    String folderPath = getDataFolder() + "/" + keyValue.get("indexesFolder") + "/" + langCode;
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getSourceFolder(String langCode) {
    String folderPath = getDataFolder() + "/" + keyValue.get("sourceFolder") + "/" + langCode;
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getTypesFolder(String langCode) {
    String folderPath = getSourceFolder(langCode) + "/" + keyValue.get("typesFolder");
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getOntologyFolder() {
    String folderPath =
        getDataFolder() + "/" + keyValue.get("sourceFolder") + "/" + keyValue.get("ontologyFolder");
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getPosModelsFolder() {
    String folderPath =
        getDataFolder() + "/" + keyValue.get("posModelsFolder") + "/" + getLanguageCode();
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getListsFolder() {
    String folderPath =
        getDataFolder() + "/" + keyValue.get("listsFolder") + "/" + getLanguageCode();
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  private static String getSpotlightFolder() {
    String folderPath = getDataFolder() + "/" + keyValue.get("spotlightFolder");
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  /***********************************************************************/
  /************************* DBPEDIA STUFFS ************************/
  /***********************************************************************/
  private static String getDBPediaPath() {
    return getInputFolder() + "/" + keyValue.get("dbpediaPath") + "/" + getLanguageCode();
  }

  public static String getDBPediaIndex() {
    return getDBPediaPath() + "/" + keyValue.get("dbpediaIndex");
  }

  public static String getDBPediaDumpFile() {
    return getDBPediaPath() + "/" + keyValue.get("dbpediaName");
  }

  public static String getIndexableDBPediaFile() {
    return getDBPediaPath() + "/" + "dbpedia_normalized.tsv";
  }

  /***********************************************************************/
  /********************** WIKIPEDIA STUFFS ***************************/
  /***********************************************************************/
  private static String getWikipediaPath() {
    return getInputFolder() + "/" + keyValue.get("wikipediaPath") + "/" + getLanguageCode();
  }

  public static String getOriginalArticlesFile() {
    return getWikipediaPath() + "/" + "dump.xml.bz2";
  }

  public static String getAugmentedArticlesFile() {
    return getWikipediaPath() + "/" + keyValue.get("augmentedArticles");
  }

  public static String getParsedArticlesFile() {
    return getWikipediaPath() + "/" + keyValue.get("parsedArticles");
  }

  public static String getDetailArticlesFile() {
    return getWikipediaPath() + "/" + keyValue.get("detailsArticles");
  }

  /***********************************************************************/
  /********************** OUTPUT & MODELS *************************/
  /***********************************************************************/
  public static String getDBModel() {
    return getLectorFolder() + "/" + getLanguageCode() + "_" + keyValue.get("dbmodel");
  }

  public static String getDBCrossValidation() {
    return getDBModel().replace(".db", "_cross.db");
  }

  public static String getOutputFactsFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "facts.bz2";
  }

  public static String getOutputOntologicalFactsFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "facts_ont.bz2";
  }

  public static String getProvenanceFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "provenance.bz2";
  }

  public static String getProvenanceOntologicalFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "provenance_ont.bz2";
  }

  public static String getOutputTypedPhrasesStatsFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "typedphrases_stats.tsv";
  }

  public static String getOutputPhrasesStatsFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "phrases_stats.tsv";
  }

  public static String getOutputRelationsStatsFile(String modelName) {
    return getModelFolder(modelName) + "/" + getLanguageCode() + "_" + "relations_stats.tsv";
  }

  /***********************************************************************/
  /*********************** TYPES INDEXES ***************************/
  /***********************************************************************/
  public static String getTypesIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("typesIndexName");
  }

  public static String getSDTypesIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("sdtypedIndexName");
  }

  public static String getAirpediaIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("airpediaIndexName");
  }

  public static String getLHDTypesIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("lhdIndexName");
  }

  public static String getDBTaxTypesIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("dbtaxIndexName");
  }

  public static String getRedirectIndex() {
    return getIndexesFolder(getLanguageCode()) + "/" + keyValue.get("redirectIndexName");
  }

  public static String getTypesIndex_Ref() {
    return getIndexesFolder("en") + "/" + keyValue.get("typesIndexName");
  }

  public static String getAirpediaIndex_Ref() {
    return getIndexesFolder("en") + "/" + keyValue.get("airpediaIndexName");
  }

  /***********************************************************************/
  /*********************** NORMALIZED TYPES ***********************/
  /***********************************************************************/
  public static String getRedirectFile() {
    return getSourceFolder(getLanguageCode()) + "/" + keyValue.get("redirectFile");
  }

  /***********************************************************************/
  /*********************** SOURCE TYPES ***************************/
  /***********************************************************************/
  public static String getSourceMainInstanceTypes() {
    return getTypesFolder(getLanguageCode()) + "/" + keyValue.get("mainInstanceType");
  }

  public static String getSourceAirpediaTypes() {
    return getTypesFolder(getLanguageCode()) + "/" + keyValue.get("airpediaInstanceType");
  }

  public static String getSourceDBTaxInstanceTypes() {
    return getTypesFolder(getLanguageCode()) + "/" + keyValue.get("dbtaxInstanceType");
  }

  public static String getSourceLHDInstanceTypes() {
    return getTypesFolder(getLanguageCode()) + "/" + keyValue.get("lhdInstanceType");
  }

  public static String getSourceSDTypedInstanceTypes() {
    return getTypesFolder(getLanguageCode()) + "/" + keyValue.get("sdtypedInstanceType");
  }

  public static String getSourceMainInstanceTypes_Ref() {
    return getTypesFolder("en") + "/" + keyValue.get("mainInstanceType");
  }

  public static String getSourceAirpediaTypes_Ref() {
    return getTypesFolder("en") + "/" + keyValue.get("airpediaInstanceType");
  }

  public static String getDBPediaOntologyFile() {
    return getOntologyFolder() + "/" + keyValue.get("ontology");
  }

  /***********************************************************************/
  /*********************** OPEN NLP MODELS ************************/
  /***********************************************************************/
  public static String getTokenModel() {
    return getPosModelsFolder() + "/" + keyValue.get("tokenModel");
  }

  public static String getLemmatizerModel() {
    return getPosModelsFolder() + "/" + keyValue.get("lemmatizerDictonary");
  }

  public static String getPOSModel() {
    return getPosModelsFolder() + "/" + keyValue.get("postaggerModel");
  }

  /***********************************************************************/
  /*********************** SPOTLIGHT FOLDER ************************/
  /***********************************************************************/
  public static String getSpotlightModel() {
    return getSpotlightFolder() + "/" + getLanguageCode();
  }

  public static String getSpotlightJar() {
    return getSpotlightFolder() + "/" + keyValue.get("pathDBSpotLocalJar");
  }

  public static String getSpotlightLocalERR(int port) {
    return getSpotlightFolder() + "/" + keyValue.get("pathDBSpotErr") + "_" + port + ".txt";
  }

  public static String getSpotlightLocalURL() {
    return keyValue.get("pathDBSpotLocalUrl");
  }

  /***********************************************************************/
  /*********************** RESOURCES LIST ************************/
  /***********************************************************************/
  public static String getCurrenciesList() {
    return getListsFolder() + "/" + keyValue.get("currencies");
  }

  public static String getNationalitiesList() {
    return getListsFolder() + "/" + keyValue.get("nationalities");
  }

  public static String getProfessionsList() {
    return getListsFolder() + "/" + keyValue.get("professions");
  }

  public static String getStopwordsList() {
    return getListsFolder() + "/" + keyValue.get("stopwords");
  }

  /***********************************************************************/
  /************************** PARAMETERS **************************/
  /***********************************************************************/
  public static int getNumArticlesToProcess() {
    return Integer.parseInt(keyValue.get("totArticle"));
  }

  public static int getChunkSize() {
    return Integer.parseInt(keyValue.get("chunckSize"));
  }


  public static boolean getOnlyTextWikilinks() {
    return keyValue.get("onlyTextWikilinks").equalsIgnoreCase("true");
  }

  public static boolean extractTables() {
    return keyValue.get("extractTables").equalsIgnoreCase("true");
  }

  public static boolean extractLists() {
    return keyValue.get("extractLists").equalsIgnoreCase("true");
  }

  public static boolean solveRedirect() {
    return keyValue.get("solveRedirect").equalsIgnoreCase("true");
  }

  public static boolean useDBpediaSpotlight() {
    return keyValue.get("useSpotlight").equalsIgnoreCase("yes");
  }

  public static double getPronounThreshold() {
    return Double.parseDouble(keyValue.get("pronounDensityThreshold"));
  }

  public static double getSubnameThreshold() {
    return Double.parseDouble(keyValue.get("subnameDensityThreshold"));
  }

  public static boolean inMemoryProcess() {
    return keyValue.get("inMemory").equalsIgnoreCase("true");
  }

  /************************************/

  public static String getLectorModelName() {
    return keyValue.get("lectorModel");
  }

  public static int getMinF() {
    return Integer.parseInt(keyValue.get("minF"));
  }

  public static int getPercUnl() {
    return Integer.parseInt(keyValue.get("percUnl"));
  }

  public static double getMajThr() {
    return Double.parseDouble(keyValue.get("majorityThreshold"));
  }

  public static String getModelCode() {
    String model = getLectorModelName();
    if (model.contains("TextExt"))
      return getLectorModelName();
    else
      return getLectorModelName() + "-" + getMinF() + "-" + getPercUnl() + "-" + getMajThr();
  }

  private static String getModelFolder(String modelName) {
    String folderPath = getOutputFolder() + "/" + modelName;
    File folder = new File(folderPath);
    if (!folder.exists())
      folder.mkdirs();
    return folder.getAbsolutePath();
  }

  /***********************************************************************/

}
