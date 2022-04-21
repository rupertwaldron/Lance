package com.ruppyrup.lance.integration;

import com.ruppyrup.lance.LanceApplication;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.publisher.Publisher;
import com.ruppyrup.lance.subscriber.Subscriber;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class LanceConfigTest {

  @Autowired
  private ApplicationContext appContext;

  @Autowired
  Publisher publisher;

  @Autowired
  Subscriber subscriber;

  private static final LanceApplication application = new LanceApplication();

  @BeforeAll
  static void setUp() throws SocketException, UnknownHostException {
    application.start();
  }

  @AfterAll
  static void tearDown() {
    application.close();
  }

  @Test
  void checkBeans() {
    Assertions.assertThat(appContext.containsBean("publisher")).isTrue();
    Assertions.assertThat(appContext.containsBean("subscriber")).isTrue();
  }

  @Test
  void publisher() {
    Assertions.assertThat(publisher).isNotNull();
  }

  @Test
  void subscriber() {
    Assertions.assertThat(subscriber).isNotNull();
  }

  @Test
  void publishAMessage() throws InterruptedException {
    Topic topic = new Topic("cart");
    Message message = new DataMessage(topic, "Info from topic cart");
    System.out.println("Test is publishing message :: " + message);
    subscriber.subscribe("sub1", topic);
    publisher.publish(message);
    Message receivedMessage = subscriber.receive();
    Assertions.assertThat(receivedMessage).isEqualTo(message);
    publisher.close();
    subscriber.close();
  }
}
