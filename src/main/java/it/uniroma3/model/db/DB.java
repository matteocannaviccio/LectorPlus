package it.uniroma3.model.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class models a generic DB. It provides the method to obtain a connection to a specific DB,
 * given its name.
 * 
 * @author matteo
 *
 */
public class DB {

  private Connection connection;
  private String title;

  /**
   * 
   * @param dbname
   */
  public DB(String dbname, boolean inMemory) {
    this.title = dbname;
    this.connection = obtainConnection(inMemory);
  }

  /**
   * Return a connection. It is called only once for each DB.
   * 
   * @param inMemory
   * @return
   */
  public Connection obtainConnection(boolean inMemory) {
    Connection conn = null;
    try {
      Class.forName("org.sqlite.JDBC");
      String sJdbc = "jdbc:sqlite";

      String sDbUrl;
      if (inMemory)
        sDbUrl = sJdbc + "::memory:";
      else
        sDbUrl = sJdbc + ":" + this.title;
      conn = DriverManager.getConnection(sDbUrl);
      Statement st = conn.createStatement();
      st.execute("PRAGMA synchronous=OFF");
      st.execute("PRAGMA jorunal_mode=MEMORY");
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
    return conn;
  }

  /**
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * @return the dbname
   */
  public String getDbTitle() {
    return title;
  }

  /**
   * 
   */
  public void closeConnection() {
    try {
      this.connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
