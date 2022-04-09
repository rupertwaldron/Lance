package com.ruppyrup.lance.broker;


import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.transceivers.Transceiver;
import java.util.Optional;

public interface Broker {

  void receive();
  void send();
  void setTransceiver(Transceiver transceiver);
  Optional<Message> getNextMessageForTopic(Topic topic);
}
