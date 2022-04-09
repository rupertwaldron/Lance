package com.ruppyrup.lance.models;

import java.util.Objects;

public record Topic(String topicName) {

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Topic topic = (Topic) o;

    return Objects.equals(topicName, topic.topicName);
  }

  @Override
  public int hashCode() {
    return topicName != null ? topicName.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Topic{" +
        "topicName='" + topicName + '\'' +
        '}';
  }
}
