package com.termmerge.nlpcore.messagebus;

import java.util.function.Consumer;

import java.util.Properties;
import fj.data.Validation;


/**
 * Contract for any implementing mechanism that grabs
 * reported data streams from some publisher in the
 * network and listens to whatever that publisher is
 * producing.
 */
public interface MessageBusConsumer
{

  /**
   * Listen to a specific data topics in the message bus
   * @return Validation
   *  Fail -> RuntimeException
   *  Success -> Thread ID
   * @param topicName
   */
  Validation<RuntimeException, Long> listenToMessageBus(
          String topicName
  );

  /**
   * Add a listener, which acts upon incoming atomic data that is on
   * the messagebus. The atomic data has a key-value structure of
   * string keys and string values
   * @param listener
   */
  void addListener(
          Consumer<Validation<RuntimeException, Properties>>
                  listener
  );

  /**
   * Remove a listener.
   * @param listener
   */
  void removeListener(
          Consumer<Validation<RuntimeException, Properties>>
                  listener
  );

  /**
   * Publish a Validation object to all listeners. Must be thread-safe as
   *  this method can be called by multiple threads.
   * @param validationObject
   */
  void publishToListeners(
          Validation<RuntimeException, Properties> validationObject
  );

  /**
   * Tear down the data messagebus
   * @return Validation
   *  Fail -> RuntimeException
   *  Success -> Thread ID
   */
  Validation<RuntimeException, Long> teardownConsumer();

}
