package com.termmerge.nlpcore.obtainer;

import java.util.Map;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;

import com.termmerge.nlpcore.AppLogger;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.ConsumerRecord;


/**
 * Obtains a data stream from a publisher using the
 * Apache Kafka publish-subscribe system.
 */
public class KafkaStreamObtainer implements StreamObtainer {

  private Properties networkSettings;  // Key-Value Pairs of Kafka Settings
  private List<Consumer> listeners;    // Thread-safe list of stream listeners
  private boolean hasAssignedTopic;    // Currently subscribing to a topic?
  private Thread pollingThread;        // Thread object that polls Kafka
  private Logger appLogger;            // Application Logger

  public KafkaStreamObtainer() {
    // Obtain required environment variables and emit an error if non-existent
    String[] kafkaSettings = {
            System.getenv("KAFKA_HOST"),
            System.getenv("KAFKA_PORT"),
            System.getenv("KAFKA_GROUP_ID")
    };
    for (String kafkaSetting : kafkaSettings) {
      if (kafkaSetting == null) {
        throw new RuntimeException(
                "System environment variables are not set!"
        );
      }
    }

    // Kafka Network Settings
    this.networkSettings = new Properties();
    networkSettings.setProperty("bootstrap.servers",
            kafkaSettings[0] + " " + kafkaSettings[1]);
    networkSettings.setProperty("group.id", kafkaSettings[2]);
    networkSettings.setProperty("enable.auto.commit", "true");
    networkSettings.setProperty("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
    networkSettings.setProperty("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");

    this.listeners = Collections.synchronizedList(
            new ArrayList<Consumer>()
    );
    this.hasAssignedTopic = false;
    this.pollingThread = null;
    this.appLogger = new AppLogger();
  }

  public void listenToStream(String topicName) {
    if (this.hasAssignedTopic) {
      throw new RuntimeException("Cannot listen/switch to another topic");
    }
    this.hasAssignedTopic = true;
    this.appLogger.info("Listening to Kafka Stream, topic: " + topicName);

    this.pollingThread = new Thread(() -> {
      // Initialize Kafka Consumer and subscribe to specified topic
      KafkaConsumer kafkaConsumer =
              new KafkaConsumer<String, String>(networkSettings);
      ArrayList topicsList = new ArrayList<String>();
      topicsList.add(topicName);
      kafkaConsumer.subscribe(topicsList);

      // Continuously Obtain Kafka records and fire listeners
      while (!Thread.currentThread().isInterrupted()) {
        synchronized (listeners) {
          ConsumerRecords<String, String> consumerRecordList =
                  kafkaConsumer.poll(100);

          for (ConsumerRecord<String, String> consumerRecord :
                  consumerRecordList) {
            for (Consumer listener : listeners) {
              Properties kafkaRecord = new Properties();
              kafkaRecord.setProperty("key", consumerRecord.key());
              kafkaRecord.setProperty("value", consumerRecord.value());

              listener.accept(kafkaRecord);
            }
          }
        }

        this.appLogger.info("(Kafka Thread) Kafka Thread interrupted");
        kafkaConsumer.close();
      }
    });
    this.pollingThread.run();
  }

  public void addListener(Consumer<Map<String, String>> listener) {
    this.listeners.add(listener);
  }

  public void removeListener(Consumer<Map<String, String>> listener) {
    this.listeners.remove(listener);
  }

  public void teardownStream() {
    this.listeners = null;

    if (this.pollingThread != null) {
      this.pollingThread.interrupt();
      this.appLogger.info("(Main Thread) Kafka Thread interrupted");
    }
  }

}
