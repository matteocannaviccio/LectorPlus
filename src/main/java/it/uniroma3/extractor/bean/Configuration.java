package it.uniroma3.extractor.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
/**
 * Loads the configuration file and set all the parameters.
 * 
 * @author matteo
 *
 */
public class Configuration {

    public static Map<String, String> keyValue = new TreeMap<String, String>();

    /**
     * Effectively reads the config file.
     * 
     * @param configFile
     */
    public static void init(String[] args){
	String lang = null;
	/*
	 * get the config file
	 */
	String configFile;
	if (args.length == 0){
	    configFile = "./config.properties";
	}else if (args.length == 1){
	    configFile = args[0];
	}else{
	    configFile = args[0];
	    lang = args[1];
	}
	    
	/*
	 * remove the following instruction when we insert a logger
	 * we keep it for now to silence all the loggers.
	 */
	BasicConfigurator.configure(new NullAppender());

	/*
	 * start here
	 */
	Pattern p = Pattern.compile("^([a-z,A-Z]+) *= *(.*)$");
	BufferedReader br = null;
	try{
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
	    
	    if (lang != null)
		keyValue.put("languageUsed", lang);
	    
	    br.close();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }


    /***********************************************************************/
    /*************************    LANGUAGES        *************************/
    /***********************************************************************/
    public static String getLanguageCode(){
	return keyValue.get("languageUsed");
    }

    public static String getLanguageProperties(){
	return getDataFolder() + "/languages/" + getLanguageCode() + ".properties";
    }

    public static Set<String> getPipelineSteps(){
	return new HashSet<String>(Arrays.asList(keyValue.get("pipeline").split(",")));
    }
    /***********************************************************************/
    /*************************    MAIN FOLDERS        **********************/
    /***********************************************************************/
    private static String getDataFolder(){
	return keyValue.get("dataFile");
    }

    private static String getInputFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("inputFolder");
	File folder = new File(folderPath); 
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    public static String getLectorFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("lectorFolder") + "/" + getLanguageCode();
	File folder = new File(folderPath); 
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getIndexesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("indexesFolder") + "/" + getLanguageCode();
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getTypesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("sourceFolder")+ "/" + getLanguageCode() + "/" + keyValue.get("typesFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getOntologyFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("sourceFolder")+ "/" + keyValue.get("ontologyFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getModelsFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("modelsFolder")+ "/" + getLanguageCode();
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getListsFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("listsFolder")+ "/" + getLanguageCode();
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getSpotlightFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("spotlightFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    /***********************************************************************/
    /*************************    DBPEDIA STUFFS        ********************/
    /***********************************************************************/

    private static String getDBPediaPath(){
	return getInputFolder() + "/" + keyValue.get("dbpediaPath") + "_" + getLanguageCode();
    }

    public static String getDBPediaIndex(){
	return getDBPediaPath() + "/" + keyValue.get("dbpediaIndex");
    } 

    public static String getDBPediaDumpFile(){
	return getDBPediaPath() + "/" + keyValue.get("dbpediaName");
    }

    public static String getIndexableDBPediaFile(){
	return getDBPediaPath() + "/" + "dbpedia_normalized.tsv";
    }

    /***********************************************************************/
    /**********************    WIKIPEDIA STUFFS        *********************/
    /***********************************************************************/

    private static String getWikipediaPath(){
	return getInputFolder() + "/" + keyValue.get("wikipediaPath") +
		"_" + getLanguageCode();
    }

    public static String getOriginalArticlesFile(){
	return getWikipediaPath() + "/" + "dump.xml.bz2";
    } 

    public static String getAugmentedArticlesFile(){
	return getWikipediaPath() + "/" + keyValue.get("augmentedArticles");
    } 

    public static String getParsedArticlesFile(){
	return getWikipediaPath() + "/" + keyValue.get("parsedArticles");
    } 

    public static String getDetailArticlesFile(){
	return getWikipediaPath() + "/" + keyValue.get("detailsArticles");
    }

    /***********************************************************************/
    /**********************    OUTPUT & MODELS     *************************/
    /***********************************************************************/

    public static String getDBModel(){
	return getLectorFolder() + "/" + getLanguageCode() + "_" + keyValue.get("dbmodel");
    }

    public static String getDBFacts(){
	return getLectorFolder() + "/" + getLanguageCode() + "_" + keyValue.get("dbfacts");
    }

    public static String getOutputFactsFile(){
	return getLectorFolder() + "/" + getLanguageCode() + "_" + keyValue.get("outputFile");
    }

    /***********************************************************************/
    /***********************    TYPES INDEXES    ***************************/
    /***********************************************************************/

    public static String getTypesIndex(){
	return getIndexesFolder() + "/" + keyValue.get("typesIndexName");
    } 

    public static String getSDTypesIndex(){
	return getIndexesFolder() + "/" + keyValue.get("sdtypedIndexName");
    } 

    public static String getLHDTypesIndex(){
	return getIndexesFolder() + "/" + keyValue.get("lhdIndexName");
    } 

    public static String getDBTaxTypesIndex(){
	return getIndexesFolder() + "/" + keyValue.get("dbtaxIndexName");
    } 

    public static String getAirpediaIndex(){
	return getIndexesFolder() + "/" + keyValue.get("airpediaIndexName");
    } 

    public static String getRedirectIndex(){
	return getIndexesFolder() + "/" + keyValue.get("redirectIndexName");
    } 

    /***********************************************************************/
    /***********************    NORMALIZED TYPES     ***********************/
    /***********************************************************************/

    public static String getRedirectFile(){
	return  keyValue.get("sourceFolder") + "/" + getLanguageCode() + "/" + keyValue.get("redirectFile");
    }

    /***********************************************************************/
    /***********************    SOURCE TYPES     ***************************/
    /***********************************************************************/

    public static String getSourceAirpediaInstanceTypes(){
	return getTypesFolder() + "/" + keyValue.get("airpediaInstanceType");
    } 

    public static String getSourceMainInstanceTypes(){
	return getTypesFolder() + "/" + keyValue.get("mainInstanceType");
    }

    public static String getSourceDBTaxInstanceTypes(){
	return getTypesFolder() + "/" + keyValue.get("dbtaxInstanceType");
    } 

    public static String getSourceLHDInstanceTypes(){
	return getTypesFolder() + "/" + keyValue.get("lhdInstanceType");
    } 

    public static String getSourceSDTypedInstanceTypes(){
	return getTypesFolder() + "/" + keyValue.get("sdtypedInstanceType");
    } 

    public static String getDBPediaOntologyFile(){
	return getOntologyFolder() + "/" + keyValue.get("ontology");
    } 

    /***********************************************************************/
    /***********************    OPEN NLP MODELS     ************************/
    /***********************************************************************/

    public static String getTokenModel(){
	return getModelsFolder() + "/" + keyValue.get("tokenModel");
    } 

    public static String getLemmatizerModel(){
	return getModelsFolder() + "/" + keyValue.get("lemmatizerDictonary");
    } 

    public static String getPOSModel(){
	return getModelsFolder() + "/" + keyValue.get("postaggerModel");
    } 

    /***********************************************************************/
    /***********************    SPOTLIGHT FOLDER    ************************/
    /***********************************************************************/
    public static String getSpotlightModel(){
	return getSpotlightFolder() + "/" + getLanguageCode();
    }
    
    public static String getSpotlightJar(){
	return getSpotlightFolder() + "/" + keyValue.get("pathDBSpotLocalJar");
    } 
    
    public static String getSpotlightLocalERR(){
	return getSpotlightFolder() + "/" + keyValue.get("pathDBSpotErr");
    }

    public static String getSpotlightLocalURL(){
	return keyValue.get("pathDBSpotLocalUrl");
    }
    /***********************************************************************/
    /***********************    RESOURCES LIST     ************************/
    /***********************************************************************/

    public static String getCurrenciesList(){
	return getListsFolder() + "/" + keyValue.get("currencies");
    }

    public static String getNationalitiesList(){
	return getListsFolder() + "/" + keyValue.get("nationalities");
    }

    public static String getProfessionsList(){
	return getListsFolder() + "/" + keyValue.get("professions");
    }

    public static String getStopwordsList(){
	return getListsFolder() + "/" + keyValue.get("stopwords");
    }

    /***********************************************************************/
    /**************************    PARAMETERS     **************************/
    /***********************************************************************/

    public static int getNumArticlesToProcess(){
	return Integer.parseInt(keyValue.get("totArticle"));
    }

    public static int getChunkSize(){
	return Integer.parseInt(keyValue.get("chunckSize"));
    }

    public static boolean getOnlyTextWikilinks(){
	return keyValue.get("onlyTextWikilinks").equalsIgnoreCase("true");	    
    }

    public static boolean extractTables(){
	return keyValue.get("extractTables").equalsIgnoreCase("true");	    
    }

    public static boolean extractLists(){
	return keyValue.get("extractLists").equalsIgnoreCase("true");	    
    }

    public static boolean solveRedirect(){
	return keyValue.get("solveRedirect").equalsIgnoreCase("true");	    
    }

    public static double getPronounThreshold(){
	return Double.parseDouble(keyValue.get("pronounDensityThreshold"));	    
    }

    public static double getSubnameThreshold(){
	return Double.parseDouble(keyValue.get("subnameDensityThreshold"));	    
    }

    public static boolean inMemoryProcess(){
	return keyValue.get("inMemory").equalsIgnoreCase("true");
    }

    /***********************************************************************/

}
