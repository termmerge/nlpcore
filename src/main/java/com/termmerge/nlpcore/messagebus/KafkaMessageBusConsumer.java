package com.termmerge.nlpcore.messagebus;

import java.util.List;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Collections;

import org.slf4j.Logger;

import fj.data.Validation;
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

  // Key-Value Pairs of Kafka Settings
  private Properties networkSettings;

  // Thread-safe list of message bus listeners
  private final
    List<Consumer<Validation<RuntimeException, Properties>>> listeners;

  // Currently subscribed to a topic?
  private boolean hasAssignedTopic;

  // Kafka Polling Thread
  private Thread pollingThread;

  // Application Logger
  private Logger logger;


  private KafkaMessageBusConsumer(Properties kafkaSettings)
  {
    // Obtain required settings and emit an error if non-existent
    String[] requiredSettings = {"connection_string", "group_id"};
    for (String requiredSetting : requiredSettings) {
      if (!kafkaSettings.containsKey(requiredSetting)) {
        throw new IllegalArgumentException(
                "Kafka settings are not correctly set!"
        );
      }
    }

    // Kafka Network Settings
    this.networkSettings = new Properties();
    networkSettings.put("bootstrap.servers",
            kafkaSettings.getProperty("connection_string"));
    networkSettings.put("auto.offset.reset", "earliest");
    networkSettings.put("group.id",
            kafkaSettings.getProperty("group_id"));
    networkSettings.put("enable.auto.commit", "true");
    networkSettings.put("key.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");
    networkSettings.put("value.deserializer",
            "org.apache.kafka.common.serialization.StringDeserializer");

    this.listeners = Collections.synchronizedList(
            new ArrayList<>()
    );
    this.hasAssignedTopic = false;
    this.pollingThread = null;
    this.logger = LoggerFactory.getLogger(KafkaMessageBusConsumer.class);
  }

  public static
    Validation<RuntimeException, KafkaMessageBusConsumer> constructConsumer(
          Properties kafkaSettings
  )
  {
    final KafkaMessageBusConsumer instance;

    try {
      instance = new KafkaMessageBusConsumer(kafkaSettings);
    } catch (IllegalArgumentException e) {
      return Validation.fail(e);
    }

    return Validation.success(instance);
  }

  public Validation<RuntimeException, Long> listenToMessageBus(
          String topicName
  )
  {
    if (this.hasAssignedTopic) {
      return Validation.fail(
              new IllegalStateException(
                      "Cannot listen/switch to another topic"
              )
      );
    }
    this.hasAssignedTopic = true;
    this.logger.info("Listening to Kafka Message Bus, topic: " + topicName);

    this.pollingThread = new Thread(() -> {
      // Initialize Kafka Consumer and subscribe to specified topic
      KafkaConsumer<String, String> kafkaConsumer =
              new KafkaConsumer<>(networkSettings);
      ArrayList<String> topicsList = new ArrayList<>();

      // Synchronized publish of a Validation object to all current consumers

      topicsList.add(topicName);
      try {
        kafkaConsumer.subscribe(topicsList);
      } catch (RuntimeException e) {
        this.publishToListeners(Validation.fail(e));
      }


      // Continuously obtain Kafka records and fire listeners
      ConsumerRecords<String, String> consumerRecordList = null;
      while (!Thread.currentThread().isInterrupted()) {
        try {
          consumerRecordList = kafkaConsumer.poll(10);
        } catch (RuntimeException e) {
          this.publishToListeners(Validation.fail(e));
        }

        if (consumerRecordList == null) {
          continue;
        }

        for (ConsumerRecord<String, String> consumerRecord :
                consumerRecordList) {
          Properties kafkaRecord = new Properties();
          kafkaRecord.put("key", consumerRecord.key());
          kafkaRecord.put("value", consumerRecord.value());
          this.publishToListeners(Validation.success(kafkaRecord));
        }
      }

      this.logger.info("(Kafka Thread) Kafka Thread interrupted");
      kafkaConsumer.close();
    });

    try {
      this.pollingThread.start();
    } catch (IllegalThreadStateException e) {
      return Validation.fail(e);
    }

    return Validation.success(this.pollingThread.getId());
  }

  public void addListener(
          Consumer<Validation<RuntimeException, Properties>> listener
  )
  {
    this.listeners.add(listener);
  }

  public void removeListener(
          Consumer<Validation<RuntimeException, Properties>> listener
  )
  {
    this.listeners.remove(listener);
  }

  public void publishToListeners(
          Validation<RuntimeException, Properties> validationObject
  )
  {
    synchronized (this.listeners) {
      for (Consumer<Validation<RuntimeException, Properties>> listener :
              listeners) {
        listener.accept(validationObject);
      }
    }
  }

  public Validation<RuntimeException, Long> teardownConsumer()
  {
    if (this.pollingThread == null) {
      return Validation.fail(
              new IllegalStateException(
                      "Consumer hasn't been started!"
              )
      );
    }

    try {
      this.pollingThread.interrupt();
    } catch (SecurityException e) {
      return Validation.fail(e);
    }

    this.logger.warn("(Main Thread) Interrupting Kafka Thread");
    return Validation.success(this.pollingThread.getId());
  }

}
