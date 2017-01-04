package com.termmerge.nlpcore.messagebus;

import java.util.Properties;

import org.junit.ClassRule;
import org.junit.Test;

import com.github.charithe.kafka.KafkaJunitRule;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import net.jodah.concurrentunit.Waiter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;


public class KafkaMessageBusConsumerTest
{

  @ClassRule
  public static KafkaJunitRule kafkaRule =
          new KafkaJunitRule(EphemeralKafkaBroker.create());

  @Test
  public void testValidSettingsPass()
  {
    Properties settings = new Properties();
    settings.setProperty(
            "connection_string",
            "localhost:" + Integer.toString(
                    kafkaRule.helper().kafkaPort()
            )
    );
    settings.setProperty("group_id", "_");

    KafkaMessageBusConsumer kafkaMessageBusConsumer =
            new KafkaMessageBusConsumer();
    Assert.assertTrue(
            kafkaMessageBusConsumer.connect(settings).isSuccess()
    );
  }

  @Test
  public void testOneMessage() throws Throwable
  {
    Waiter waiter = new Waiter();

    // Create a Producer and publish a random message
    KafkaProducer<String, String> testProducer =
            kafkaRule.helper().createStringProducer();
    testProducer.send(
            new ProducerRecord<>("testTopic", "testKey", "testValue")
    );
    testProducer.flush();

    // Create a Consumer
    Properties consumerSettings = new Properties();
    consumerSettings.setProperty(
            "connection_string",
            "localhost:" + Integer.toString(
                    kafkaRule.helper().kafkaPort()
            )
    );
    consumerSettings.setProperty("group_id", "test");
    KafkaMessageBusConsumer kafkaMessageBusConsumer =
            new KafkaMessageBusConsumer();
    Assert.assertTrue(
            kafkaMessageBusConsumer.connect(consumerSettings).isSuccess()
    );

    // Add a listener and listen to the message bus
    kafkaMessageBusConsumer.addListener((validationObject) -> {
      waiter.assertTrue(validationObject.isSuccess());

      Properties record = validationObject.success();
      waiter.assertEquals(record.getProperty("key"), "testKey");
      waiter.assertEquals(record.getProperty("value"), "testValue");
      waiter.resume();
    });
    kafkaMessageBusConsumer.listenToMessageBus("testTopic");

    waiter.await(1000);
  }

}
