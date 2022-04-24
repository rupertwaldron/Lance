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
import org.springframework.context.annotation.Scope;

@Configuration
public class LanceConfig {

  @Bean(destroyMethod = "close")
  @Scope("prototype")
  public Publisher publisher() throws SocketException, UnknownHostException {
    return new LancePublisher();
  }

  @Bean(destroyMethod = "close")
  @Scope("prototype")
  public Subscriber subscriber() throws SocketException, UnknownHostException {
    return new LanceSubscriber();
  }
}
