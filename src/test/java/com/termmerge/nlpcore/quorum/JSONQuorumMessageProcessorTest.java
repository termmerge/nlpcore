package com.termmerge.nlpcore.quorum;

import fj.data.Validation;
import org.junit.Test;

import org.junit.Assert;


public class JSONQuorumMessageProcessorTest
{

  @Test
  public void testSerialization()
  {
    TaskIdManager taskIdManager = new TaskIdManager()
    {
      @Override
      public Validation<RuntimeException, String> generateId()
      {
        return Validation.success("1");
      }

      @Override
      public Validation<RuntimeException, String> destroyId(String taskId)
      {
        return Validation.success("1");
      }
    };

    Validation<RuntimeException, QuorumMessage> quorumMessageValidation =
            (new QuorumMessage.Builder())
            .asRequestMessage()
                    .setTask(QuorumMessage.BATCH_ANALYTICS)
                    .setParam("key1", "value1")
            .build(taskIdManager);
    Assert.assertTrue(
            quorumMessageValidation.isSuccess()
    );

    JSONQuorumMessageProcessor jsonProcessor =
            new JSONQuorumMessageProcessor();
    Validation<RuntimeException, String> validationObject =
            jsonProcessor.serialize(quorumMessageValidation.success());
    Assert.assertTrue(
            validationObject.isSuccess()
    );
  }

  @Test
  public void testDeserialization()
  {
    String jsonPacket = "{" +
            "'sender': 'someSender'," +
            "'recipient': 'someRecipient'," +
            "'task': 'empire'," +
            "'time': '1483117437420'," +
            "'taskId': 'ABC1'," +
            "'params': {'key1': 'value1', 'key2': 'value2'}" +
            "}";

    JSONQuorumMessageProcessor jsonProcessor =
            new JSONQuorumMessageProcessor();

    Validation<RuntimeException, QuorumMessage> validationObject =
            jsonProcessor.deserialize(jsonPacket);
    Assert.assertTrue(
            validationObject.isSuccess()
    );

    QuorumMessage quorumMessage = validationObject.success();
    Assert.assertEquals(
            "someSender",
            quorumMessage.getSender()
    );
    Assert.assertEquals(
            "someRecipient",
            quorumMessage.getRecipient()
    );
    Assert.assertEquals(
            (long) new Long("1483117437420"),
            quorumMessage.getTime().getTime()
    );
    Assert.assertEquals(
            "ABC1",
            quorumMessage.getTaskId()
    );
    Assert.assertEquals(
            "value1",
            quorumMessage.getParams().getProperty("key1")
    );
    Assert.assertEquals(
            "value2",
            quorumMessage.getParams().getProperty("key2")
    );
  }

}
