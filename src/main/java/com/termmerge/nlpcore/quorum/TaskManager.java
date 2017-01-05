package com.termmerge.nlpcore.quorum;

import fj.data.Validation;


/**
 * Contract for any mechanism that allows us to generate uniquely
 *  identifiable task ids (relative to one physical client) or
 *  destroy them
 */
public interface TaskManager
{

  /**
   * Generates a uniquely identifiable task id
   * @return String Task ID for the task
   */
  Validation<RuntimeException, String> generateId();

  /**
   * Destroy this task id (so it can now be
   *  possibly used by another in the system)
   * @param taskId
   */
  Validation<RuntimeException, ?> destroyId(String taskId);

}
