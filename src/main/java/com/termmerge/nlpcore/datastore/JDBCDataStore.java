package com.termmerge.nlpcore.datastore;

import java.sql.ResultSet;
import java.util.function.Function;
import java.util.stream.Stream;
import com.termmerge.nlpcore.datastore.entity.PostgresEntity;
import java.sql.PreparedStatement;
import java.sql.Connection;

import java.util.Arrays;
import java.sql.DriverManager;
import java.util.Properties;
import java.sql.SQLException;

import fj.data.Validation;
import com.aol.cyclops.control.ReactiveSeq;
import org.apache.commons.dbutils.ResultSetIterator;


/**
 * Generalized DataStore implementation for JDBC-based datastores
 *  Not thread-safe
 */
class JDBCDataStore implements
        DataStore<Object[], PostgresEntity, String, Boolean>
{

  private String driverName;
  private Connection dbConnection;
  private Properties properties;

  protected JDBCDataStore(
          Properties properties,
          String driverName
  )
  {
    this.driverName = driverName;
    this.dbConnection = null;
    this.properties = properties;
  }

  public Validation<RuntimeException, Boolean> connect(
          Properties properties
  )
  {
    String[] requiredProperties = {"connection_string"};
    for (String requiredProperty: requiredProperties) {
      if (!properties.containsKey(requiredProperty)) {
        return Validation.fail(
                new IllegalArgumentException("connection_string required")
        );
      }
    }

    if (this.dbConnection != null) {
      return Validation.fail(
              new RuntimeException(
                      "Database connection already initialized!"
              )
      );
    }
    else if (this.driverName == null) {
      return Validation.fail(
              new RuntimeException(
                      "Database driver name not set"
              )
      );
    }

    try {
      Class.forName(this.driverName);

      this.dbConnection = DriverManager.getConnection(
              this.properties.getProperty("connection_string")
      );
    } catch (ClassNotFoundException e) {
      return Validation.fail(
              new RuntimeException("Cannot find Postgres Driver.")
      );
    } catch (SQLException e) {
      return Validation.fail(
              new RuntimeException("Cannot connect to database.")
      );
    }
    
    return Validation.success(true);
  }

  public Validation<RuntimeException, Stream<PostgresEntity>> query(
          String sqlQuery,
          Function<Object[], PostgresEntity> queryMapper
  )
  {
    ReactiveSeq<Object[]> stream = null;

    try {
      PreparedStatement preparedStatement =
              this.dbConnection.prepareStatement(sqlQuery);
      ResultSet resultSet = preparedStatement.executeQuery();
      stream = ReactiveSeq.fromIterator(
              new ResultSetIterator(resultSet)
      );
    } catch (SQLException e) {
      return Validation.fail(
              new RuntimeException("SQL Query execution failed!")
      );
    }

    return Validation.success(
            stream.map(queryMapper)
    );
  }

  public Validation<RuntimeException, Stream<PostgresEntity>> query(
          String[] sqlQueries,
          Function<Object[], PostgresEntity>[] queryMappers
  )
  {
    try {
      this.dbConnection.setAutoCommit(false);
    } catch (SQLException e) {
      return Validation.fail(
              new RuntimeException("Cannot set autocommit off!")
      );
    }

    Stream<Validation<RuntimeException, Stream<PostgresEntity>>>
            stream =
            ReactiveSeq
                    .fromStream(Arrays.stream(sqlQueries))
                    .zip(Arrays.stream(queryMappers))
                    .map(tuple -> tuple.map(this::query));

    Validation<RuntimeException, Stream<PostgresEntity>> result =
            stream.reduce(
                    Validation.success(ReactiveSeq.empty()),
                    (accumValidation, currentValidation) -> {
                      if (accumValidation.isFail()) {
                        return accumValidation;
                      }
                      else if (currentValidation.isFail()) {
                        return currentValidation;
                      }

                      return Validation.success(
                              Stream.concat(
                                      accumValidation.success(),
                                      currentValidation.success()
                              )
                      );
                    }
            );

    try {
      this.dbConnection.commit();
      this.dbConnection.setAutoCommit(true);
    } catch (SQLException e) {
      return Validation.fail(
              new RuntimeException("Cannot set autocommit back on!")
      );
    } finally {
      stream.close();
    }

    return result;
  }

  public Validation<RuntimeException, Boolean> disconnect()
  {
    if (this.dbConnection == null) {
      return Validation.fail(
              new IllegalStateException("DB Connection not initiated")
      );
    }

    try {
      this.dbConnection.close();
    } catch (SQLException e) {
      return Validation.fail(
              new RuntimeException("DB Connection complained on close.")
      );
    }

    return Validation.success(true);
  }

}
