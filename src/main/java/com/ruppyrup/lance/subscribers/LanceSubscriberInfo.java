package com.ruppyrup.lance.subscribers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class LanceSubscriberInfo implements SubscriberInfo {

  private String subscriberName;
  private int port;

  public LanceSubscriberInfo(String subscriberName, int port) {
    this.subscriberName = subscriberName;
    this.port = port;
  }

  @Override
  public String getSubscriberName() {
    return subscriberName;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String toJsonString() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "";
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LanceSubscriberInfo that = (LanceSubscriberInfo) o;

    if (port != that.port) {
      return false;
    }
    return Objects.equals(subscriberName, that.subscriberName);
  }

  @Override
  public int hashCode() {
    int result = subscriberName != null ? subscriberName.hashCode() : 0;
    result = 31 * result + port;
    return result;
  }

  @Override
  public String toString() {
    return "LanceSubscriber{" +
        "subscriberName='" + subscriberName + '\'' +
        ", port=" + port +
        '}';
  }
}
