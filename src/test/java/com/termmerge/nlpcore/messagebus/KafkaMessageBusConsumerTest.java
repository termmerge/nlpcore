package com.termmerge.nlpcore.messagebus;

import java.util.Properties;

import fj.data.Validation;
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
  public void testInvalidSettingFails()
  {
    Properties settings = new Properties();
    settings.setProperty("connection_string", "_");
    settings.setProperty("group_id", "_");
    Assert.assertTrue(
            KafkaMessageBusConsumer.constructConsumer(settings).isSuccess()
    );
  }

  @Test
  public void testOneMessage() throws Throwable
  {
    Waiter waiter = new Waiter();

    KafkaProducer<String, String> testProducer =
            kafkaRule.helper().createStringProducer();
    testProducer.send(
            new ProducerRecord<>("testTopic", "testKey", "testValue")
    );
    testProducer.flush();

    Properties consumerSettings = new Properties();
    consumerSettings.setProperty(
            "connection_string",
            "localhost:" + Integer.toString(
                    kafkaRule.helper().kafkaPort()
            )
    );
    consumerSettings.setProperty("group_id", "test");

    Validation<RuntimeException, KafkaMessageBusConsumer>
            kafkaMessageBusConsumerValidation =
            KafkaMessageBusConsumer.constructConsumer(consumerSettings);
    Assert.assertTrue(
            kafkaMessageBusConsumerValidation.isSuccess()
    );

    KafkaMessageBusConsumer kafkaMessageBus =
            kafkaMessageBusConsumerValidation.success();
    kafkaMessageBus.addListener((validationObject) -> {
      waiter.assertTrue(validationObject.isSuccess());

      Properties record = validationObject.success();
      waiter.assertEquals(record.getProperty("key"), "testKey");
      waiter.assertEquals(record.getProperty("value"), "testValue");
      waiter.resume();
    });
    kafkaMessageBus.listenToMessageBus("testTopic");

    waiter.await(1000);
  }

}
