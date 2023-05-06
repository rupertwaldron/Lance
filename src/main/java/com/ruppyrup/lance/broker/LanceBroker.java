package com.ruppyrup.lance.broker;

import static com.ruppyrup.lance.utils.LanceLogger.LOGGER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriberInfo;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

public class LanceBroker implements Broker {

  // todo need a de-register method

  private static final ObjectMapper mapper = new ObjectMapper();
  private static LanceBroker lanceBrokerInstance;
  private Transceiver msgTransceiver;
  private Transceiver subTransceiver;
  private volatile boolean stopped = false;

  private final Map<Topic, Queue<Message>> receivedMessages = new ConcurrentHashMap<>();

  private final Map<Topic, List<SubscriberInfo>> subscribers = new ConcurrentHashMap<>();

  Semaphore full = new Semaphore(0);
  Semaphore empty = new Semaphore(1);

  private volatile int count = 1;
  private volatile int pubCount = 1;

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
    if (optionalMessage.isEmpty()) {
      return;
    }

    Message message = optionalMessage.get();
    LOGGER.info(pubCount++ + " Message received from publisher :: " + message.getContents());
    Topic topic = message.getTopic();

    try {
      empty.acquire();
    } catch (InterruptedException e) {
      LOGGER.warning("receiver could not acquire lock empty");
    }

    if (receivedMessages.containsKey(topic)) {
      receivedMessages.get(topic).add(message);
    } else {
      Queue<Message> topicMessages = new LinkedList<>();
      topicMessages.add(message);
      receivedMessages.put(topic, topicMessages);
    }

    full.release(); // release the lock to say there is a message
  }

  @Override
  public void send() {
    try {
      full.acquire();
    } catch (InterruptedException e) {
      LOGGER.warning("sender could not acquire full lock");
    }
    for (var entry : receivedMessages.entrySet()) {
      while (!entry.getValue().isEmpty()) {
        Message message = entry.getValue().peek();
        List<SubscriberInfo> subList = subscribers.get(message.getTopic());
        if (subList == null) {
          break;
        }
        LOGGER.info(count++ + " Sending message to following subscribers :: " + subList);
        msgTransceiver.send(message, subList);
        entry.getValue().poll();
      }
    }
    empty.release();
  }

  @Override
  public void register() {
    Optional<Message> optionalMessage = subTransceiver.receive();
    if (optionalMessage.isEmpty()) {
      return;
    }

    Message message = optionalMessage.get();
    String stringSubscriber = message.getContents();
    LOGGER.info("Registered :: " + stringSubscriber);
    Topic topic = message.getTopic();

    SubscriberInfo subscriberInfo;
    try {
      subscriberInfo = mapper.readValue(stringSubscriber, LanceSubscriberInfo.class);
    } catch (JsonProcessingException e) {
      LOGGER.warning("Can't convert string to subscriber" + e.getMessage());
      return;
    }

    if (subscribers.containsKey(topic)) {
      List<SubscriberInfo> prevRegisterSubscribersWithSameName = subscribers.get(topic).stream()
          .filter(sub -> sub.getSubscriberName().equals(subscriberInfo.getSubscriberName()))
          .toList();

      prevRegisterSubscribersWithSameName.forEach(removeSub -> subscribers.get(topic).remove(removeSub));

      subscribers.get(topic).add(subscriberInfo);
    } else {
      List<SubscriberInfo> subscribeList = new ArrayList<>();
      subscribeList.add(subscriberInfo);
      subscribers.put(topic, subscribeList);
    }
  }

  private boolean alreadyRegisteredThenDeRegister(Topic topic, SubscriberInfo subscriberInfo) {
    return subscribers.get(topic).remove(subscriberInfo);
  }

  @Override
  public void close()  {
    if (subTransceiver != null) {
      subTransceiver.close();
    }
    if (msgTransceiver != null) {
      msgTransceiver.close();
    }
    stopped = true;
    while(!receivedMessages.isEmpty()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
//    receivedMessages.clear();
    subscribers.clear();
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
  public List<SubscriberInfo> getSubscribersByTopic(Topic topic) {
    return subscribers.get(topic);
  }

  public void listTopics() {
    subscribers.forEach((key, value) -> {
      System.out.println(key);
      value.forEach(System.out::println);
    });
  }
}
