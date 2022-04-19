package com.ruppyrup.lance.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MessageUtilsTest {

  @Test
  void getMessageReturnsCorrectBytes() {
    byte[] expectedBytes = {123, 34, 99, 111, 110, 116, 101, 110, 116, 115, 34, 58, 34, 109, 101, 115, 115, 97, 103, 101, 34, 44, 34, 116, 111, 112, 105, 99, 34, 58, 123, 34, 116, 111, 112, 105, 99, 78, 97, 109, 101, 34, 58, 34, 116, 111, 112, 105, 99, 49, 34, 125, 125};
    Topic topic = new Topic("topic1");
    Message message = new DataMessage(topic, "message");
    byte[] messageBytes = MessageUtils.getMessageBytes(message);
    assertArrayEquals(expectedBytes, messageBytes);
  }
}
