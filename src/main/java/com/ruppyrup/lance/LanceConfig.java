package com.ruppyrup.lance;

import com.ruppyrup.lance.publisher.LancePublisher;
import com.ruppyrup.lance.publisher.Publisher;
import com.ruppyrup.lance.subscriber.LanceSubscriber;
import com.ruppyrup.lance.subscriber.Subscriber;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LanceConfig {

  @Bean(destroyMethod = "close")
//  @Scope("prototype")
  public Publisher publisher() throws SocketException, UnknownHostException {
    LancePublisher lancePublisher = new LancePublisher(4445);
    lancePublisher.start();
    return lancePublisher;
  }

  @Bean(destroyMethod = "close")
//  @Scope("prototype")
  public Subscriber subscriber() throws SocketException, UnknownHostException {
    LanceSubscriber lanceSubscriber = new LanceSubscriber(4446);
    lanceSubscriber.start();
    return lanceSubscriber;
  }
}
