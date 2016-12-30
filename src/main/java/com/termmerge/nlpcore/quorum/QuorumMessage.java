package com.termmerge.nlpcore.quorum;

import java.util.Date;
import java.util.Properties;
import java.util.Arrays;

import fj.data.Validation;


/**
 * Structure of QuorumMessage, which represents the messages sent through the
 *  message bus. It can represent either a request to run some NLP-based
 *  computation, a response to a request or analytics messages
 */
public class QuorumMessage
{

  // Values allowable for "sender" and "recipient"
  public static final String GROUP_NLP_INTERFACE = "nlpcore_interface";
  public static final String GROUP_NLP_COMPUTE = "nlpcore_compute";
  private static String[] ALLOWABLE_SENDER_RECIPIENT = {
          GROUP_NLP_INTERFACE, GROUP_NLP_COMPUTE
  };

  // Values allowable for "task"
  public static final String STREAM_ANALYTICS = "stream_analytics";
  public static final String BATCH_ANALYTICS = "batch_analytics";
  public static final String CONVERGENCE_RADIUS = "convergence_radius";
  public static final String NLP_TOKENIZE = "nlp_tokenize";
  public static final String NLP_SENTENCE = "nlp_sentence";
  public static final String NLP_POS = "nlp_pos";
  public static final String NLP_NER = "nlp_ner";
  public static final String NLP_CONSTITUENCY = "nlp_constituency";
  public static final String NLP_DEPENDENCY = "nlp_dependency";
  public static final String NLP_COREFERENCE = "nlp_coreference";
  public static final String NLP_NATLOG_POLARITY = "nlp_natlog_polarity";
  public static final String OPEN_INFO_EXTRACT = "nlp_openinfo_extract";
  public static final String WORDNET= "wordnet";
  public static final String FRAMENET = "framenet";
  private static String[] ALLOWABLE_TASKS = {
          STREAM_ANALYTICS, BATCH_ANALYTICS, CONVERGENCE_RADIUS,
          NLP_TOKENIZE, NLP_SENTENCE, NLP_POS, NLP_NER, NLP_CONSTITUENCY,
          NLP_DEPENDENCY, NLP_COREFERENCE, NLP_NATLOG_POLARITY,
          OPEN_INFO_EXTRACT, WORDNET, FRAMENET
  };

  // Where this message originated from
  private String sender;

  // Who is the message intended for
  private String recipient;

  // What task is this message intended for processing
  private String task;

  // When combined with "task", uniquely identifiable atomic NLP task
  //  for some singular client
  private String taskId;

  // What time was this message created
  private Date time;

  // Additional parameters for the task
  private Properties params;

  /**
   * Allows one to safely create instances of QuorumMessage
   */
  public static class Builder
  {

    private String sender;
    private String recipient;
    private String task;
    private String taskId;
    private Date time;
    private Properties params;

    public Builder asResponseMessage()
    {
      this.sender = QuorumMessage.GROUP_NLP_COMPUTE;
      this.recipient = QuorumMessage.GROUP_NLP_INTERFACE;
      return this;
    }

    public Builder asRequestMessage()
    {
      this.sender = QuorumMessage.GROUP_NLP_INTERFACE;
      this.recipient = QuorumMessage.GROUP_NLP_COMPUTE;
      return this;
    }

    public Builder fromTo(String sender, String recipient)
    {
      this.sender = sender;
      this.recipient = recipient;
      return this;
    }

    public Builder setTask(String task)
    {
      this.task = task;
      return this;
    }

    public Builder setParam(String key, String value)
    {
      if (this.params == null) {
        this.params = new Properties();
      }

      this.params.setProperty(key, value);
      return this;
    }

    public Validation<RuntimeException, QuorumMessage> build(
            TaskIdManager taskIdManager
    )
    {
      this.taskId = taskIdManager.generateId();
      this.time = new Date();
      if (!Arrays.asList(QuorumMessage.ALLOWABLE_SENDER_RECIPIENT)
              .contains(this.sender) ||
              !Arrays.asList(QuorumMessage.ALLOWABLE_SENDER_RECIPIENT)
                      .contains(this.recipient) ||
              !Arrays.asList(QuorumMessage.ALLOWABLE_TASKS)
                      .contains(this.task)
              ) {
        return Validation.fail(new IllegalStateException(
                "Cannot construct valid QuorumMessage - Invalid " +
                        "values for sender, recipient and/or task."
        ));
      }

      return Validation.success(new QuorumMessage(this));
    }

  }

  private QuorumMessage(Builder builder)
  {
    this.sender = builder.sender;
    this.recipient = builder.recipient;
    this.task = builder.task;
    this.taskId = builder.taskId;
    this.time = builder.time;
    this.params = builder.params;
  }

  public String getSender()
  {
    return this.sender;
  }

  public String getRecipient()
  {
    return this.recipient;
  }

  public String getTask()
  {
    return this.task;
  }

  public String getTaskId()
  {
    return this.taskId;
  }

  public Date getTime()
  {
    return (Date) this.time.clone();
  }

  public Properties getParams()
  {
    return (Properties) this.params.clone();
  }

}
