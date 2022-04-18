package com.ruppyrup.lance.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriber;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LanceBroker implements Broker {

  // todo need a de-register method

  private static final Logger LOGGER = Logger.getLogger(LanceBroker.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static LanceBroker lanceBrokerInstance;
  private Transceiver msgTransceiver;
  private Transceiver subTransceiver;
  private boolean stopped = false;

  private final Map<Topic, Queue<Message>> receivedMessages = new ConcurrentHashMap<>();

  private final Map<Topic, List<Subscriber>> subscribers = new ConcurrentHashMap<>();

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
    Optional<Message> optionalMessage = msgTransceiver.receive();
    if (optionalMessage.isEmpty()) return;

    Message message = optionalMessage.get();
    LOGGER.info("Message received :: " + message.getContents());
    Topic topic = message.getTopic();

    if (receivedMessages.containsKey(topic)) {
      receivedMessages.get(topic).add(message);
    } else {
      Queue<Message> topicMessages = new LinkedList<>();
      topicMessages.add(message);
      receivedMessages.put(topic, topicMessages);
    }
  }

  @Override
  public void send() {
    for(var entry : receivedMessages.entrySet()) {
      while(!entry.getValue().isEmpty()) {
        Message message = entry.getValue().poll();
        List<Subscriber> subList = subscribers.get(message.getTopic());
        msgTransceiver.send(message, subList);
//        try {
//          TimeUnit.MILLISECONDS.sleep(1);
//        } catch (InterruptedException e) {
//          throw new RuntimeException(e);
//        }
      }
    }
  }

  @Override
  public void register() {
    Optional<Message> optionalMessage = subTransceiver.receive();
    if (optionalMessage.isEmpty()) return;

    Message message = optionalMessage.get();
    String stringSubscriber = message.getContents();
    LOGGER.info("Registered :: " + stringSubscriber);
    Topic topic = message.getTopic();

    Subscriber subscriber;
    try {
      subscriber = mapper.readValue(stringSubscriber, LanceSubscriber.class);
    } catch (JsonProcessingException e) {
      LOGGER.warning("Can't convert string to subscriber" + e.getMessage());
      return;
    }

    if (subscribers.containsKey(topic)) {
      subscribers.get(topic).add(subscriber);
    } else {
      List<Subscriber> subscribeList = new ArrayList<>();
      subscribeList.add(subscriber);
      subscribers.put(topic, subscribeList);
    }
  }

  @Override
  public void closeSockets() {
    if (subTransceiver != null) subTransceiver.close();
    if (msgTransceiver != null) msgTransceiver.close();
    stopped = true;
  }

  public boolean isRunning() {
    return !stopped;
  }

  @Override
  public void setSubTransceiver(Transceiver subTransceiver) {
    this.subTransceiver = subTransceiver;
  }

  @Override
  public void setMsgTransceiver(Transceiver msgTransceiver) {
    this.msgTransceiver = msgTransceiver;
  }

  @Override
  public Optional<Message> getNextMessageForTopic(Topic topic) {
    return Optional.ofNullable(receivedMessages.get(topic).poll());
  }

  @Override
  public List<Subscriber> getSubscribersByTopic(Topic topic) {
    return subscribers.get(topic);
  }
}
