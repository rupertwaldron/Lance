# Created by rupertwaldron at 11/04/2022
Feature: Publish and Subscribe Feature
  Check a class can publish to a topic sends data and then
  another class can receive the data for that topic

  Scenario: Can publish and receive data
    Given Lance Broker is receiving message data
    Given a udp message is created with data "test message" and topic "topic1"
    When a publisher sends the message to Lance Broker
    Then Lance Broker will store the message under the correct topic

  Scenario: Can add a subscriber
    Given Lance Broker is receiving subscriptions
    And a subscriber registers for the topic on port 3333 for topic "topic1" with subscriber name "subName1"
    Then the subscriber will be found for that topic

  Scenario: Subscriber receives message
    Given Lance Broker is receiving message data
    And Lance Broker is receiving subscriptions
    And a subscriber registers for the topic on port 3333 for topic "topic1" with subscriber name "subName1"
    And a udp message is created with data "test message" and topic "topic1"
    When a publisher sends the message to Lance Broker
