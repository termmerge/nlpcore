package com.termmerge.nlpcore.obtainer;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Contract for any implementing mechanism that grabs
 * reported data streams from some publisher in the
 * network and listens to whatever that publisher is
 * producing.
 */
public interface StreamObtainer
{

  /**
   * Listen to a specific data stream that is publishing under a certain
   * topicName
   * @param topicName
   */
  void listenToStream(String topicName);

  /**
   * Add a listener, which acts upon incoming atomic data that is on the
   * stream. The atomic data has a key-value structure of string keys and
   * string values
   * @param listener
   */
  void addListener(Consumer<Map<String, String>> listener);

  /**
   * Remove a listener.
   * @param listener
   */
  void removeListener(Consumer<Map<String, String>> listener);

  /**
   * Tear down the data stream
   */
  void teardownStream();

}
