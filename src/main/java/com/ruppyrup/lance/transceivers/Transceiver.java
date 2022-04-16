package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.util.List;
import java.util.Optional;

public interface Transceiver {

  void send(Message message, List<Subscriber> subscribes);
  Optional<Message> receive();

  void close();
}
