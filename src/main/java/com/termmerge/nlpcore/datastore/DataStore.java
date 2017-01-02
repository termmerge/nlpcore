package com.termmerge.nlpcore.datastore;

import java.util.function.Function;
import java.util.stream.Stream;
import com.termmerge.nlpcore.NetworkIO;

import java.util.Properties;

import fj.data.Validation;


/**
 * Contract for implementations that abstract over some datastore
 *  technology (Postgres, Redis, etc)
 */
public interface DataStore<
        AtomicRawType, AtomicResultType,
        QueryBuilderType, ConnectionType> extends NetworkIO
{

  /**
   * Connect with the DataStore
   * @return CompletableFuture
   *  -> Validation object on async finish
   */
  Validation<RuntimeException, ConnectionType> connect(
          Properties properties
  );

  /**
   * Query the DataStore to manipulate the stored dataset
   * @param queryBuilder - query to run
   * @param queryMapper - wraps over the raw atomic results
   *   returned by the data store to the final atomic results.
   * @return Validation object on async finish
   */
  Validation<RuntimeException, Stream<AtomicResultType>> query(
          QueryBuilderType queryBuilder,
          Function<AtomicRawType, AtomicResultType> queryMapper
  );

  /**
   * Query the DataStore to manipulate the stored dataset, with the list
   *  of ordered queries ran as one transaction.
   * @param queryBuilders - queries to run
   * @param queryMappers - wraps over the raw atomic results
   *   returned by the data store to the final atomic results.
   * @return Validation object on async finish
   */
  Validation<RuntimeException, Stream<AtomicResultType>> query(
          QueryBuilderType[] queryBuilders,
          Function<AtomicRawType, AtomicResultType>[] queryMappers
  );

  /**
   * Disconnect with the DataStore
   * @return Validation object on async finish
   */
  Validation<RuntimeException, ConnectionType> disconnect();

}
