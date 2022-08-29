
@Performance
Feature: Publish messages receivecd from multiple subscribers at a high rate

  Scenario: Publish messages from multiple publishers to a single subscriber
    Given the lance broker is running
    And a subscriber is created with listening port 3333 with name "subscriberName"
    And a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    And a udp message is created with data "message" and topic "topicName"
    When 100 publishers send the message "message" to Lance Broker 40 times
    Then the subscriber with name "subscriberName" receives the message "message" 3500 times

    Example: One subscriber receives one message
      | topicName | subscriberName | message      |
      | topic1    | subName1       | test message |

