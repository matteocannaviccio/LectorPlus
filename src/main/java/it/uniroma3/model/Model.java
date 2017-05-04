package it.uniroma3.model;

import it.uniroma3.triples.WikiTriple;

public abstract class Model{

    /**
     * This is the only method that can be used from the clients.
     * 
     * @param phrase
     * @return
     */
    protected abstract String predictRelation(WikiTriple t);

}
