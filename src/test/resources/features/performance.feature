
@Performance
Feature: Publish messages receivecd from multiple subscribers at a high rate

  Scenario: Publish messages from multiple publishers to a single subscriber
    Given the lance broker is running
    And a subscriber is created with listening port 3333 with name "subscriberName" on subPort 4446
    And a subscriber registers for the topic "topicName" with subscriber name "subscriberName"
    And a udp message is created with data "message" and topic "topicName"
    When 1 publishers send the message "message" to Lance Broker 4000 times
    Then the subscriber with name "subscriberName" receives the message "message" 4000 times in 4000 mSeconds

    Example: One subscriber receives one message
      | topicName | subscriberName | message      | messageCount |
      | topic1    | subName1       | test message | 5000         |

