package com.termmerge.nlpcore.messagebus;

import java.util.Map;
import java.util.List;
import java.util.function.Consumer;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * Obtains a data messagebus from a publisher using the
 * Apache Kafka publish-subscribe system.
 */
public class KafkaMessageBusConsumer implements MessageBusConsumer
{

  private Properties networkSettings;  // Key-Value Pairs of Kafka Settings
  private List<Consumer> listeners;    // Thread-safe list of messagebus listeners
  private boolean hasAssignedTopic;    // Currently subscribed to a topic?
  private Thread pollingThread;        // Kafka Polling Thread
  private Logger logger;               // Application Logger


  public KafkaMessageBusConsumer(Map<String, String> kafkaSettings)
  {
    // Obtain required settings and emit an error if non-existent
    String[] requiredSettings = {"connection_string", "group_id"};
    for (String requiredSetting : requiredSettings) {
      if (kafkaSettings.get(requiredSetting) == null) {
        throw new IllegalArgumentException(
                "Kafka settings are not correctly set!"
        );
      }
    }

    // Kafka Network Settings
    this.networkSettings = new Properties();
    networkSettings.put("bootstrap.servers",
            kafkaSettings.get("connection_string"));
    networkSettings.put("auto.offset.reset", "earliest");
    networkSettings.put("group.id", kafkaSettings.get("group_id"));
    networkSettings.put("enable.auto.commit", "true");
    networkSettings.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
    networkSettings.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");

    this.listeners = Collections.synchronizedList(
            new ArrayList<Consumer>()
    );
    this.hasAssignedTopic = false;
    this.pollingThread = null;
    this.logger = LoggerFactory.getLogger(KafkaMessageBusConsumer.class);
  }

  public void listenToStream(String topicName)
  {
    if (this.hasAssignedTopic) {
      throw new IllegalStateException("Cannot listen/switch to another topic");
    }
    this.hasAssignedTopic = true;
    this.logger.info("Listening to Kafka Stream, topic: " + topicName);

    this.pollingThread = new Thread(() -> {
      // Initialize Kafka Consumer and subscribe to specified topic
      KafkaConsumer<String, String> kafkaConsumer =
              new KafkaConsumer<>(networkSettings);
      ArrayList topicsList = new ArrayList<String>();
      topicsList.add(topicName);
      kafkaConsumer.subscribe(topicsList);

      // Continuously Obtain Kafka records and fire listeners
      while (!Thread.currentThread().isInterrupted()) {
        ConsumerRecords<String, String> consumerRecordList =
                kafkaConsumer.poll(10);

        synchronized (listeners) {
          for (ConsumerRecord<String, String> consumerRecord :
                  consumerRecordList) {
            for (Consumer listener : listeners) {
              Properties kafkaRecord = new Properties();
              kafkaRecord.put("key", consumerRecord.key());
              kafkaRecord.put("value", consumerRecord.value());
              listener.accept(kafkaRecord);
            }
          }
        }
      }

      this.logger.info("(Kafka Thread) Kafka Thread interrupted");
      kafkaConsumer.close();
    });
    this.pollingThread.start();
  }

  public void addListener(Consumer<Map<String, String>> listener)
  {
    this.listeners.add(listener);
  }

  public void removeListener(Consumer<Map<String, String>> listener)
  {
    this.listeners.remove(listener);
  }

  public void teardownStream()
  {
    this.listeners = null;

    if (this.pollingThread != null) {
      this.pollingThread.interrupt();
      this.logger.warn("(Main Thread) Interrupting Kafka Thread");
    }
  }

}
