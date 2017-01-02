package com.termmerge.nlpcore.datastore;

import java.util.Properties;

/**
 * Postgres-based implementation of DataStore (deriving off of JDBC)
 */
public class PostgresDataStore extends JDBCDataStore
{

  private PostgresDataStore(Properties properties)
  {
    super(properties, "org.postgresql.Driver");
  }

}
