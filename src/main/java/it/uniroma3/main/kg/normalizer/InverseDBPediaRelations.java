package it.uniroma3.main.kg.normalizer;

import java.util.HashMap;
import java.util.Map;

public class InverseDBPediaRelations {
    
    public static Map<String, String> inverse(){
	Map<String, String> inverse = new HashMap<String, String>();
	inverse.put("associatedBand(-1)", "associatedMusicalArtist");
	inverse.put("associatedMusicalArtist(-1)", "associatedMusicalArtist");
	inverse.put("associatedBand", "associatedMusicalArtist");
	inverse.put("architect(-1)","significantBuilding");
	inverse.put("author(-1)","notableWork");
	inverse.put("board(-1)","keyPerson");
	inverse.put("child(-1)","parent");
	inverse.put("childOrganisation(-1)","parentOrganisation");
	inverse.put("coverArtist(-1)","notableWork");
	inverse.put("division(-1)","parentCompany");
	inverse.put("doctoralAdvisor(-1)","doctoralStudent");
	inverse.put("doctoralStudent(-1)","doctoralAdvisor");
	inverse.put("ethnicGroup(-1)","populationPlace");
	inverse.put("influenced(-1)","influencedBy");
	inverse.put("influencedBy(-1)","influenced");
	inverse.put("leftTributary(-1)","riverMouth");
	inverse.put("lieutenant(-1)","governor");
	inverse.put("majorIsland(-1)","archipelago");
	inverse.put("manager(-1)","managerClub");
	inverse.put("managerClub(-1)","manager");
	inverse.put("notableStudent(-1)","academicAdvisor");
	inverse.put("notableWork(-1)","author");
	inverse.put("officialLanguage(-1)","spokenIn");
	inverse.put("parent(-1)","child");
	inverse.put("parentCompany(-1)","subsidiary");
	inverse.put("partner(-1)","partner");
	inverse.put("predecessor(-1)","successor");
	inverse.put("previousEvent(-1)","followingEvent");
	inverse.put("previousWork(-1)","subsequentWork");
	inverse.put("primeMinister(-1)","successor");
	inverse.put("regionalLanguage(-1)","spokenIn");
	inverse.put("related(-1)","related");
	inverse.put("relatedMeanOfTransportation(-1)","relatedMeanOfTransportation");
	inverse.put("relation(-1)","relation");
	inverse.put("relative(-1)","relative");
	inverse.put("rightTributary(-1)","riverMouth");
	inverse.put("riverMouth(-1)","rightTributary");
	inverse.put("significantBuilding(-1)","architect");
	inverse.put("similar(-1)","sisterStation");
	inverse.put("sisterStation(-1)","sisterStation");
	inverse.put("splitFromParty(-1)","mergedIntoParty");
	inverse.put("spouse(-1)","spouse");
	inverse.put("stadium(-1)","tenant");
	inverse.put("subsidiary(-1)","parentCompany");
	inverse.put("successor(-1)","predecessor");
	inverse.put("vicePresident(-1)","president");
	return inverse;
    }
    
}
