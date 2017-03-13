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
 * 
 * 
 * 
 * @author matteo
 *
 */
public class Configuration {

    // keeps everything that is read from the file or the overriding options
    public static Map<String, String> keyValue = new TreeMap<String, String>();

    /**
     * 
     * @param configFile
     */
    public static void setConfigFile(String configFile){
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

    public static String getOutputFolder(){
	return keyValue.get("outputFolder");
    }

    public static int getChunkSize(){
	return Integer.parseInt(keyValue.get("chunckSize"));
    }

    public static String getLanguagePropertiesFolder(){
	return keyValue.get("languagePropertiesFolder");
    }

    public static String getInputWikipediaDump(){
	return keyValue.get("inputWikipediaDump");
    }

    public static String getTestWikipediaDump(){
	return keyValue.get("testWikipediaArticle");
    }

    public static String getLanguageUsed(){
	return keyValue.get("languageUsed");
    }

    public static String getTokenModel(){
	return keyValue.get("tokenModel");
    }

    public static String getSentenceModel(){
	return keyValue.get("sentencesModel");
    }

    public static String getPosTagModel(){
	return keyValue.get("postaggerModel");
    }

    public static String getLemmaDictionary(){
	return keyValue.get("lemmatizerDictonary");
    }

    public static String getRedirectFile(){
	return keyValue.get("redirectFile");
    }

    public static String getRedirectIndex(){
	return keyValue.get("redirectIndex");
    }

    public static String getTypesFileAirpedia(){
	return keyValue.get("typesFileAirpedia");
    }

    public static String getTypesIndexAirpedia(){
	return keyValue.get("typesIndexAirpedia");
    }
    
    public static String getTypesFileOriginal(){
	return keyValue.get("typesFileOriginal");
    }

    public static String getTypesIndexOriginal(){
	return keyValue.get("typesIndexOriginal");
    }

    public static boolean getOnlyAbstractFlag(){
	return keyValue.get("onlyAbstract").equals("true");	    
    }

    public static boolean getOnlyTextWikilinks(){
	return keyValue.get("onlyTextWikilinks").equals("true");	    
    }

    public static String getExperimentFolder(){
	return keyValue.get("experimentFolder");
    }

    public static String getInputDump50Articles(){
	return keyValue.get("miniDumpExp");
    }

    public static boolean getEDTestingMode(){
	return keyValue.get("edTestingMode").equals("true");	    
    }

    public static boolean extractTables(){
	return keyValue.get("extractTables").equals("true");	    
    }

    public static boolean extractLists(){
	return keyValue.get("extractLists").equals("true");	    
    }

    public static boolean solveRedirect(){
	return keyValue.get("solveRedirect").equals("true");	    
    }
}
