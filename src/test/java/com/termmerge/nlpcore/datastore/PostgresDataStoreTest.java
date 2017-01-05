package com.termmerge.nlpcore.datastore;

import java.util.function.Function;
import java.util.stream.Stream;

import java.util.Properties;

import acolyte.jdbc.StatementHandler;

import acolyte.jdbc.RowLists;
import acolyte.jdbc.CompositeHandler;
import com.termmerge.nlpcore.datastore.entity.PostgresEntity;
import fj.data.Validation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PostgresDataStoreTest
{

  private class TestableEntity implements PostgresEntity {
    String column1;
    String column2;
  }

  private Properties dataStoreProperties;

  @Before
  public void beforeEachSetupDB()
  {
    StatementHandler sqlHandler = new CompositeHandler()
            .withQueryDetection("^SELECT")
            .withQueryHandler((sql, parameters) ->
                    RowLists
                            .rowList2(String.class, String.class)
                            .withLabel(1, "column1")
                            .withLabel(2, "column2")
                            .append("row1_column1", "row1_column2")
                            .asResult()
            );
    acolyte.jdbc.Driver.register("testOne", sqlHandler);

    this.dataStoreProperties = new Properties();
    this.dataStoreProperties.setProperty(
            "connection_string",
            "jdbc:acolyte:somePass?handler=testOne"
    );
  }

  @Test
  public void testOneQuery()
  {
    PostgresDataStore postgresDataStore = new PostgresDataStore();
    Validation<RuntimeException, Boolean> validationObject =
            postgresDataStore.connect(this.dataStoreProperties);
    Assert.assertTrue(validationObject.isSuccess());

    Validation<RuntimeException, Stream<?>> queryValidation =
            postgresDataStore.query(
                    "SELECT * FROM table",
                    (resultTuple) -> {
                      TestableEntity testableEntity = new TestableEntity();
                      testableEntity.column1 = (String) resultTuple[0];
                      testableEntity.column2 = (String) resultTuple[1];
                      return testableEntity;
                    }
    );
    Assert.assertTrue(queryValidation.isSuccess());

    queryValidation.success().forEach((testableEntity) -> {
      Assert.assertTrue(testableEntity instanceof TestableEntity);

      TestableEntity testableEntityCasted = (TestableEntity) testableEntity;
      Assert.assertEquals(
              testableEntityCasted.column1,
              "row1_column1"
      );
      Assert.assertEquals(
              testableEntityCasted.column2,
              "row1_column2"
      );
    });
  }

  @Test
  public void testMultipleQueries()
  {
    PostgresDataStore postgresDataStore = new PostgresDataStore();
    Validation<RuntimeException, Boolean> validationObject =
            postgresDataStore.connect(this.dataStoreProperties);
    Assert.assertTrue(validationObject.isSuccess());


    Function<Object[], TestableEntity> mapper = (resultTuple) -> {
      TestableEntity testableEntity = new TestableEntity();
      testableEntity.column1 = (String) resultTuple[0];
      testableEntity.column2 = (String) resultTuple[1];
      return testableEntity;
    };
    String[] sqlQueries = new String[]{"SELECT * FROM table"};
    Function<Object[], TestableEntity>[] mappers = new Function[]{mapper};

    Validation<RuntimeException, Stream<?>> queryValidation =
            postgresDataStore.query(
                    sqlQueries,
                    mappers
            );
    Assert.assertTrue(queryValidation.isSuccess());

    queryValidation.success().forEach((testableEntity) -> {
      Assert.assertTrue(testableEntity instanceof TestableEntity);

      TestableEntity testableEntityCasted = (TestableEntity) testableEntity;
      Assert.assertEquals(
              testableEntityCasted.column1,
              "row1_column1"
      );
      Assert.assertEquals(
              testableEntityCasted.column2,
              "row1_column2"
      );
    });
  }

}
