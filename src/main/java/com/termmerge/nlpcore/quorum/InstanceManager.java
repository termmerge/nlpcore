package com.termmerge.nlpcore.quorum;

import java.util.function.Consumer;

import fj.data.Validation;


/**
 * Contract for managing an application instance and setting it up properly.
 *  Handles application bootstrapping and cleanup phases.
 */
public interface InstanceManager
{

  /**
   * Bootstrap this app instance as an Interface Group instance.
   *  Interface Groups are accessible via HTTP/Websockets and
   *  do not perform any NLP computation - they only create
   *  tasks and wait for the resolution or streaming results
   *  of an NLP task.
   * @param listener - Event Listener on finish of bootstrapping
   */
  void bootstrapAsInterface(
          Consumer<Validation<RuntimeException, ?>> listener
  );

  /**
   * Bootstrap this app instance as a Compute Group instance.
   *  Compute Groups are inaccessible save through messaging
   *  using a Message Bus. They only perform NLP computation
   *  and when done, send the results across the Message Bus
   *  to the Interface Group.
   * @param listener - Event Listener on finish of bootstrapping
   */
  void bootstrapAsCompute(
          Consumer<Validation<RuntimeException, ?>> listener
  );

  /**
   * Cleanup this app instance and shutdown properly.
   * @param listener - Event Listener on finish of cleanup
   * @return Validation object
   */
  void cleanup(
          Consumer<Validation<RuntimeException, ?>> listener
  );

}
