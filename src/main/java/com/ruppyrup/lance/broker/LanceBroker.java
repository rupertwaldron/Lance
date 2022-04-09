package com.ruppyrup.lance.broker;

import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public class LanceBroker implements Broker {

  private static LanceBroker lanceBrokerInstance;
  private Transceiver transceiver;
  private final Map<Topic, Queue<Message>> receivedMessages = new HashMap<>();
  private final Map<Topic, Subscriber> subscribers = new HashMap<>();

  private LanceBroker() {
  }

  public static LanceBroker getInstance() {
    if (null == lanceBrokerInstance) {
      lanceBrokerInstance = new LanceBroker();
    }
    return lanceBrokerInstance;
  }

  @Override
  public void receive() {
    Message message = transceiver.receive();

    Topic topic = message.getTopic();

    if (!receivedMessages.containsKey(topic)) {
      Queue<Message> topicMessages = receivedMessages.computeIfAbsent(topic,
          k -> new LinkedList<>());
      topicMessages.add(message);
    } else {
      receivedMessages.get(topic).add(message);
    }
  }

  @Override
  public void send() {
    for(var entry : receivedMessages.entrySet()) {
      while(!entry.getValue().isEmpty()) {
        transceiver.send(entry.getValue().poll());
      }
    }
  }

  @Override
  public void register(Topic topic, Subscriber subscriber) {
    subscribers.put(topic, subscriber);
  }

  @Override
  public void setTransceiver(Transceiver transceiver) {
    this.transceiver = transceiver;
  }

  @Override
  public Optional<Message> getNextMessageForTopic(Topic topic) {
    return Optional.ofNullable(receivedMessages.get(topic).poll());
  }

  @Override
  public Map<Topic, Subscriber> getSubscribers() {
    return subscribers;
  }
}
