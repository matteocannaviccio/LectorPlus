package it.uniroma3.model.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author matteo
 *
 */
public class DBCrossValidation extends DBLector {

  private DBModel db_source;

  /**
   * A DBCrossValidation is built on top of the DBModel. This constructor is used when a
   * DBCrossValidation already exists.
   */
  public DBCrossValidation(String dbname) {
    super(dbname);
  }

  /**
   * A DBCrossValidation is built on top of the DBModel. It has a name and a number of
   * cross-validation parts. This constructor is used when a DBCrossValidation has to be created.
   */
  public DBCrossValidation(String db_crossvalidation_name, int nParts, DBModel db_source) {
    super(db_crossvalidation_name);
    this.db_source = db_source;
    create(nParts);
  }

  /**
   * 
   */
  protected void create(int nParts) {
    System.out.println("\t-> creating DBCrossValidation with " + nParts + " parts, ");
    db_source.createNecessaryIndexes();
    createSchema(nParts);

    // retrieve all the facts from the DB
    List<String> all_facts = new ArrayList<String>();
    all_facts.addAll(db_source.retrieveAllLabeled("labeled_triples"));
    all_facts.addAll(db_source.retrieveAllUnlabeled("unlabeled_triples"));

    System.out.println("\t-> retrieved all the triples " + all_facts.size());

    // split all facts in nParts partitions
    List<List<String>> partitions = split(all_facts, nParts);
    System.out.println("\t-> partition avg. size: " + all_facts.size() / nParts);

    // fill the tables in the crossvalidation db
    System.out.print("\t-> inserting part ");
    int ins = 0;
    for (int n = 0; n < nParts; n++) {
      for (int j = 0; j < nParts; j++) {
        if (j == n) {
          System.out.print(j + ", ");
          int inserted =
              batchInsertEvaluationTriples("CV_evaluation_triples_" + n, partitions.get(j));
          ins += inserted;
        } else {
          batchInsertEvidenceTriples("CV_evidence_" + n, partitions.get(j));
        }
      }
    }
    System.out.println(" with " + ins / nParts + " avg. evaluated");
  }

  /**
   * Split a list of T objects in SIZE random parts.
   * 
   * @param list
   * @param size
   * @return
   */
  public static <T> List<List<T>> split(List<T> list, int size) {
    Collections.shuffle(list);
    List<List<T>> result = new ArrayList<List<T>>(size);
    for (int i = 0; i < size; i++) {
      result.add(new ArrayList<T>());
    }
    int index = 0;
    for (T t : list) {
      result.get(index).add(t);
      index = (index + 1) % size;
    }
    return result;
  }

  /**
   * 
   */
  private void createSchema(int nParts) {
    for (int n = 0; n < nParts; n++) {
      createSchemaModelTriples("CV_evidence_" + n);
      createTest("CV_evaluation_triples_" + n);
    }
  }

  /**
   * 
   * @param table_name
   */
  private void createTest(String table_name) {
    String dropLabeled = "DROP TABLE IF EXISTS " + table_name;
    String createLabeled =
        "CREATE TABLE " + table_name + "(" + "phrase_placeholder text, " + "type_subject text, "
            + "type_object text, " + "wiki_subject text, " + "wiki_object text)";

    String createIndexModel = "CREATE INDEX IF NOT EXISTS index_" + table_name + " ON " + table_name
        + "(phrase_placeholder, type_subject, type_object, wiki_subject, wiki_object)";

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
   * @param table_name
   * @param labeled
   */
  private int batchInsertEvaluationTriples(String table_name, List<String> entries) {
    String insert = "INSERT INTO " + table_name + " VALUES(?,?,?,?,?)";
    int inserted = 0;
    try {
      this.getConnection().setAutoCommit(false);
      PreparedStatement pstmt = this.getConnection().prepareStatement(insert);
      /*
       * Here we insert only one instance for each triple composed by subject-phrase-object. At
       * evaluation time we will query dbpedia to get the label.
       */
      for (String entry : entries) {
        /*
         * phrase_placeholder + "|||" + type_subject + "|||" + type_object + "|||" + relation;
         */
        String[] fields = entry.split("\\|\\|\\|");
        String phrase_placeholder = fields[0];
        String type_subject = fields[1];
        String type_object = fields[2];
        String relation = fields[3];

        if (!relation.equals("NONE")) {
          inserted += 1;
          pstmt.setString(1, phrase_placeholder);
          pstmt.setString(2, type_subject);
          pstmt.setString(3, type_object);
          pstmt.addBatch();
        }
      }
      pstmt.executeBatch();
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
    return inserted;
  }

  /**
   * 
   * @param string
   * @param list
   */
  private void batchInsertEvidenceTriples(String table_name, List<String> toAdd) {
    // first insert all the labeled facts that we add in this partition
    List<String> entries = new LinkedList<String>();
    int size = toAdd.size() / 50;
    for (String triple : toAdd) {
      /*
       * phrase_placeholder + "|||" + type_subject + "|||" + type_object + "|||" + relation;
       */
      String[] fields = triple.split("\\|\\|\\|");
      String phrase_placeholder = fields[0];
      String type_subject = fields[1];
      String type_object = fields[2];
      String relation = fields[3];
      entries.add(phrase_placeholder + "\t" + type_subject + "\t" + type_object + "\t" + relation);
      if (entries.size() > size) {
        batchInsertModelEntry(table_name, entries);
        entries.clear();
      }
    }
    batchInsertModelEntry(table_name, entries);
    entries.clear();

  }

}

