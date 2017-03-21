package it.uniroma3.configuration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Loads the configuration file and set the parameters.
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
    public static void init(String configFile){
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
	    br.close();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    /***********************************************************************/
    public static String getExperimentFolder(){
	return null;
    } 

    public static String getInputDump50Articles(){
	return null;
    } 
    /***********************************************************************/

    /***********************************************************************/
    private static String getArticlesFolder(){
	return keyValue.get("articlesFolder");
    }

    public static String getOriginalArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("originalArticles");
    } 

    public static String getAugmentedArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("augmentedArticles");
    } 

    public static String getParsedArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("parsedArticles");
    } 

    public static String getTriplifiedArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("triplifiedArticles");
    } 
    /***********************************************************************/

    /***********************************************************************/
    private static String getIndexesFolder(){
	return keyValue.get("indexesFolder");
    }

    public static String getTypesIndex(){
	return getIndexesFolder() + "/" + keyValue.get("typesIndexName");
    } 

    public static String getAirpediaIndex(){
	return getIndexesFolder() + "/" + keyValue.get("airpediaIndexName");
    } 

    public static String getRedirectIndex(){
	return getIndexesFolder() + "/" + keyValue.get("redirectIndexName");
    } 
    /***********************************************************************/

    /***********************************************************************/
    public static String getLanguageCode(){
	return keyValue.get("languageUsed");
    }

    public static String getLanguageProperties(){
	return keyValue.get("languagePropertiesFolder") + "/" 
		+ keyValue.get("languageUsed") + ".properties";
    }
    /***********************************************************************/

    /***********************************************************************/
    private static String getSourcesFolder(){
	return keyValue.get("sourcesFolder");
    }

    public static String getRedirectFile(){
	return getSourcesFolder() + "/" + keyValue.get("redirectFile");
    } 

    public static String getTypesOriginalFile(){
	return getSourcesFolder() + "/" + keyValue.get("typesOriginalFile");
    } 

    public static String getAirpediaFile(){
	return getSourcesFolder() + "/" + keyValue.get("typesAirpediaFile");
    } 
    /***********************************************************************/

    /***********************************************************************/
    private static String getModelsFolder(){
	return keyValue.get("modelsFolder");
    }

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
    
    /***********************************************************************/
    private static String getListsFolder(){
	return keyValue.get("listsFolder");
    }
    
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

    /***********************************************************************/

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

}
