package com.ruppyrup.lance.broker;


import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.List;
import java.util.Optional;


public interface Broker {
  void receive();
  void send();
  void register();

  void closeSockets();

  void setSubTransceiver(Transceiver subTransceiver);

  void setMsgTransceiver(Transceiver msgTransceiver);
  Optional<Message> getNextMessageForTopic(Topic topic);
  List<SubscriberInfo> getSubscribersByTopic(Topic topic);
}
