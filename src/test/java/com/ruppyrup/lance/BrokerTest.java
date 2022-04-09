package com.ruppyrup.lance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BrokerTest {

  private MockTransceiver udpTransceiver;
  private Broker lanceBroker;

  @Test
  void testOnlyOneInstanceOfBrokerIsCreated() {
    LanceBroker lanceBroker1 = LanceBroker.getInstance();
    LanceBroker lanceBroker2 = LanceBroker.getInstance();
    assertEquals(lanceBroker1, lanceBroker2);
  }

  @Nested
  @DisplayName("Broker send and receive tests")
  class SendReceiveTest {

    @BeforeEach
    private void setup() {
      udpTransceiver = new MockTransceiver();
      lanceBroker = LanceBroker.getInstance();
      lanceBroker.setTransceiver(udpTransceiver);
    }

    @Test
    void testBrokerCanReceiveUdpPackets() {
      String expected1 = "expected1";
      Topic topic1 = new Topic("Test1");
      setTransceiverMessageString(topic1, expected1);
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      lanceBroker.receive();
      assertEquals(2, udpTransceiver.getReceiveCount());
    }
  }

  @Nested
  @DisplayName("Process message tests")
  class MessageProcessTests {

    private final String expected2 = "expected2";
    private final String expected1 = "expected2";
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    private void setup() {
      udpTransceiver = new MockTransceiver();
      lanceBroker = LanceBroker.getInstance();
      lanceBroker.setTransceiver(udpTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }

    @Test
    void testBrokerStoresMessagesByTopic() {
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic2, expected2);
      lanceBroker.receive();
      assertEquals(expected1,
          lanceBroker.getNextMessageForTopic(topic1).orElseThrow().getContents());
      assertEquals(expected2,
          lanceBroker.getNextMessageForTopic(topic2).orElseThrow().getContents());
    }

    @Test
    void testBrokerStoresMultipleMessagesByTheSameTopic() {
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic1, expected2);
      lanceBroker.receive();
      assertEquals(expected1,
          lanceBroker.getNextMessageForTopic(topic1).orElseThrow().getContents());
      assertEquals(expected2,
          lanceBroker.getNextMessageForTopic(topic1).orElseThrow().getContents());
    }
  }

  private void setTransceiverMessageString(Topic topic, String s) {
    LanceMessage message = new LanceMessage(topic, s);
    udpTransceiver.setMessage(message);
  }

  @Nested
  @DisplayName("Process message tests")
  class MessageSendingTests {

    private final String expected1 = "expected1";
    private final String expected2 = "expected2";
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    private void setup() {
      udpTransceiver = new MockTransceiver();
      lanceBroker = LanceBroker.getInstance();
      lanceBroker.setTransceiver(udpTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }


    @Test
    void testBrokerSendsStoredMessagesFromTheSameTopic() {
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic1, expected2);
      lanceBroker.receive();
      lanceBroker.send();
      Assertions.assertEquals(2, udpTransceiver.getSendCount());
    }

    @Test
    void testBrokerSendsStoredMessagesFromTheDifferentTopics() {
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic2, expected2);
      lanceBroker.receive();
      lanceBroker.send();
      Assertions.assertEquals(2, udpTransceiver.getSendCount());
    }
  }

  @Nested
  @DisplayName("Subscriber Tests")
  class SubscriberTests {

    private final String expected1 = "expected1";
    private final String expected2 = "expected2";
    private Topic topic1;
    private Topic topic2;

    @BeforeEach
    private void setup() {
      udpTransceiver = new MockTransceiver();
      lanceBroker = LanceBroker.getInstance();
      lanceBroker.setTransceiver(udpTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }

    @Test
    void testRegisterSubscriber() {
      Subscriber subscriber = new MockSubscriber();
      lanceBroker.register(topic1, subscriber);
      Assertions.assertTrue(lanceBroker.getSubscribers().containsKey(topic1));
      Assertions.assertTrue(lanceBroker.getSubscribers().containsValue(subscriber));
    }

    @Test
    void testRegisterMultipleSubscribers() {
      Subscriber subscriber1 = new MockSubscriber();
      Subscriber subscriber2 = new MockSubscriber();
      lanceBroker.register(topic1, subscriber1);
      lanceBroker.register(topic2, subscriber2);
      Assertions.assertEquals(subscriber1, lanceBroker.getSubscribers().get(topic1));
      Assertions.assertEquals(subscriber2, lanceBroker.getSubscribers().get(topic2));
    }
  }
}

class MockTransceiver implements Transceiver {

  private final List<Message> messages = new ArrayList<>();
  private int receiveCount;
  private int sendCount;
  private int messageCount;
  private int messageIndex;

  MockTransceiver() {
  }

  @Override
  public void send(Message message) {
    System.out.println("Sending message " + message);
    sendCount++;
  }

  @Override
  public Message receive() {
    return messages.get(receiveCount++);
  }

  public int getReceiveCount() {
    return receiveCount;
  }

  public int getSendCount() {
    return sendCount;
  }

  public void setMessage(Message message) {
    messages.add(message);
    messageCount++;
  }
}

class MockSubscriber implements Subscriber {

  int port = 8888;

  @Override
  public int getPort() {
    return 0;
  }
}
