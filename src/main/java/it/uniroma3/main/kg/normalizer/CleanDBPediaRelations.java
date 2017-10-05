package it.uniroma3.main.kg.normalizer;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author matteo
 *
 */
public class CleanDBPediaRelations {

  Map<String, String> commutative;
  Map<String, String> traslation;

  /**
   * 
   */
  public CleanDBPediaRelations() {
    this.commutative = commutative();
    this.traslation = traslation();
  }

  /**
   * 
   * @return
   */
  private Map<String, String> commutative() {
    Map<String, String> commutative = new HashMap<String, String>();
    commutative.put("associatedBand(-1)", "associatedMusicalArtist");
    commutative.put("associatedMusicalArtist(-1)", "associatedMusicalArtist");
    commutative.put("affiliation(-1)", "affiliation");
    commutative.put("influenced(-1)", "influencedBy");
    commutative.put("influencedBy(-1)", "influenced");
    commutative.put("partner(-1)", "partner");
    commutative.put("related(-1)", "related");
    commutative.put("relation(-1)", "relation");
    commutative.put("relative(-1)", "relative");
    commutative.put("similar(-1)", "similar");
    commutative.put("spouse(-1)", "spouse");
    return commutative;
  }

  /**
   * 
   * @return
   */
  private Map<String, String> traslation() {
    Map<String, String> traslation = new HashMap<String, String>();

    // music domain
    traslation.put("musicalBand", "musicalArtist");
    traslation.put("musicalBand(-1)", "musicalArtist(-1)");
    traslation.put("associatedBand", "associatedMusicalArtist");
    traslation.put("formerBandMember", "bandMember");
    traslation.put("bandMember(-1)", "associatedMusicalArtist");
    traslation.put("formerBandMember(-1)", "associatedMusicalArtist");

    // affiliation can be generalized
    traslation.put("nationalAffiliation", "affiliation");
    traslation.put("nationalAffiliation(-1)", "affiliation");
    traslation.put("internationalAffiliation", "affiliation");
    traslation.put("internationalAffiliation(-1)", "affiliation");
    traslation.put("europeanAffiliation", "affiliation");
    traslation.put("europeanAffiliation(-1)", "affiliation");

    traslation.put("architect(-1)", "significantBuilding");
    traslation.put("significantBuilding(-1)", "architect");

    traslation.put("author(-1)", "notableWork");
    traslation.put("coverArtist(-1)", "notableWork");
    traslation.put("board(-1)", "keyPerson");

    // remove completely education
    traslation.put("education", "almaMater");
    traslation.put("education(-1)", "almaMater(-1)");

    traslation.put("formerBroadcastNetwork", "broadcastNetwork");
    traslation.put("formerBroadcastNetwork(-1)", "broadcastNetwork(-1)");
    traslation.put("formerChoreographer", "choreographer");
    traslation.put("formerChoreographer(-1)", "choreographer(-1)");

    // simplify academic-doctoral relationships
    traslation.put("doctoralAdvisor(-1)", "notableStudent");
    traslation.put("academicAdvisor(-1)", "notableStudent");
    traslation.put("doctoralStudent", "notableStudent");

    traslation.put("doctoralStudent(-1)", "academicAdvisor");
    traslation.put("doctoralAdvisor", "academicAdvisor");
    traslation.put("notableStudent(-1)", "academicAdvisor");

    traslation.put("ethnicGroup(-1)", "populationPlace");
    traslation.put("locationCountry", "country");
    traslation.put("locationCountry(-1)", "country(-1)");
    traslation.put("locationCity", "city");
    traslation.put("locationCity(-1)", "city(-1)");
    traslation.put("locatedInArea", "location");
    traslation.put("locatedInArea(-1)", "location(-1)");

    traslation.put("leftTributary(-1)", "riverMouth");
    traslation.put("rightTributary(-1)", "riverMouth");
    traslation.put("inflow(-1)", "outflow");
    traslation.put("outflow(-1)", "inflow");

    traslation.put("lieutenant(-1)", "governor");
    traslation.put("governor(-1)", "lieutenant");
    traslation.put("majorIsland(-1)", "archipelago");

    traslation.put("developer(-1)", "product");
    traslation.put("product(-1)", "developer");

    // team <--> coach
    traslation.put("coach", "manager");
    traslation.put("coach(-1)", "team");
    traslation.put("formerCoach", "manager");
    traslation.put("formerCoach(-1)", "team");
    traslation.put("manager(-1)", "team");
    traslation.put("managerClub", "team");
    traslation.put("managerClub(-1)", "manager");
    traslation.put("coachedTeam", "team");
    traslation.put("coachedTeam(-1)", "manager");
    traslation.put("generalManager(-1)", "team");
    traslation.put("generalManager", "manager");
    traslation.put("club", "team");
    traslation.put("club(-1)", "team(-1)");
    traslation.put("draftTeam", "team");
    traslation.put("draftTeam(-1)", "team(-1)");
    traslation.put("debutTeam", "team");
    traslation.put("debutTeam(-1)", "team(-1)");
    traslation.put("formerTeam", "team");
    traslation.put("formerTeam(-1)", "team(-1)");
    traslation.put("prospectTeam", "team");
    traslation.put("prospectTeam(-1)", "team(-1)");
    traslation.put("natonalTeam", "team");
    traslation.put("natonalTeam(-1)", "team(-1)");

    traslation.put("notableWork(-1)", "author");

    traslation.put("officialLanguage", "language");
    traslation.put("regionalLanguage", "language");
    traslation.put("language(-1)", "spokenIn");
    traslation.put("officialLanguage(-1)", "spokenIn");
    traslation.put("regionalLanguage(-1)", "spokenIn");

    traslation.put("child(-1)", "parent");
    traslation.put("parent(-1)", "child");

    // companies ad organisation hierarchy
    traslation.put("division(-1)", "parentOrganisation");
    traslation.put("childOrganisation(-1)", "parentOrganisation");
    traslation.put("subsidiary(-1)", "parentOrganisation");

    traslation.put("owningCompany", "owningOrganisation");
    traslation.put("owningCompany(-1)", "owningOrganisation(-1)");

    traslation.put("designCompany", "designer");
    traslation.put("designCompany(-1)", "designer(-1)");

    traslation.put("parentOrganisation(-1)", "childOrganisation");
    traslation.put("parentCompany(-1)", "childOrganisation");

    traslation.put("otherParty", "party");
    traslation.put("otherParty(-1)", "party(-1)");

    // temporal
    traslation.put("previousEvent(-1)", "followingEvent");
    traslation.put("previousWork(-1)", "subsequentWork");
    traslation.put("subsequentWork(-1)", "previousWork");
    traslation.put("successor(-1)", "predecessor");
    traslation.put("predecessor(-1)", "successor");

    traslation.put("primeMinister(-1)", "president(-1)");
    traslation.put("primeMinister", "president");
    traslation.put("vicePresident(-1)", "president");

    traslation.put("part(-1)", "isPartOf");
    traslation.put("isPartOf(-1)", "part");

    traslation.put("relatedMeanOfTransportation(-1)", "similar");
    traslation.put("relatedMeanOfTransportation", "similar");
    traslation.put("sisterStation", "similar");
    traslation.put("sisterStation(-1)", "similar");
    traslation.put("sisterNewspaper", "similar");
    traslation.put("sisterNewspaper(-1)", "similar");
    traslation.put("sisterCollege", "similar");
    traslation.put("sisterCollege(-1)", "similar");

    traslation.put("splitFromParty(-1)", "mergedIntoParty");
    traslation.put("stadium(-1)", "tenant");
    traslation.put("homeStadium", "stadium");
    traslation.put("homeStadium(-1)", "tenant");
    traslation.put("homeArena", "stadium");
    traslation.put("homeArena(-1)", "tenant");

    traslation.put("formerPartner", "partner");
    traslation.put("formerPartner(-1)", "partner");
    traslation.put("currentPartner", "partner");
    traslation.put("currentPartner(-1)", "partner");

    traslation.put("stadium(-1)", "tenant");
    traslation.put("splitFromParty(-1)", "mergedIntoParty");
    traslation.put("stadium(-1)", "tenant");

    traslation.put("foundedBy(-1)", "board");
    traslation.put("board(-1)", "foundedBy");

    return traslation;
  }

  /**
   * 
   * @param relation
   * @return
   */
  private static String checksIsGood(String relation) {
    if (relation.contains("nearest") || relation.contains("highest") || relation.contains("lowest"))
      return "notValid";
    else
      return relation;
  }

  /**
   * 
   * @param originalRelation
   * @return
   */
  public String cleanRelation(String originalRelation) {
    String finalRelation = originalRelation;
    if (this.traslation.containsKey(originalRelation)) {
      finalRelation = this.traslation.get(originalRelation);
    }
    if (this.commutative.containsKey(originalRelation)) {
      finalRelation = this.commutative.get(originalRelation);
    }
    return checksIsGood(finalRelation);
  }

}
