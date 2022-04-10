package com.ruppyrup.lance.models;

import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LanceMessage that = (LanceMessage) o;

    if (!Objects.equals(contents, that.contents)) {
      return false;
    }
    return Objects.equals(topic, that.topic);
  }

  @Override
  public int hashCode() {
    int result = contents != null ? contents.hashCode() : 0;
    result = 31 * result + (topic != null ? topic.hashCode() : 0);
    return result;
  }
}
