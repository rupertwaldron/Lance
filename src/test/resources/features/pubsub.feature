# Created by rupertwaldron at 11/04/2022
Feature: Publish and Subscribe Feature
  Check a class can publish to a topic sends data and then
  another class can receive the data for that topic

  Scenario: Subscriber receives message
    Given Lance Broker can receive 1 message
    And Lance Broker is receiving 1 subscriptions
    And Lance Broker is sending every 3 milliseconds intervals
    And a subscriber is created with listening port 3333 with name "<subscriberName>"
    And a subscriber registers for the topic "<topicName>" with subscriber name "<subscriberName>"
    Then 1 subscriber will be found for topic "<topicName>"
    And a udp message is created with data "<message>" and topic "<topicName>"
    When a publisher sends the message "<message>" to Lance Broker
    Then the subscriber with name "<subscriberName>" receives the message "<message>"

    Example:
      | topicName | subscriberName | message      |
      | topic1    | subName1       | test message |

  Scenario: Subscriber unsubscribes and receives no messages
    Given Lance Broker can receive 1 message
    And Lance Broker is receiving 2 subscriptions
    And Lance Broker is sending every 3 milliseconds intervals
    And a subscriber is created with listening port 3333 with name "subName1"
    And a subscriber registers for the topic "topic1" with subscriber name "subName1"
    And a subscriber registers for the topic "topic1" with subscriber name "subName1"
    Then 0 subscribers will be found for topic "topic1"

  Scenario: Multiple subscribers receive their respective messages
    Given Lance Broker can receive 2 messages
    And Lance Broker is receiving 2 subscriptions
    And Lance Broker is sending every 3 milliseconds intervals
    And a subscriber is created with listening port 3333 with name "subName1"
    And a subscriber registers for the topic "topic1" with subscriber name "subName1"
    And a subscriber is created with listening port 3334 with name "subName2"
    And a subscriber registers for the topic "topic2" with subscriber name "subName2"
    Then 1 subscriber will be found for topic "topic1"
    And 1 subscriber will be found for topic "topic2"
    And a udp message is created with data "test1 message1" and topic "topic1"
    And a udp message is created with data "test2 message2" and topic "topic2"
    When a publisher sends the message "test1 message1" to Lance Broker
    When a publisher sends the message "test2 message2" to Lance Broker
    Then the subscriber with name "subName1" receives the message "test1 message1"
    Then the subscriber with name "subName2" receives the message "test2 message2"

