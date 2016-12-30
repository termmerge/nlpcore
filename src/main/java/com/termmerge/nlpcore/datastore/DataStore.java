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

  CompletableFuture<Validation<RuntimeException, ?>> connect();

  CompletableFuture<Stream<AtomicResultType>> query(
          QueryBuilderType queryBuilder
  );

  CompletableFuture<Validation<RuntimeException, ?>> disconnect();

}
