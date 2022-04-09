package com.ruppyrup.lance.broker;


import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.Map;
import java.util.Optional;


public interface Broker {
  void receive();
  void send();
  void register(Topic topic, Subscriber subscriber);
  void setTransceiver(Transceiver transceiver);
  Optional<Message> getNextMessageForTopic(Topic topic);
  Map<Topic, Subscriber> getSubscribers();
}
