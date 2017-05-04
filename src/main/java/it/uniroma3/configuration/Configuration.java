package it.uniroma3.configuration;

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
	/*
	 * get the config file
	 */
	String configFile;
	if (args.length == 0){
	    configFile = "src/main/resources/config.properties";
	}else{
	    configFile = args[0];
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
	    br.close();
	}catch(Exception e){
	    e.printStackTrace();
	}
    }

    /***********************************************************************/
    private static String getDataFolder(){
	return keyValue.get("dataFile");
    }

    private static String getArticlesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("articlesFolder");
	File folder = new File(folderPath); 
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getIndexesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("indexesFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getNormalizedFilesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("normalizedFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getTypesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("typesFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getRelationsFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("relationsFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getOntologyFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("ontologyFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getModelsFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("modelsFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getListsFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("listsFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }

    private static String getTriplesFolder(){
	String folderPath = getDataFolder() + "/" + keyValue.get("triplesFolder");
	File folder = new File(folderPath);
	if(!folder.exists())
	    folder.mkdirs();
	return folder.getAbsolutePath();
    }
    /***********************************************************************/

    /***********************************************************************/
    public static String getOriginalArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("originalArticles");
    } 

    public static String getAugmentedArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("augmentedArticles");
    } 

    public static String getParsedArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("parsedArticles");
    } 

    public static String getDetailArticlesFile(){
	return getArticlesFolder() + "/" + keyValue.get("detailsArticles");
    }
    
    public static Set<String> getPipelineSteps(){
	return new HashSet<String>(Arrays.asList(keyValue.get("pipeline").split(",")));
    }
    
    /***********************************************************************/

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

    public static String getKGIndex(){
	return getIndexesFolder() + "/" + keyValue.get("kgIndexName");
    } 
    /***********************************************************************/

    /***********************************************************************/
    public static String getLanguageCode(){
	return keyValue.get("languageUsed");
    }

    public static String getLanguageProperties(){
	return getDataFolder() + "/languages/" + getLanguageCode() + ".properties";
    }
    /***********************************************************************/

    /***********************************************************************/
    public static String getIndexableRedirectFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("redirectFile");
    } 

    public static String getIndexableDBPediaAirpediaFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("typesAirpediaFile");
    }

    public static String getIndexableDBPediaSDTypedFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("typesSDTypedFile");
    }

    public static String getIndexableDBPediaLHDFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("typesLHDFile");
    }

    public static String getIndexableDBPediaDBTaxFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("typesDBTaxFile");
    }

    public static String getIndexableDBPediaNormalizedTypesFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("normalizedDBpediaTypes");
    }

    public static String getIndexableDBPediaNormalizedRelationsFile(){
	return getNormalizedFilesFolder() + "/" + keyValue.get("normalizedDBpediaRelations");
    }

    /*********************************/

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

    public static String getSourceMappingBasedFile(){
	return getRelationsFolder() + "/" + keyValue.get("mappingBasedDBpediaSource");
    } 

    public static String getDBPediaOntologyFile(){
	return getOntologyFolder() + "/" + keyValue.get("ontology");
    } 
    /***********************************************************************/

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

    /***********************************************************************/

    public static String getStatisticsFile(){
	return getTriplesFolder() + "/" + keyValue.get("stats");
    }
    
    public static String getFactsProducedFile(){
	return getTriplesFolder() + "/" + keyValue.get("facts");
    }
    
    /***********************************************************************/


}
