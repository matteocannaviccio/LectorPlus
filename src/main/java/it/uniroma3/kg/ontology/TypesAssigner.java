package it.uniroma3.kg.ontology;

import java.util.List;

public class TypesAssigner {

    private Ontology ontology;
    private TypesResolver resolver;

    public TypesAssigner(){
	this.ontology = new Ontology();
	this.resolver = new TypesResolver();
    }

    /**
     * @return the ontology
     */
    public Ontology getOntology() {
	return ontology;
    }

    /**
     * @return the resolver
     */
    public TypesResolver getResolver() {
	return resolver;
    }

    /**
     * 
     * @param wikid
     * @return
     */
    public List<String> assignTypes(String wikid){
	return resolver.assignTypes(wikid);
    }

    /**
     * 
     * @param wikid
     * @return
     */
    public TGPattern assignTGPattern(String wikid){
	List<String> types = resolver.assignTypes(wikid);
	if (!types.isEmpty()){	// assume we have only one type (true with only dbpedia instance types)
	    return ontology.getTGPattern(types.get(0));
	}
	return null;
    }

}
