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
 * DataStore implementation for Postgres
 *  Not thread-safe
 */
public class PostgresDataStore implements
        DataStore<Object[], PostgresEntity, String, Boolean>
{

  private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

  private Connection dbConnection;
  private final Properties properties;

  private PostgresDataStore(Properties properties)
  {
    this.dbConnection = null;
    this.properties = properties;
  }

  public static
    Validation<RuntimeException, PostgresDataStore> createDataStore(
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

    return Validation.success(
            new PostgresDataStore(properties)
    );
  }

  public Validation<RuntimeException, Boolean> connect()
  {
    try {
      Class.forName(PostgresDataStore.POSTGRES_DRIVER);

      if (this.dbConnection != null) {
        return Validation.fail(
                new RuntimeException(
                        "Database connection already initialized!"
                )
        );
      }

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
