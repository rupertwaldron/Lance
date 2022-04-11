package com.ruppyrup.lance.cucumber.stepDefs;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.cucumber.publisher.LancePublish;
import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.UdpTransceiver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
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

  @Given("a udp message is created with data {string} and topic {string}")
  public void aUdpMessageIsCreatedWithDataAndTopic(String data, String topic) {
    Topic topic1 = new Topic(topic);
    Message message1 = new LanceMessage(topic1, data);
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
    Optional<Message> message = LanceBroker.getInstance().getNextMessageForTopic(topic);
    Assertions.assertEquals(expectedMessage, message.orElse(new LanceMessage()));
  }

  @Given("Lance Broker is receiving udp data")
  public void lanceBrokerIsReceivingUdpData() throws SocketException, UnknownHostException {
    Transceiver transceiver = new UdpTransceiver(new DatagramSocket(4445), InetAddress.getLocalHost(), 4445);
    LanceBroker.getInstance().setTransceiver(transceiver);
    CompletableFuture<Void> receiverFuture = CompletableFuture.runAsync(
        () -> {
          while(true){
            LanceBroker.getInstance().receive();
          }
        });
    TestData.setData("receiverFuture", receiverFuture);
  }
}
