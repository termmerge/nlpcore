package com.termmerge.nlpcore.obtainer;

import org.junit.ClassRule;
import org.junit.Test;
import com.github.charithe.kafka.KafkaJunitRule;
import com.github.charithe.kafka.EphemeralKafkaBroker;
import net.jodah.concurrentunit.Waiter;

import java.util.Map;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;


public class KafkaStreamObtainerTest
{

  @ClassRule
  public static KafkaJunitRule kafkaRule =
          new KafkaJunitRule(EphemeralKafkaBroker.create());

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidSettingFails()
  {
    Map settings = new Properties();
    new KafkaStreamObtainer(settings);
  }

  @Test
  public void testOneMessage() throws Throwable
  {
    Waiter waiter = new Waiter();

    KafkaProducer testProducer = kafkaRule.helper().createStringProducer();
    testProducer.send(
            new ProducerRecord<>("testTopic", "testKey", "testValue")
    );
    testProducer.flush();

    Map consumerSettings = new Properties();
    consumerSettings.put(
            "connection_string",
            "localhost:" + Integer.toString(
                    kafkaRule.helper().kafkaPort()
            )
    );
    consumerSettings.put("group_id", "test");
    KafkaStreamObtainer testConsumer =
            new KafkaStreamObtainer(consumerSettings);
    testConsumer.addListener((record) -> {
      waiter.assertEquals(record.get("key"), "testKey");
      waiter.assertEquals(record.get("value"), "testValue");
      waiter.resume();
    });
    testConsumer.listenToStream("testTopic");
    waiter.await(500);
  }

}
