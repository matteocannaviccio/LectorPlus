package it.uniroma3.model.paper;

import java.util.HashMap;
import java.util.Map;

public class Questions {

  public static Map<String, String> specialQuestions = fillMap();

  public static Map<String, String>  fillMap(){
    Map<String, String> specialQuestions = new HashMap<>();
    specialQuestions.put("artist", "the artist of");
    specialQuestions.put("affiliation", "affiliated (or related) with");
    specialQuestions.put("archipelago", "the archipelago of");
    specialQuestions.put("author", "the author of");
    specialQuestions.put("associatedMusicalArtist", "a musical artist associated to");
    specialQuestions.put("country", "the country of");
    specialQuestions.put("almaMater", "a school/university attended by");
    specialQuestions.put("award", "an award/reward obtained by");
    specialQuestions.put("battle", "a battle that involved");
    specialQuestions.put("board", "managed by"); 
    specialQuestions.put("bandMember", "a member of the band");
    specialQuestions.put("birthPlace", "the birthplace of");
    specialQuestions.put("broadcastArea", "the broadcast area of");
    specialQuestions.put("basedOn", "based on");
    specialQuestions.put("capital", "the capital city of");
    specialQuestions.put("childOrganisation", "a child organisation of");
    specialQuestions.put("city", "a city related to");
    specialQuestions.put("canton", "a canton of");
    specialQuestions.put("commander", "a commander involved in");
    specialQuestions.put("country", "the country (or nationality) of");
    specialQuestions.put("company", "the company that produces");
    specialQuestions.put("creator", "the creator of");
    specialQuestions.put("deathPlace", "the deathplace of");
    specialQuestions.put("developer", "the developer of");
    specialQuestions.put("director", "the director of");
    specialQuestions.put("district", "a district of");
    specialQuestions.put("ethnicity", "the ethnicity of");
    specialQuestions.put("editor", "an editor of");
    specialQuestions.put("employer", "the employer of");
    specialQuestions.put("ethnicGroup", "an ethnic group of");
    specialQuestions.put("endPoint", "the end point of");
    specialQuestions.put("garrison", "a garrison for");
    specialQuestions.put("governmentType", "the type of government of");
    specialQuestions.put("governingBody", "the governing body of");
    specialQuestions.put("genre", "the genre of");
    specialQuestions.put("ground", "a place related to");
    specialQuestions.put("family", "in the family of");
    specialQuestions.put("foundedBy", "founded by");
    specialQuestions.put("headquarter", "the headquarter of");
    specialQuestions.put("hometown", "the hometown of");
    specialQuestions.put("influenced", "influenced by");
    specialQuestions.put("influencedBy", "influenced by");
    specialQuestions.put("keyPerson", "an important person for");

    specialQuestions.put("isPartOf", "contained in");
    specialQuestions.put("part", "contained in");

    specialQuestions.put("industry", "working in the industry of");
    specialQuestions.put("isPartOfMilitaryConflict", "involved in the battle of");
    specialQuestions.put("knownFor", "known for");
    specialQuestions.put("languageFamily", "a language in the family of");
    specialQuestions.put("ethnicity", "the ethnicity of");
    specialQuestions.put("location", "the location of");
    specialQuestions.put("network", "the network of");
    specialQuestions.put("nationality", "the nationality of");
    specialQuestions.put("notableCommander", "a commander of");
    specialQuestions.put("occupation", "the occupation of");
    specialQuestions.put("location", "the location of");
    specialQuestions.put("leader", "the leader of");
    specialQuestions.put("largestSettlement", "the largest settlement of");
    specialQuestions.put("leaderParty", "the leader of");
    specialQuestions.put("league", "a league played by");
    specialQuestions.put("mountainRange", "the mountains containing");
    specialQuestions.put("manager", "the manager of");
    specialQuestions.put("manufacturer", "the manufacturer of");
    specialQuestions.put("militaryBranch", "a military branch of");
    specialQuestions.put("militaryUnit", "a military unit of");
    specialQuestions.put("musicalArtist", "the artist of");
    specialQuestions.put("neighboringMunicipality", "a neighborood of");
    specialQuestions.put("operator", "operating for");
    specialQuestions.put("parent", "a parent of");
    specialQuestions.put("parentCompany", "a parent company of");
    specialQuestions.put("parentOrganisation", "a parent organization of");
    specialQuestions.put("placeOfBurial", "the place of burial of");
    specialQuestions.put("regionServed", "a region served by");
    specialQuestions.put("religion", "the religion of");
    specialQuestions.put("spouse", "married with");
    specialQuestions.put("partner", "the partner of");
    specialQuestions.put("party", "the party of");
    specialQuestions.put("politicalPartyInLegislature", "a party involved in");
    specialQuestions.put("presenter", "the presenter of");
    specialQuestions.put("president", "the president of");
    specialQuestions.put("profession", "the profession of");
    specialQuestions.put("riverMouth", "the body of water at the end of");
    specialQuestions.put("routeStart", "a intersection of");
    specialQuestions.put("season", "a season played by");
    specialQuestions.put("servingRailwayLine", "a rail line for");
    specialQuestions.put("spokenIn", "spoken in");
    specialQuestions.put("significantProject", "a project of");
    specialQuestions.put("child", "a child of");
    specialQuestions.put("owner", "the owner of");
    specialQuestions.put("picture", "a picture of");
    specialQuestions.put("place", "the place of");
    specialQuestions.put("populationPlace", "the place lived by");
    specialQuestions.put("region", "the region of");
    specialQuestions.put("product", "a product of");
    specialQuestions.put("person", "a person related to");
    specialQuestions.put("relative", "a relative of");
    specialQuestions.put("residence", "the residence of");
    specialQuestions.put("restingPlace", "the resting place of");
    specialQuestions.put("routeJunction", "a junction of");
    specialQuestions.put("school", "the school of");
    specialQuestions.put("similar", "similar to");
    specialQuestions.put("splitFromParty", "splited from");
    specialQuestions.put("similar", "similar to");
    specialQuestions.put("state", "the state of");
    specialQuestions.put("sourceCountry", "the source place of");
    specialQuestions.put("subsequentWork", "the subsequent work of");
    specialQuestions.put("successor", "the successor of");
    specialQuestions.put("starring", "an actor of");  
    specialQuestions.put("type", "the type/category of");
    specialQuestions.put("territory", "the territory of");
    specialQuestions.put("trainer", "the trainer of");
    specialQuestions.put("team", "the team of");
    specialQuestions.put("successor", "the successor of");
    return specialQuestions;
  }

  /**
   * 
   * @param relation
   * @return 
   */
  public static String getQuestion(String subject, String relation, String object){
    String raw = relation.replaceAll("\\(-1\\)", "");
    String text = "the " + raw + " of";
    if (specialQuestions.containsKey(raw))
      text = specialQuestions.get(raw);

    String question;

    if (relation.equals("isPartOf") || relation.equals("part"))
      question = "is " + subject + " " + text + " " + object + "?";
    else{
      if (relation.contains("(-1)"))
        question = "is " + subject + " " + text + " " + object + "?";
      else
        question = "is " + object + " " + text + " " + subject + "?";
    }

    return question;
  }
}
