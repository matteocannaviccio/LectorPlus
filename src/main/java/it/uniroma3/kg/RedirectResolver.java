package it.uniroma3.kg;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import it.uniroma3.configuration.Configuration;

public class RedirectResolver {

    private KeyValueIndex indexRedirect;

    /**
     * 
     */
    public RedirectResolver(){
	if (!new File(Configuration.getRedirectIndex()).exists()){
	    System.out.print("Creating REDIRECT resolver ...");
	    long start_time = System.currentTimeMillis();
	    this.indexRedirect = new KeyValueIndex(Configuration.getRedirectFile(), Configuration.getRedirectIndex());
	    long end_time = System.currentTimeMillis();
	    System.out.println(" done in " + TimeUnit.MILLISECONDS.toSeconds(end_time - start_time)  + " sec.");
	}
	else // we already have the index
	    this.indexRedirect = new KeyValueIndex(Configuration.getRedirectIndex());

    }

    /**
     * 
     * @param possibleRedirect
     * @return
     */
    public String resolveRedirect(String possibleRedirect){
	String targetPage = possibleRedirect;
	Optional<String> target = indexRedirect.retrieveKeys(possibleRedirect).stream().findFirst();
	if (target.isPresent())
	    targetPage = target.get();
	return targetPage;
    }
}
