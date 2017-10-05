package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import it.uniroma3.config.Configuration;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.bean.WikiTriple.TType;
import it.uniroma3.main.util.CounterMap;
import it.uniroma3.main.util.Pair;

/**
 * This class extends a generic DB introducing methods to handle a model table.
 * 
 * Here we handle this table only because is the only one in common between a: - DBModel -
 * DBCrossValidation
 * 
 * It has the following schema: phrase_placeholder TEXT type_subject TEXT type_object TEXT relation
 * TEXT occurrences INT
 * 
 * We provide methods to insert a batch of entries in the model table or retrieve the whole
 * evidence.
 * 
 * 
 * @author matteo
 *
 */
public class DBLector extends DB {

  /**
   * Create a db for lector process
   * 
   * @param dbname
   */
  public DBLector(String dbname) {
    super(dbname, Configuration.inMemoryProcess());
  }

  /**
   * This is the schema of the table. We need to specify the name in order to use it with a
   * cross-validation db.
   * 
   * @param table_name
   */
  protected void createSchemaModelTriples(String table_name) {
    String dropLabeled = "DROP TABLE IF EXISTS " + table_name;
    String createLabeled = "CREATE TABLE " + table_name + "(" + "phrase_placeholder text, "
        + "type_subject text, " + "type_object text, " + "relation text, " + "occurrences int)";
    String createIndexModel = "CREATE UNIQUE INDEX IF NOT EXISTS index_" + table_name + " ON "
        + table_name + "(phrase_placeholder, type_subject, type_object, relation)";

    try (Statement stmt = this.getConnection().createStatement()) {
      stmt.executeUpdate(dropLabeled);
      stmt.executeUpdate(createLabeled);
      stmt.executeUpdate(createIndexModel);

    } catch (SQLException e) {
      try {
        this.getConnection().rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
      e.printStackTrace();
    }
  }

  /**
   * 
   * @param list
   */
  protected void batchInsertModelEntry(String table_name, List<String> entries) {
    String insert = "INSERT OR REPLACE INTO " + table_name + " VALUES (?, ?, ?, ?, "
        + "COALESCE((SELECT occurrences FROM " + table_name + " WHERE phrase_placeholder=? "
        + "AND type_subject=? " + "AND type_object=? " + "AND relation=?), 0) + 1)";

    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (String entry : entries) {
        String phrase_placeholder = entry.split("\t")[0];
        String type_sbj = entry.split("\t")[1];
        String type_obj = entry.split("\t")[2];
        String relation = entry.split("\t")[3];

        stmt.setString(1, phrase_placeholder);
        stmt.setString(2, type_sbj);
        stmt.setString(3, type_obj);
        stmt.setString(4, relation);
        stmt.setString(5, phrase_placeholder);
        stmt.setString(6, type_sbj);
        stmt.setString(7, type_obj);
        stmt.setString(8, relation);
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
      this.getConnection().setAutoCommit(true);
    } catch (SQLException e) {
      try {
        this.getConnection().rollback();
      } catch (SQLException e1) {
        e1.printStackTrace();
      }
      e.printStackTrace();
    }
  }


  /**
   * Returns typed phrases, relations and their counts from the Model Table.
   * 
   * [Settlement] located in [Settlement] --- isPartOf , 675 K [Settlement] surrounded by [City] ---
   * location , 342 K [Settlement] in [Settlement] --- isPartOf , 3444 K [Settlement] by [City] ---
   * <NONE> , 234
   * 
   * @param tableName the name of the mmodel_triples (useful in crossvalidation)
   * @param minF the minimum frequency to consider a typed phrase
   * @param percentageUnlabeled the percentage of unlabeled (100% is WithNone, 0% is OnlyPositive)
   * 
   * @return
   */
  public CounterMap<String> retrieveEvidence(String tableName, int minF, int percentageUnlabeled) {

    CounterMap<String> evidence = new CounterMap<String>();
    CounterMap<String> evidence_unlabeled = new CounterMap<String>();

    String query = "SELECT phrase_placeholder, type_subject, type_object, "
        + "relation, occurrences FROM " + tableName + " WHERE occurrences>?";

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      stmt.setInt(1, minF);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String phrase_placeholder = rs.getString(1);
          String type_subject = rs.getString(2);
          String type_object = rs.getString(3);
          String typed_phrase = type_subject + "\t" + phrase_placeholder + "\t" + type_object;
          String relation = rs.getString(4);
          int occurrences = rs.getInt(5);
          if (!relation.equals("NONE"))
            evidence.add(typed_phrase + "\t" + relation, occurrences);
          else
            evidence_unlabeled.add(typed_phrase + "\t" + relation, occurrences);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // discount the unlabeled, if needed
    evidence.addAll(evidence_unlabeled.filterPercentageRandom(percentageUnlabeled));

    return evidence;
  }

  /**
   * 
   * @param tableName
   * @param relation
   * @param minF
   * @return
   */
  public List<String> retrieveLabeled4SpecificRelation(String tableName, String relation,
      int minF) {
    List<String> evidence = new LinkedList<String>();

    String query = "SELECT phrase_placeholder, type_subject, type_object, "
        + "relation, occurrences FROM " + tableName + " WHERE occurrences>? AND relation=?";

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      stmt.setInt(1, minF);
      stmt.setString(2, relation);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String phrase_placeholder = rs.getString(1);
          String type_subject = rs.getString(2);
          String type_object = rs.getString(3);
          String typed_phrase = type_subject + "\t" + phrase_placeholder + "\t" + type_object;
          String rel = rs.getString(4);
          int occurrences = rs.getInt(5);
          for (int i = 0; i < occurrences; i++)
            evidence.add(typed_phrase + "\t" + rel);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return evidence;
  }

  /**
   * 01- wikid text 02- section text 03- phrase_original text 04- phrase_placeholder text 05-
   * phrase_pre text 06- phrase_post text 07- subject text 08- wiki_subject text 09- type_subject
   * text 10- object text 11- wiki_object text 12- type_object text 13- relation text
   * 
   * @param tableName
   * @param relation
   * @param minF
   * @return
   */
  public List<Pair<WikiTriple, String>> retrieveLabeledTriples(String tableName) {
    List<Pair<WikiTriple, String>> evidence = new LinkedList<Pair<WikiTriple, String>>();

    String query = "SELECT * FROM " + tableName;

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String wikid = rs.getString(1);
          String section = rs.getString(2);
          String phrase_original = rs.getString(3);
          String phrase_placeholder = rs.getString(4);
          String phrase_pre = rs.getString(5);
          String phrase_post = rs.getString(6);
          String subject = rs.getString(7);

          String type_subject = rs.getString(9);
          String object = rs.getString(10);

          String type_object = rs.getString(12);
          String relation = rs.getString(13);

          WikiTriple t =
              new WikiTriple(wikid, section, "", phrase_original, phrase_placeholder, phrase_pre,
                  phrase_post, subject, object, type_subject, type_object, TType.JOINABLE.name());

          evidence.add(Pair.make(t, relation));

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return evidence;
  }

  /**
   * 01- wikid text 02- section text, 03- sentence text 04- phrase_original text 05-
   * phrase_placeholder text 06- phrase_pre text 07- phrase_post text 08- subject text 09-
   * wiki_subject text 10- type_subject text 11- object text 12- wiki_object text 13- type_object
   * text
   * 
   * @param tableName
   * @param relation
   * @param minF
   * @return
   */
  public List<WikiTriple> retrieveUnlabeledTriples(String tableName) {
    List<WikiTriple> evidence = new LinkedList<WikiTriple>();

    String query = "SELECT * FROM " + tableName;

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String wikid = rs.getString(1);
          String section = rs.getString(2);
          String sentence = rs.getString(3);
          String phrase_original = rs.getString(4);
          String phrase_placeholder = rs.getString(5);
          String phrase_pre = rs.getString(6);
          String phrase_post = rs.getString(7);
          String subject = rs.getString(8);

          String type_subject = rs.getString(10);
          String object = rs.getString(11);

          String type_object = rs.getString(13);

          WikiTriple t = new WikiTriple(wikid, section, sentence, phrase_original,
              phrase_placeholder, phrase_pre, phrase_post, subject, object, type_subject,
              type_object, TType.JOINABLE.name());

          evidence.add(t);

        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return evidence;
  }


}
