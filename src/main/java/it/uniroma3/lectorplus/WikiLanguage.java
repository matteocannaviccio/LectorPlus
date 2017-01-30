package it.uniroma3.lectorplus;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikiLanguage {
    private static final Logger logger = LoggerFactory.getLogger(WikiLanguage.class);
    private Properties properties;
    private static final String SEPARATOR = "=";

    public WikiLanguage(String lang) {
	properties = new Properties();

	try {
	    properties.load(WikiLanguage.class.getResourceAsStream("/lang/locale-"
		    + lang + ".properties"));
	} catch (IOException e) {
	    logger.error("readling the locale for language {} ({})", lang, e.toString());
	    System.exit(-1);
	}

	logger.info("using {} language ",properties.get("language"));

    }

}
