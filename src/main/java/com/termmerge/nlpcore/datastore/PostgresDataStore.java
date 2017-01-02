package com.termmerge.nlpcore.datastore;


/**
 * Postgres-based implementation of DataStore (deriving off of JDBC)
 */
public class PostgresDataStore extends JDBCDataStore
{

  public PostgresDataStore()
  {
    super("org.postgresql.Driver");
  }

}
