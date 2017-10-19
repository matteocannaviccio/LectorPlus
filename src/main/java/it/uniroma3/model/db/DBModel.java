package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.apache.commons.lang3.StringUtils;
import it.uniroma3.main.bean.WikiMVL;
import it.uniroma3.main.bean.WikiTriple;
import it.uniroma3.main.util.Pair;

/**
 * This is the DB produced by Lector's pipeline on a given Wikipedia dump.
 * 
 * It includes four tables: - labeled_triples - unlabeled_triples - other_triples - mvl_collection -
 * nationality_collection
 * 
 * And one for the model: - model
 * 
 * 
 * @author matteo
 *
 */
public class DBModel extends DBLector {

  public static String labeled_table = "labeled_triples";
  public static String unlabeled_table = "unlabeled_triples";
  public static String model_triples = "model_triples";
  public static String other_table = "other_triples";
  public static String mvl_table = "mvl_collection";
  public static String nationality_table = "nationality_collection";

  /**
   * Create a db evidence
   * 
   * @param dbname
   */
  public DBModel(String dbname) {
    super(dbname);
  }

  /**
   * 
   */
  public void createDB() {
    String dropLabeled = "DROP TABLE IF EXISTS " + labeled_table;
    String createLabeled = "CREATE TABLE " + labeled_table + "(" + "wikid text, " + "section text, "
        + "phrase_original text, " + "phrase_placeholder text, " + "phrase_pre text, "
        + "phrase_post text, " + "subject text, " + "wiki_subject text, " + "type_subject text, "
        + "object text, " + "wiki_object text, " + "type_object text, " + "relation text)";

    String dropUnlabeled = "DROP TABLE IF EXISTS " + unlabeled_table;
    String createUnlabeled =
        "CREATE TABLE " + unlabeled_table + "(" + "wikid text, " + "section text, "
            + "sentence text, " + "phrase_original text, " + "phrase_placeholder text, "
            + "phrase_pre text, " + "phrase_post text, " + "subject text, " + "wiki_subject text, "
            + "type_subject text, " + "object text, " + "wiki_object text, " + "type_object text)";

    String dropOther = "DROP TABLE IF EXISTS " + other_table;
    String createOther = "CREATE TABLE " + other_table + "(" + "wikid text, " + "section text, "
        + "phrase_original text, " + "phrase_placeholder text, " + "phrase_pre text, "
        + "phrase_post text, " + "subject text, " + "wiki_subject text, " + "type_subject text, "
        + "object text, " + "wiki_object text, " + "type_object text, " + "type text)";

    String dropMVLCollection = "DROP TABLE IF EXISTS " + mvl_table;
    String createMVLCollection = "CREATE TABLE " + mvl_table + "(" + "code text, " + "wikid text, "
        + "section text, " + "list text)";

    String dropNationalitiesCollection = "DROP TABLE IF EXISTS " + nationality_table;
    String createNationalitiesCollection = "CREATE TABLE " + nationality_table + "("
        + "wikid text, " + "sentence text, " + "subject_type text, " + "object text)";

    try (Statement stmt = this.getConnection().createStatement()) {

      stmt.executeUpdate(dropLabeled);
      stmt.executeUpdate(createLabeled);
      stmt.executeUpdate(dropUnlabeled);
      stmt.executeUpdate(createUnlabeled);
      stmt.executeUpdate(dropOther);
      stmt.executeUpdate(createOther);
      stmt.executeUpdate(dropMVLCollection);
      stmt.executeUpdate(createMVLCollection);
      stmt.executeUpdate(dropNationalitiesCollection);
      stmt.executeUpdate(createNationalitiesCollection);

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
   */
  public void createNecessaryIndexes() {
    if (!checkIndexExists("indexmodelrelationphrase") || !checkIndexExists("indexmodelphrase")
        || !checkIndexExists("indexmodeltypesphrase") || !checkIndexExists("indexunlabeledphrase")
        || !checkIndexExists("indexother")) {

      System.out.print("\t-> Creating indexes ... ");

      String indexModelRelationPhrase = "CREATE INDEX IF NOT EXISTS indexmodelrelationphrase "
          + "ON labeled_triples(relation, phrase_placeholder)";
      String indexModelPhrase =
          "CREATE INDEX IF NOT EXISTS indexmodelphrase " + "ON labeled_triples(phrase_placeholder)";
      String indexModelTypesPhrase = "CREATE INDEX IF NOT EXISTS indexmodeltypesphrase "
          + "ON labeled_triples(type_subject, type_object, relation, phrase_placeholder)";
      String indexUnlabeledPhrase = "CREATE INDEX IF NOT EXISTS indexunlabeledphrase "
          + "ON unlabeled_triples(phrase_placeholder, type_subject, type_object)";
      String indexOther = "CREATE INDEX IF NOT EXISTS indexother " + "ON other_triples(type)";

      try (Statement stmt = this.getConnection().createStatement()) {
        stmt.executeUpdate(indexModelRelationPhrase);
        stmt.executeUpdate(indexModelPhrase);
        stmt.executeUpdate(indexModelTypesPhrase);
        stmt.executeUpdate(indexOther);
        stmt.executeUpdate(indexUnlabeledPhrase);

      } catch (SQLException e) {
        try {
          this.getConnection().rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
        e.printStackTrace();
      }
      System.out.println("Done");
    }
  }

  /**
   * 
   * @param tableName
   * @param indexName
   */
  private void createIndexForAll(String tableName, String indexName) {
    if (!checkIndexExists(indexName)) {
      System.out.print("\t-> Creating indexs for all " + tableName + " ... ");
      String index = "CREATE INDEX IF NOT EXISTS " + indexName + " " + "ON " + tableName
          + "(phrase_placeholder, wiki_subject, wiki_object, type_subject, type_object)";
      try (Statement stmt = this.getConnection().createStatement()) {
        stmt.executeUpdate(index);
      } catch (SQLException e) {
        try {
          this.getConnection().rollback();
        } catch (SQLException e1) {
          e1.printStackTrace();
        }
        e.printStackTrace();
      }
      System.out.println("Done");
    }
  }

  /**
   * This is the schema of labeled_triples:
   * 
   * 01- wikid text 02- section text 03- phrase_original text 04- phrase_placeholder text 05-
   * phrase_pre text 06- phrase_post text 07- subject text 08- wiki_subject text 09- type_subject
   * text 10- object text 11- wiki_object text 12- type_object text 13- relation text
   * 
   * @param triple
   * @param relation
   */
  public void batchInsertLabeledTriple(Queue<Pair<WikiTriple, String>> labeled_triples) {
    String insert = "INSERT INTO " + labeled_table + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (Pair<WikiTriple, String> triple : labeled_triples) {
        stmt.setString(1, triple.key.getWikid());
        stmt.setString(2, triple.key.getSection());
        stmt.setString(3, triple.key.getPhraseOriginal());
        stmt.setString(4, triple.key.getPhrasePlaceholders());
        stmt.setString(5, triple.key.getPre());
        stmt.setString(6, triple.key.getPost());
        stmt.setString(7, triple.key.getSubject());
        stmt.setString(8, triple.key.getWikiSubject());
        stmt.setString(9, triple.key.getSubjectType());
        stmt.setString(10, triple.key.getObject());
        stmt.setString(11, triple.key.getWikiObject());
        stmt.setString(12, triple.key.getObjectType());
        stmt.setString(13, triple.value);
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
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
   * This is the schema of unlabeled_triples:
   * 
   * 01- wikid text 02- section text, 03- sentence text 04- phrase_original text 05-
   * phrase_placeholder text 06- phrase_pre text 07- phrase_post text 08- subject text 09-
   * wiki_subject text 10- type_subject text 11- object text 12- wiki_object text 13- type_object
   * text
   * 
   * @param triple
   */
  public void batchInsertUnlabeledTriple(Queue<WikiTriple> unlabeled_triples) {
    String insert = "INSERT INTO " + unlabeled_table + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (WikiTriple triple : unlabeled_triples) {
        stmt.setString(1, triple.getWikid());
        stmt.setString(2, triple.getSection());
        stmt.setString(3, triple.getWholeSentence());
        stmt.setString(4, triple.getPhraseOriginal());
        stmt.setString(5, triple.getPhrasePlaceholders());
        stmt.setString(6, triple.getPre());
        stmt.setString(7, triple.getPost());
        stmt.setString(8, triple.getSubject());
        stmt.setString(9, triple.getWikiSubject());
        stmt.setString(10, triple.getSubjectType());
        stmt.setString(11, triple.getObject());
        stmt.setString(12, triple.getWikiObject());
        stmt.setString(13, triple.getObjectType());
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
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
   * This is the schema of other_triples:
   * 
   * 01- wikid text 02- section text 03- phrase_original text 04- phrase_placeholder text 05-
   * phrase_pre text 06- phrase_post text 07- subject text 08- wiki_subject text 09- type_subject
   * text 10- object text 11- wiki_object text 12- type_object text 13- type text
   * 
   * @param triple
   */
  public void batchInsertOtherTriple(Queue<WikiTriple> other_triples) {
    String insert = "INSERT INTO " + other_table + " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (WikiTriple triple : other_triples) {
        stmt.setString(1, triple.getWikid());
        stmt.setString(2, triple.getSection());
        stmt.setString(3, triple.getPhraseOriginal());
        stmt.setString(4, triple.getPhrasePlaceholders());
        stmt.setString(5, triple.getPre());
        stmt.setString(6, triple.getPost());
        stmt.setString(7, triple.getSubject());
        stmt.setString(8, triple.getWikiSubject());
        stmt.setString(9, triple.getSubjectType());
        stmt.setString(10, triple.getObject());
        stmt.setString(11, triple.getWikiObject());
        stmt.setString(12, triple.getObjectType());
        stmt.setString(13, triple.getType().name());
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
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
  public void batchInsertMVList(Queue<WikiMVL> lists) {
    String insert = "INSERT INTO " + mvl_table + " VALUES(?,?,?,?)";
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (WikiMVL mvl : lists) {
        stmt.setString(1, mvl.getCode());
        stmt.setString(2, mvl.getWikid());
        stmt.setString(3, mvl.getSection());
        stmt.setString(4, StringUtils.join(mvl.getListWikid(), ","));
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
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
  public void batchInsertNationalityTriple(Queue<String[]> nationalities) {
    String insert = "INSERT INTO " + nationality_table + " VALUES(?,?,?,?)";
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement stmt = this.getConnection().prepareStatement(insert);
      for (String[] nat : nationalities) {
        stmt.setString(1, nat[0]);
        stmt.setString(2, nat[1]);
        stmt.setString(3, nat[2]);
        stmt.setString(4, nat[3]);
        stmt.addBatch();
      }
      stmt.executeBatch();
      this.getConnection().commit();
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
   * @param modelTableName
   * @return
   */
  private boolean checkTableExists(String modelTableName) {
    boolean exists = false;
    String query = "SELECT count(*) FROM sqlite_master WHERE type='table' AND name=?";
    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      stmt.setString(1, modelTableName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.getInt(1) == 1)
          exists = true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    // check that is not empty
    if (exists) {
      query = "SELECT count(*) FROM " + modelTableName;
      try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          if (rs.getInt(1) == 0)
            exists = false;
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return exists;
  }

  /**
   * 
   * @param modelTableName
   * @return
   */
  private boolean checkIndexExists(String indexName) {
    boolean exists = false;
    String query = "SELECT count(*) FROM sqlite_master WHERE type='index' AND name=?";
    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      stmt.setString(1, indexName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.getInt(1) == 1)
          exists = true;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return exists;
  }


  /**
   * Create the model table. It reads from the db all the labeled and unlabeled triples, picking
   * only the phrase_placeholder, the types and the relative relation.
   * 
   */
  public void deriveModelTable() {
    createNecessaryIndexes();
    if (!checkTableExists("model_triples")) {
      createSchemaModelTriples(model_triples);
      System.out.println("\t-> Deriving model table from Evidence DB (labeled)");

      int line = 0;
      int count = 0;

      String query =
          "SELECT phrase_placeholder, type_subject, type_object, relation FROM " + labeled_table;
      String count_query = "SELECT  count(*) FROM " + "(" + query + ")";
      try (PreparedStatement stmt = this.getConnection().prepareStatement(count_query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            line = rs.getInt(1);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

      List<String> allToAdd = new LinkedList<String>();
      try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            count += 1;
            if (count % 1000000 == 0)
              System.out.println("\t-> " + count + "/" + line);

            String phrase_placeholder = rs.getString(1);
            String type_sbj = rs.getString(2);
            String type_obj = rs.getString(3);
            String relation = rs.getString(4);
            String all = phrase_placeholder + "\t" + type_sbj + "\t" + type_obj + "\t" + relation;
            allToAdd.add(all);
            if (allToAdd.size() > 1000000) {
              batchInsertModelEntry("model_triples", allToAdd);
              allToAdd.clear();
            }
          }
        }
        batchInsertModelEntry("model_triples", allToAdd);
        allToAdd.clear();

      } catch (SQLException e) {
        e.printStackTrace();
      }

      line = 0;
      count = 0;

      System.out.println("\t-> Deriving model table from Evidence DB (unlabeled)");
      query = "SELECT phrase_placeholder, type_subject, type_object FROM " + unlabeled_table + "";
      count_query = "SELECT  count(*) FROM " + "(" + query + ")";
      try (PreparedStatement stmt = this.getConnection().prepareStatement(count_query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            line = rs.getInt(1);
          }
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }

      try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            count += 1;
            if (count % 1000000 == 0)
              System.out.println("\t-> " + count + "/" + line);

            String phrase_placeholder = rs.getString(1);
            String type_sbj = rs.getString(2);
            String type_obj = rs.getString(3);
            String relation = "NONE";
            String all = phrase_placeholder + "\t" + type_sbj + "\t" + type_obj + "\t" + relation;
            allToAdd.add(all);
            if (allToAdd.size() > 1000000) {
              batchInsertModelEntry("model_triples", allToAdd);
              allToAdd.clear();
            }
          }
        }

        batchInsertModelEntry(model_triples, allToAdd);
        allToAdd.clear();

      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Return all the labeled triples and put them in a list.
   * 
   * @return
   */
  public List<String> retrieveAllLabeled(String tableName) {
    createNecessaryIndexes();
    createIndexForAll(tableName, "indexAll_" + tableName);
    List<String> allEntries = new LinkedList<String>();

    String query = "SELECT phrase_placeholder, type_subject, type_object, wiki_subject, wiki_object, relation FROM " + tableName;

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String phrase_placeholder = rs.getString(1);
          String type_subject = rs.getString(2);
          String type_object = rs.getString(3);
          String wiki_subject = rs.getString(4);
          String wiki_object = rs.getString(5);
          String relation = rs.getString(6);

          // create the key
          String entry = phrase_placeholder + "|||" +
          type_subject + "|||" + type_object + "|||" +
              wiki_subject + "|||" + wiki_object +
              "|||" + relation;
          
          allEntries.add(entry);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return allEntries;
  }

  /**
   * Return all the unlabeled triples and put them in a list.
   * 
   * @return
   */
  public List<String> retrieveAllUnlabeled(String tableName) {
    createNecessaryIndexes();
    createIndexForAll(tableName, "indexAll_" + tableName);
    List<String> allEntries = new LinkedList<String>();

    String query = "SELECT phrase_placeholder, type_subject, type_object, wiki_subject, wiki_object FROM " + tableName;

    try (PreparedStatement stmt = this.getConnection().prepareStatement(query)) {
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String phrase_placeholder = rs.getString(1);
          String type_subject = rs.getString(2);
          String type_object = rs.getString(3);
          String wiki_subject = rs.getString(4);
          String wiki_object = rs.getString(5);
          String relation = "NONE";

          // create the key
          String entry = phrase_placeholder + "|||" +
              type_subject + "|||" + type_object + "|||" +
                  wiki_subject + "|||" + wiki_object +
                  "|||" + relation;
          
          allEntries.add(entry);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return allEntries;
  }

}
