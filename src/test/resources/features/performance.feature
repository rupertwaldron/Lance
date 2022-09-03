@Performance
Feature: Publish messages receivecd from multiple subscribers at a high rate

  Scenario Outline: Publish messages from multiple publishers to a single subscriber
    Given the lance broker is running
    And a subscriber is created with listening port 3333 with name <subscriberName> on subPort 4446
    And a subscriber registers for the topic <topicName> with subscriber name <subscriberName>
    And a udp message is created with data <message> and topic <topicName>
    When <noPublishers> publishers send the message <message> to Lance Broker <messageCount> times
    Then the subscriber with name <subscriberName> receives the message <message> <totalMessages> times in <timeLimit> mSeconds

    Examples: One subscriber receives one message
      | topicName | subscriberName | message        | messageCount | totalMessages | timeLimit | noPublishers |
      | "topic1"  | "subName1"     | "test message" | 100          | 100           | 100       | 1            |
      | "topic1"  | "subName1"     | "test message" | 100          | 2000          | 2000     | 20           |
#      | "topic1"  | "subName1"     | "test message" | 100           | 10000          | 500       | 100          |
