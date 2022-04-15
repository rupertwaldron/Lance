package com.ruppyrup.lance.cucumber.stepDefs;

import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.cucumber.publisher.LancePublish;
import com.ruppyrup.lance.cucumber.subscriber.LanceSubscribe;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriber;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.MsgTransceiver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
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
import org.junit.jupiter.api.Assertions;

public class Stepdefs {

  @Before
  public void setup(){

  }

  @After
  public void teardown() {
    TestData.clear();
  }

  @Given("Lance Broker is receiving subscriptions")
  public void lanceBrokerIsReceivingUdpData() throws SocketException, UnknownHostException {
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446), InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> LanceBroker.getInstance().register());
    TestData.setData("subscriberFuture", subscriberFuture);
  }

  @Given("Lance Broker is receiving message data")
  public void lanceBrokerIsReceivingMessageData() throws SocketException, UnknownHostException {
    Transceiver msgTransceiver = new MsgTransceiver(new DatagramSocket(4445), InetAddress.getLocalHost(), 4445);
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
    LancePublish.publish(message);
  }

  @Then("Lance Broker will store the message under the correct topic")
  public void lanceBrokerWillStoreTheMessageUnderTheCorrectTopic() {
    Topic topic = TestData.getData("topic1", Topic.class);
    Message expectedMessage = TestData.getData("message1", Message.class);
    CompletableFuture<Void> receiverFuture = TestData.getData("receiverFuture", CompletableFuture.class);
    receiverFuture.join();
    Optional<Message> message = LanceBroker.getInstance().getNextMessageForTopic(topic);
    Assertions.assertEquals(expectedMessage, message.orElse(new DataMessage()));
  }

  @When("a subscriber registers for the topic on port {int} for topic {string} with subscriber name {string}")
  public void aSubscriberRegistersForTopic(int port, String topic, String subscriberName) {
    Topic topic1 = new Topic(topic);
    TestData.setData("topic1", topic1);
    TestData.setData("subPort", port);
    TestData.setData("subName", subscriberName);
    LanceSubscribe lanceSubscribe = new LanceSubscribe(port);
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
}
