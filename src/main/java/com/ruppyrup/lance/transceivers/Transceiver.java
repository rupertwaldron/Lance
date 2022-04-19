package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.Closeable;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import java.util.List;
import java.util.Optional;

public interface Transceiver extends Closeable {

  void send(Message message, List<SubscriberInfo> subscribes);
  Optional<Message> receive();
}
