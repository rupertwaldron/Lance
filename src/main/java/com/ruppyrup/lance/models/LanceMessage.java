package com.ruppyrup.lance.models;

public class LanceMessage implements Message {
  private String contents;
  private Topic topic;

  public LanceMessage(Topic topic, String contents) {
    this.contents = contents;
    this.topic = topic;
  }

  @Override
  public String getContents() {
    return contents;
  }

  @Override
  public void setContents(String contents) {
    this.contents = contents;
  }

  @Override
  public Topic getTopic() {
    return topic;
  }

  @Override
  public String toString() {
    return "LanceMessage{" +
        "contents='" + contents + '\'' +
        ", topic=" + topic +
        '}';
  }
}
