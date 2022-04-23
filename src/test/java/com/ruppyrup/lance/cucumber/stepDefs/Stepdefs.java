package com.ruppyrup.lance.cucumber.stepDefs;

import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.publisher.LancePublisher;
import com.ruppyrup.lance.subscriber.LanceSubscriber;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.MsgTransceiver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;

public class Stepdefs {

  @Before
  public void setup() {

  }

  @After
  public void teardown() {
    LanceBroker.getInstance().close();
    TestData.getData("schedulerService", ScheduledExecutorService.class).shutdownNow();
    TestData.clear();
  }

  @Given("Lance Broker is receiving {int} subscriptions")
  public void lanceBrokerIsReceivingUdpData(int subCount) throws SocketException, UnknownHostException {
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446),
        InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> {
          for (int i = 0; i < subCount; i++)
            LanceBroker.getInstance().register();
        });
    TestData.setData("subscriberFuture", subscriberFuture);
  }

  @Given("Lance Broker can receive {int} message(s)")
  public void lanceBrokerIsReceivingMessageData(int messageCount) throws SocketException, UnknownHostException {
    Transceiver msgTransceiver = new MsgTransceiver(new DatagramSocket(4445),
        InetAddress.getLocalHost(), 4445);
    LanceBroker.getInstance().setMsgTransceiver(msgTransceiver);
    CompletableFuture<Void> receiverFuture = CompletableFuture.runAsync(
        () -> {
          for (int i = 0; i < messageCount; i++)
            LanceBroker.getInstance().receive();
        });
    TestData.setData("receiverFuture", receiverFuture);
  }

  @Given("a udp message is created with data {string} and topic {string}")
  public void aUdpMessageIsCreatedWithDataAndTopic(String data, String topic) {
    Topic topic1 = new Topic(topic);
    Message message1 = new DataMessage(topic1, data);
    TestData.setData(data, message1);
  }

  @When("a publisher sends the message {string} to Lance Broker {int} time(s)")
  public void aPublisherSendsTheMessageToLanceBroker(String messageData, int publishCount) throws SocketException, UnknownHostException {
    Message message = TestData.getData(messageData, Message.class);
    var publisher = new LancePublisher();
    publisher.start();
    for (int i = 0; i < publishCount; i++) {
      publisher.publish(message);
    }
  }

  @Then("Lance Broker will store the message under the correct topic")
  public void lanceBrokerWillStoreTheMessageUnderTheCorrectTopic() {
    Topic topic = TestData.getData("topic1", Topic.class);
    Message expectedMessage = TestData.getData("message1", Message.class);
    CompletableFuture<Void> receiverFuture = TestData.getData("receiverFuture",
        CompletableFuture.class);
    receiverFuture.join();
    Optional<Message> message = LanceBroker.getInstance().getNextMessageForTopic(topic);
    Assertions.assertEquals(expectedMessage, message.orElse(new DataMessage()));
  }

  @When("a subscriber registers for the topic {string} with subscriber name {string}")
  public void aSubscriberRegistersForTopic(String topic, String subscriberName) {
    Topic topic1 = new Topic(topic);
    TestData.setData(topic, topic1);
    LanceSubscriber lanceSubscriber = TestData.getData(subscriberName, LanceSubscriber.class);
    lanceSubscriber.subscribe(subscriberName, topic1);
  }

  @Then("{int} subscriber(s) will be found for topic {string}")
  public void theNumberOfSubscribersWillBeFoundForThatTopic(int subscriberCount, String topicName) {
    Topic topic = TestData.getData(topicName, Topic.class);
    CompletableFuture<Void> subscriberFuture = TestData.getData("subscriberFuture",
        CompletableFuture.class);
    subscriberFuture.join();
    List<SubscriberInfo> subscribersByTopic = LanceBroker.getInstance().getSubscribersByTopic(topic);
    Assertions.assertEquals(subscriberCount, subscribersByTopic.size());
  }

  @Given("Lance Subscriber is receiving data on port {int}")
  public void lanceSubscribeIsReceivingUdpData(int port) {
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> LanceBroker.getInstance().register());
    TestData.setData("subscriberFuture", subscriberFuture);
  }

  @And("a subscriber is created with listening port {int} with name {string}")
  public void aSubscriberIsCreatedWithListeningPort(int port, String subscriberName)
      throws SocketException, UnknownHostException {
    LanceSubscriber lanceSubscriber = new LanceSubscriber(port);
    lanceSubscriber.start();
    TestData.setData(subscriberName, lanceSubscriber);
    TestData.setData(subscriberName + "Port", port);
  }

  @Then("the subscriber with name {string} receives the message {string} {int} time(s)")
  public void theSubscriberReceivesTheMessage(String subscriberName, String messageData, int messageCount) {
    LanceSubscriber lanceSubscriber = TestData.getData(subscriberName, LanceSubscriber.class);
    DataMessage expectedMessage = TestData.getData(messageData, DataMessage.class);
    for (int i = 0; i < messageCount; i++) {
      Message receivedMessage = lanceSubscriber.receive();
      Assertions.assertEquals(expectedMessage, receivedMessage);
    }
  }

  @And("Lance Broker is sending every {int} milliseconds intervals")
  public void lanceBrokerIsSendingEveryMillisecondsIntervals(int interval) {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(() -> LanceBroker.getInstance().send(), 5, interval, TimeUnit.MILLISECONDS);
    TestData.setData("schedulerService", service);
  }
}
