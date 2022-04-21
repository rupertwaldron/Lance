package com.ruppyrup.lance;

import com.ruppyrup.lance.publisher.LancePublisher;
import com.ruppyrup.lance.publisher.Publisher;
import com.ruppyrup.lance.subscriber.LanceSubscriber;
import com.ruppyrup.lance.subscriber.Subscriber;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LanceConfig {

  @Bean
  public Publisher publisher() throws SocketException, UnknownHostException {
    return new LancePublisher();
  }

  @Bean
  public Subscriber subscriber() throws SocketException, UnknownHostException {
    return new LanceSubscriber(6667);
  }

}
