package com.ruppyrup.lance;

import com.ruppyrup.lance.publisher.LancePublisher;
import com.ruppyrup.lance.publisher.Publisher;
import com.ruppyrup.lance.subscriber.LanceSubscriber;
import com.ruppyrup.lance.subscriber.Subscriber;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LanceConfig {

  @Value("${lance.subscriber.port:4547}")
  private int subscriberPort;

  @Bean(destroyMethod = "close")
  public Publisher publisher() throws SocketException, UnknownHostException {
    return new LancePublisher();
  }

  @Bean(destroyMethod = "close")
  public Subscriber subscriber() throws SocketException, UnknownHostException {
    return new LanceSubscriber(subscriberPort);
  }

}
