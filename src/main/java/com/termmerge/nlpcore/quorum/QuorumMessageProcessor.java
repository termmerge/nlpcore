package com.termmerge.nlpcore.quorum;

import fj.data.Validation;


/**
 * Contract for a processor that allows for serializing and
 *  deserializing Quorum Messages on and off network transport
 */
public interface QuorumMessageProcessor
{

  /**
   * Serialize a QuorumMessage for network transport
   * @param quorumMessage
   * @return String
   */
  Validation<RuntimeException, String> serialize(
          QuorumMessage quorumMessage
  );

  /**
   * Deserialize a QuorumMessage (usually coming off of
   *  network transport)
   * @param quorumMessage
   * @return QuorumMessage
   */
  Validation<RuntimeException, QuorumMessage> deserialize(
          String quorumMessage
  );

}
