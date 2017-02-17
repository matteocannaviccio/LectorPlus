package it.uniroma3.components;

import java.util.Optional;

import it.uniroma3.configuration.Configuration;

public class RedirectResolver {
    
    private static KeyValueIndex indexRedirect = new KeyValueIndex(Configuration.getRedirectFile(), Configuration.getRedirectIndex());

    /**
     * 
     * @param possibleRedirect
     * @return
     */
    public static String getTargetPage(String possibleRedirect){
	String targetPage = possibleRedirect;
	Optional<String> target = indexRedirect.retrieveKeys(possibleRedirect).stream().findFirst();
	if (target.isPresent())
	    targetPage = target.get();
	return targetPage;
	
    }
}
