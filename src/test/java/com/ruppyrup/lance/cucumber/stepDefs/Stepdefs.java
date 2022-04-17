package com.ruppyrup.lance.cucumber.stepDefs;

import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.publisher.LancePublish;
import com.ruppyrup.lance.subscriber.LanceSubscribe;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
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
    LanceBroker.getInstance().closeSockets();
    List<Runnable> schedulerService = TestData.getData("schedulerService",
        ScheduledExecutorService.class).shutdownNow();
    TestData.clear();
  }

  @Given("Lance Broker is receiving subscriptions")
  public void lanceBrokerIsReceivingUdpData() throws SocketException, UnknownHostException {
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446),
        InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> LanceBroker.getInstance().register());
    TestData.setData("subscriberFuture", subscriberFuture);
  }

  @Given("Lance Broker is receiving message data")
  public void lanceBrokerIsReceivingMessageData() throws SocketException, UnknownHostException {
    Transceiver msgTransceiver = new MsgTransceiver(new DatagramSocket(4445),
        InetAddress.getLocalHost(), 4445);
    LanceBroker.getInstance().setMsgTransceiver(msgTransceiver);
    CompletableFuture<Void> receiverFuture = CompletableFuture.runAsync(
        () -> LanceBroker.getInstance().receive());
    TestData.setData("receiverFuture", receiverFuture);
  }

  @Given("a udp message is created with data {string} and topic {string}")
  public void aUdpMessageIsCreatedWithDataAndTopic(String data, String topic) {
    Topic topic1 = new Topic(topic);
    Message message1 = new DataMessage(topic1, data);
    TestData.setData("message1", message1);
    TestData.setData("topic1", topic1);
  }

  @When("a publisher sends the message to Lance Broker")
  public void aPublisherSendsTheMessageToLanceBroker() {
    Message message = TestData.getData("message1", Message.class);
    new LancePublish().publish(message);
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
    TestData.setData("topic1", topic1);
    TestData.setData("subName", subscriberName);
    LanceSubscribe lanceSubscribe = TestData.getData("lanceSubscribe", LanceSubscribe.class);
    lanceSubscribe.subscribe(subscriberName, topic1);
  }

  @Then("the subscriber will be found for that topic")
  public void theSubscriberWillBeFoundForThatTopic() {
    Topic topic = TestData.getData("topic1", Topic.class);
    String subscriberName = TestData.getData("subName", String.class);
    int subscriberPort = TestData.getData("subPort", Integer.class);
    CompletableFuture<Void> subscriberFuture = TestData.getData("subscriberFuture",
        CompletableFuture.class);
    subscriberFuture.join();
    List<Subscriber> subscribersByTopic = LanceBroker.getInstance().getSubscribersByTopic(topic);
    Assertions.assertEquals(subscriberPort, subscribersByTopic.get(0).getPort());
    Assertions.assertEquals(subscriberName, subscribersByTopic.get(0).getSubscriberName());
  }

  @Given("Lance Subscriber is receiving data on port {int}")
  public void lanceSubscribeIsReceivingUdpData(int port) {
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> LanceBroker.getInstance().register());
    TestData.setData("subscriberFuture", subscriberFuture);
  }

  @And("a subscriber is created with listening port {int}")
  public void aSubscriberIsCreatedWithListeningPort(int port) {
    LanceSubscribe lanceSubscribe = new LanceSubscribe(port);
    TestData.setData("lanceSubscribe", lanceSubscribe);
    TestData.setData("subPort", port);
  }

  @Then("the subscriber receives the message")
  public void theSubscriberReceivesTheMessage() {
    LanceSubscribe lanceSubscribe = TestData.getData("lanceSubscribe", LanceSubscribe.class);
    Optional<Message> receivedMessage = lanceSubscribe.receive();
    DataMessage expectedMessage = TestData.getData("message1", DataMessage.class);
    Assertions.assertEquals(expectedMessage, receivedMessage.orElse(null));
  }

  @And("Lance Broker is sending every {int} milliseconds intervals")
  public void lanceBrokerIsSendingEveryMillisecondsIntervals(int interval) {
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(() -> LanceBroker.getInstance().send(), 5, interval, TimeUnit.MILLISECONDS);
    TestData.setData("schedulerService", service);
  }
}
