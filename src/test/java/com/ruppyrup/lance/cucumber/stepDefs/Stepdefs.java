package com.ruppyrup.lance.cucumber.stepDefs;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Stepdefs {

  @Given("a udp message is created with data {string} and topic {string}")
  public void aUdpMessageIsCreatedWithDataAndTopic(String data, String topic) {

  }

  @When("a publisher sends the message to Lance Broker")
  public void aPublisherSendsTheMessageToLanceBroker() {

  }

  @Then("Lance Broker will store the message under the correct topic")
  public void lanceBrokerWillStoreTheMessageUnderTheCorrectTopic() {

  }
}
