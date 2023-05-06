package com.ruppyrup.lance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriberInfo;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BrokerTest {

  private MockTransceiver udpTransceiver;
  private LanceBroker lanceBroker;

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
      lanceBroker.setMsgTransceiver(udpTransceiver);
    }

    @Test
    void testBrokerCanReceiveUdpPackets() {
      lanceBroker.setEmpty(new Semaphore(2));
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
      lanceBroker.setMsgTransceiver(udpTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }

    @Test
    void testBrokerStoresMessagesByTopic() {
      lanceBroker.setEmpty(new Semaphore(2));
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
      lanceBroker.setEmpty(new Semaphore(2));
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic1, expected2);
      lanceBroker.receive();
      assertEquals(expected1,
          lanceBroker.getNextMessageForTopic(topic1).orElseThrow().getContents());
      assertEquals(expected2,
          lanceBroker.getNextMessageForTopic(topic1).orElseThrow().getContents());
    }

    @Test
    void closeSocketsIsCalled() {
      lanceBroker.clearMessages();
      lanceBroker.close();
      assertTrue(udpTransceiver.isClosed());
    }
  }

  private void setTransceiverMessageString(Topic topic, String s) {
    DataMessage message = new DataMessage(topic, s);
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
      lanceBroker.setMsgTransceiver(udpTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }


    @Test
    void testBrokerSendsStoredMessagesFromTheSameTopic() {
      lanceBroker.setEmpty(new Semaphore(2));
      setTransceiverMessageString(topic1, expected1);
      lanceBroker.receive();
      setTransceiverMessageString(topic1, expected2);
      lanceBroker.receive();
      lanceBroker.send();
      Assertions.assertEquals(2, udpTransceiver.getSendCount());
    }

    @Test
    void testBrokerSendsStoredMessagesFromTheDifferentTopics() {
      lanceBroker.setEmpty(new Semaphore(2));
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
  class SubscribeTests {

    private Topic topic1;
    private Topic topic2;
    MockSubTransceiver subTransceiver;

    @BeforeEach
    private void setup() {
      udpTransceiver = new MockTransceiver();
      subTransceiver = new MockSubTransceiver();
      lanceBroker = LanceBroker.getInstance();
      lanceBroker.setMsgTransceiver(udpTransceiver);
      lanceBroker.setSubTransceiver(subTransceiver);
      topic1 = new Topic("Test1");
      topic2 = new Topic("Test2");
    }

    @AfterEach
    private void tearDown() {
      if (lanceBroker.getSubscribersByTopic(topic1) != null) lanceBroker.getSubscribersByTopic(topic1).clear();
      if (lanceBroker.getSubscribersByTopic(topic2) != null) lanceBroker.getSubscribersByTopic(topic2).clear();
    }

    @Test
    void testRegisterSubscriber() {
      SubscriberInfo subscribe = new LanceSubscriberInfo("sub1", 1001);
      Message subMessage = new DataMessage(topic1, subscribe.toJsonString());
      subTransceiver.setMessage(subMessage);
      lanceBroker.register();
      Assertions.assertEquals(subscribe.toJsonString(), lanceBroker.getSubscribersByTopic(topic1).get(0).toJsonString());
    }

    @Test
    void testRegisterMultipleSubscribers() {
      SubscriberInfo subscribe1 = new LanceSubscriberInfo("sub1", 1001);
      SubscriberInfo subscribe2 = new LanceSubscriberInfo("sub2", 1002);
      Message subMessage1 = new DataMessage(topic1, subscribe1.toJsonString());
      Message subMessage2 = new DataMessage(topic2, subscribe2.toJsonString());
      subTransceiver.setMessage(subMessage1);
      subTransceiver.setMessage(subMessage2);
      lanceBroker.register();
      lanceBroker.register();
      Assertions.assertEquals(subscribe1, lanceBroker.getSubscribersByTopic(topic1).get(0));
      Assertions.assertEquals(subscribe2, lanceBroker.getSubscribersByTopic(topic2).get(0));
    }

    @Test
    void testRegisterMultipleSubscribersToSameTopic() {
      SubscriberInfo subscribe1 = new LanceSubscriberInfo("sub1", 1001);
      SubscriberInfo subscribe2 = new LanceSubscriberInfo("sub2", 1002);
      Message subMessage1 = new DataMessage(topic1, subscribe1.toJsonString());
      Message subMessage2 = new DataMessage(topic1, subscribe2.toJsonString());
      subTransceiver.setMessage(subMessage1);
      subTransceiver.setMessage(subMessage2);
      lanceBroker.register();
      lanceBroker.register();

      SubscriberInfo[] subArray = {subscribe1, subscribe2};
      Assertions.assertArrayEquals(subArray, lanceBroker.getSubscribersByTopic(topic1).toArray(
          SubscriberInfo[]::new));
    }

    @Test
    void testSubscribeTwiceToRegisterTheMostRecentSubscriber() {
      SubscriberInfo subscribe1 = new LanceSubscriberInfo("sub1", 1001);
      SubscriberInfo subscribe2 = new LanceSubscriberInfo("sub1", 1002);
      Message subMessage1 = new DataMessage(topic1, subscribe1.toJsonString());
      Message subMessage2 = new DataMessage(topic1, subscribe2.toJsonString());
      subTransceiver.setMessage(subMessage1);
      subTransceiver.setMessage(subMessage2);
      lanceBroker.register();
      lanceBroker.register();
      SubscriberInfo[] subArray = {subscribe2};
      Assertions.assertArrayEquals(subArray, lanceBroker.getSubscribersByTopic(topic1).toArray(SubscriberInfo[]::new));
    }
  }
}

class MockTransceiver implements Transceiver {

  private final List<Message> messages = new ArrayList<>();
  private int receiveCount;
  private int sendCount;
  private boolean isClosed;

  MockTransceiver() {
  }

  @Override
  public void send(Message message, List<SubscriberInfo> subscribes) {
    System.out.println("Sending message " + message);
    sendCount++;
  }

  @Override
  public Optional<Message> receive() {
    return Optional.of(messages.get(receiveCount++));
  }

  @Override
  public void close() {
    isClosed = true;
  }

  public boolean isClosed() {
    return isClosed;
  }

  public int getReceiveCount() {
    return receiveCount;
  }

  public int getSendCount() {
    return sendCount;
  }

  public void setMessage(Message message) {
    messages.add(message);
  }
}

class MockSubTransceiver implements Transceiver {

  private final List<Message> messages = new ArrayList<>();
  private int receiveCount;
  private int sendCount;

  MockSubTransceiver() {
  }

  @Override
  public void send(Message message, List<SubscriberInfo> subscribes) {
    System.out.println("Sending message " + message);
    sendCount++;
  }

  @Override
  public Optional<Message> receive() {
    return Optional.of(messages.get(receiveCount++));
  }

  @Override
  public void close() {

  }

  public int getReceiveCount() {
    return receiveCount;
  }

  public int getSendCount() {
    return sendCount;
  }

  public void setMessage(Message message) {
    messages.add(message);
  }
}
