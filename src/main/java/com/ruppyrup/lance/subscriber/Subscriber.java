package com.ruppyrup.lance.subscriber;

import com.ruppyrup.lance.Closeable;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import reactor.core.publisher.Flux;

public interface Subscriber extends Closeable {

  void subscribe(String subscriberName, Topic topic);

  void unsubscribe(String subscriberName, Topic topic);

  Message receive();

  Flux<Message> createUdpFlux();

  @Override
  void close();
}
