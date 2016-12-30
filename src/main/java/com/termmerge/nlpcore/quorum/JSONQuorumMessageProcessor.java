package com.termmerge.nlpcore.quorum;

import java.lang.reflect.Type;

import java.util.Date;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fj.data.Validation;

import com.google.gson.JsonSyntaxException;

/**
 * Implementation of QuorumMessageProcessor, where serialization
 *  and deserialization is concerned with JSON as a network transport
 *  protocol
 */
public class JSONQuorumMessageProcessor
        implements QuorumMessageProcessor
{

  private Gson gson;

  public JSONQuorumMessageProcessor()
  {
    JsonDeserializer jsonDeserializer = new JsonDeserializer<Date>()
    {
      @Override
      public Date deserialize(
              JsonElement jsonElement,
              Type type,
              JsonDeserializationContext jsonDeserializationContext
      )
      {
        return new Date(jsonElement.getAsJsonPrimitive().getAsLong());
      }
    };

    this.gson = (new GsonBuilder()).registerTypeAdapter(
            Date.class,
            jsonDeserializer
    ).create();
  }

  public Validation<RuntimeException, String> serialize(
          QuorumMessage quorumMessage
  )
  {
    String serialized;
    try {
      serialized = this.gson.toJson(quorumMessage);
    } catch (JsonSyntaxException e) {
      return Validation.fail(e);
    }

    return Validation.success(serialized);
  }

  public Validation<RuntimeException, QuorumMessage> deserialize(
          String serialized
  )
  {
    QuorumMessage deserialized;
    try {
      deserialized = this.gson.fromJson(
              serialized, QuorumMessage.class
      );
    } catch (JsonSyntaxException e) {
      return Validation.fail(e);
    }
    return Validation.success(deserialized);
  }

}
