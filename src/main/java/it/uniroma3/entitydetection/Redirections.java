package it.uniroma3.entitydetection;

import org.apache.commons.collections.map.MultiValueMap;

public class Redirections {

    private static MultiValueMap page2targetRedirect = new MultiValueMap();

    /**
     * 
     * @param page
     * @param targetRedirect
     */
    public static void addRedirect(String page, String targetRedirect){
	page2targetRedirect.put(page, targetRedirect);
    }

    /**
     * 
     * @param page
     * @return
     */
    public static String normalizeWikid(String page){
	if (page2targetRedirect.containsKey(page))
	    page = (String) page2targetRedirect.get(page);
	return page;
    }
}
