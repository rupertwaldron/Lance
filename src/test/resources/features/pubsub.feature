# Created by rupertwaldron at 11/04/2022
Feature: Publish and Subscribe Feature
  Check a class can publish to a topic sends data and then
  another class can receive the data for that topic

  Scenario: Subscriber receives message
    Given Lance Broker is receiving message data
    And Lance Broker is receiving subscriptions
    And Lance Broker is sending every 3 milliseconds intervals
    And a subscriber is created with listening port 3333
    And a subscriber registers for the topic "topic1" with subscriber name "subName1"
    Then the subscriber will be found for that topic
    And a udp message is created with data "test message" and topic "topic1"
    When a publisher sends the message to Lance Broker
    Then the subscriber receives the message
