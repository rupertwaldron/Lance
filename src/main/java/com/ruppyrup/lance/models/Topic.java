package com.ruppyrup.lance.models;

public class Topic {
  private final String topicName;

  public Topic(String topicName) {
    this.topicName = topicName;
  }

  @Override
  public String toString() {
    return "Topic{" +
        "topicName='" + topicName + '\'' +
        '}';
  }
}
