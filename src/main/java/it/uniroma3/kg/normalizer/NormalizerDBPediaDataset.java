package it.uniroma3.kg.normalizer;

public class NormalizerDBPediaDataset {
    
    /**
     * 
     * @param uri
     * @return
     */
    protected static String getResourceName(String uri){
	String namespace = "http://dbpedia.org/resource/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    protected static String getPredicateName(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.replace(namespace, "");
    }

    /**
     * 
     * @param uri
     * @return
     */
    protected static boolean isDBPediaResource(String uri){
	String namespace = "http://dbpedia.org/resource/";
	return uri.contains(namespace);
    }
    
    /**
     * 
     * @param uri
     * @return
     */
    protected static boolean isIntermediateNode(String uri){
	return uri.contains("__");
    }
    

    /**
     * 
     * @param uri
     * @return
     */
    protected static boolean isInDBPediaOntology(String uri){
	String namespace = "http://dbpedia.org/ontology/";
	return uri.contains(namespace);
    }

}
