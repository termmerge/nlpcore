package com.termmerge.nlpcore.datastore;

import java.util.stream.Stream;

import fj.data.Validation;
import java.util.concurrent.CompletableFuture;


/**
 * Contract for implementations that abstract over some datastore
 *  technology (Postgres, Redis, etc)
 */
public interface DataStore<AtomicResultType, QueryBuilderType>
{

  /**
   * Connect with the DataStore
   * @return CompletableFuture
   *  -> Validation object on async finish
   */
  CompletableFuture<Validation<RuntimeException, ?>> connect();

  /**
   * Query the DataStore to manipulate the stored dataset
   * @param queryBuilder
   * @return CompletableFuture
   *  -> Stream of AtomicResultType on async finish
   */
  CompletableFuture<Stream<AtomicResultType>> query(
          QueryBuilderType queryBuilder
  );

  /**
   * Disconnect with the DataStore
   * @return CompletableFuture
   *  -> Validation object on async finish
   */
  CompletableFuture<Validation<RuntimeException, ?>> disconnect();

}
