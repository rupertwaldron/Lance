@Standard
Feature: Publish and Subscribe Feature
  Check a class can publish to a topic sends data and then
  another class can receive the data for that topic

  Scenario: Subscriber receives message
    Given Lance Broker has a subscriber port of 5556 and a message port of 4445
    And Lance Broker can receive 1 message
    And Lance Broker is receiving 1 subscriptions
    And a subscriber is created with listening port 3333 with name "subscriberName"
    When a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    And 1 subscriber will be found for topic "topicName"
    And a udp message is created with data "message" and topic "topicName"
    And a publisher sends the message "message" to Lance Broker 1 time
    Then the subscriber with name "subscriberName" receives the message "message" 1 time

  Example: One subscriber one message
  | topicName | subscriberName | message      |
  | topic1    | subName1       | test message |

  Scenario: Lance takes the latest subscriber by name not port
    Given Lance Broker has a subscriber port of 5556 and a message port of 4445
    And Lance Broker can receive 1 message
    And Lance Broker is receiving 2 subscriptions
    And a subscriber is created with listening port 3333 with name "subscriberName"
    And a subscriber is created with listening port 3334 with name "subscriberName"
    When a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    And a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    Then 1 subscribers will be found for topic "topicName"

  Example: Subscribe and unsubscribe from one topic
  | topicName | subscriberName |
  | topic1    | subName        |

  Scenario: Multiple subscribers receive their respective messages
    Given Lance Broker has a subscriber port of 5556 and a message port of 4445
    And Lance Broker can receive 2 messages
    And Lance Broker is receiving 2 subscriptions
    And a subscriber is created with listening port 3333 with name "subscriberName1"
    And a subscriber registers for the topic "topicName1" with subscriber name "subscriberName1"
    And a subscriber is created with listening port 3334 with name "subscriberName2"
    And a subscriber registers for the topic "topicName2" with subscriber name "subscriberName2"
    And a udp message is created with data "message1" and topic "topicName1"
    And a udp message is created with data "message2" and topic "topicName2"
    And 1 subscriber will be found for topic "topicName1"
    And 1 subscriber will be found for topic "topicName2"
    When a publisher sends the message "message1" to Lance Broker 1 time
    And a publisher sends the message "message2" to Lance Broker 1 time
    Then the subscriber with name "subscriberName1" receives the message "message1" 1 time
    And the subscriber with name "subscriberName2" receives the message "message2" 1 time

  Example: Two subscribers receive their respective messages
  | topicName1 | subscriberName1 | message1       | topicName2 | subscriberName2 | message2       |
  | topic1     | subName1        | test1 message1 | topic2     | subName2        | test2 message2 |

  Scenario A subscriber can receive multiple messages
    Given Lance Broker has a subscriber port of 5556 and a message port of 4445
    And Lance Broker can receive 10 messages
    And Lance Broker is receiving 1 subscriptions
    And a subscriber is created with listening port 3333 with name "subscriberName"
    When a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    And 1 subscriber will be found for topic "topicName"
    And a udp message is created with data "message" and topic "topicName"
    And a publisher sends the message "message" to Lance Broker 10 times
    Then the subscriber with name "subscriberName" receives the message "message" 10 times

  Example: One subscriber receives multiple messages
  | topicName | subscriberName | message      |
  | topic1    | subName1       | test message |

  Scenario: Multiple subscribers receive same messages when subscribe to same topic
    Given Lance Broker has a subscriber port of 5556 and a message port of 4445
    And Lance Broker can receive 10 messages
    And Lance Broker is receiving 2 subscriptions
    And a subscriber is created with listening port 3333 with name "subscriberName1"
    And a subscriber registers for the topic "topicName1" with subscriber name "subscriberName1"
    And a subscriber is created with listening port 3334 with name "subscriberName2"
    And a subscriber registers for the topic "topicName1" with subscriber name "subscriberName2"
    And a udp message is created with data "message1" and topic "topicName1"
    And 2 subscribers will be found for topic "topicName1"
    When a publisher sends the message "message1" to Lance Broker 10 time
    Then the subscriber with name "subscriberName1" receives the message "message1" 10 times
    And the subscriber with name "subscriberName2" receives the message "message1" 10 time

  Example: Two subscribers receive their respective messages
  | topicName1 | subscriberName1 | message1       |
  | topic1     | subName1        | test1 message1 |


