package com.ruppyrup.lance.broker;

import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.logging.Logger;

public class LanceBroker implements Broker {

  private static final Logger LOGGER = Logger.getLogger(LanceBroker.class.getName());
  private static LanceBroker lanceBrokerInstance;
  private Transceiver transceiver;
  private final Map<Topic, Queue<Message>> receivedMessages = new HashMap<>();
  private final Map<Topic, List<Subscriber>> subscribers = new HashMap<>();

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
    Optional<Message> optionalMessage = transceiver.receive();
    if (optionalMessage.isEmpty()) return;


    Message message = optionalMessage.get();
    LOGGER.info("Message received :: " + message.getContents());
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
        Message message = entry.getValue().poll();
        List<Subscriber> subList = subscribers.get(message.getTopic());
        transceiver.send(message, subList);
      }
    }
  }

  @Override
  public void register(Topic topic, Subscriber subscriber) {
    if (subscribers.containsKey(topic)) {
      subscribers.get(topic).add(subscriber);
    } else {
      List<Subscriber> subscriberList = new ArrayList<>();
      subscriberList.add(subscriber);
      subscribers.put(topic, subscriberList);
    }

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
  public Map<Topic, List<Subscriber>> getSubscribers() {
    return subscribers;
  }
}
