package com.termmerge.nlpcore;

import fj.data.Validation;
import java.util.Properties;


/**
 * Contract for Network-bound IO abstractions
 */
public interface NetworkIO
{

  /**
   * Connect to the specified network-bound IO
   * @param properties - configurable settings used to connect
   * @return validation object
   */
  Validation<RuntimeException, ?> connect(Properties properties);

  /**
   * Disconnect from the specified network-bound IO
   * @return validation object
   */
  Validation<RuntimeException, ?> disconnect();

}
