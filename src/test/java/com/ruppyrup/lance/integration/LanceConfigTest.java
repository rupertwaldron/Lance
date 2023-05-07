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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = "lance.subscriber.port=4444")
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

  @BeforeEach
  void start() throws SocketException {
    subscriber.start();
    publisher.start();
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
  void publishAMessage() throws InterruptedException {
    Topic topic = new Topic("cart");
    Message message = new DataMessage(topic, "Info from topic cart");
    System.out.println("Test is publishing message :: " + message);
    subscriber.subscribe("sub1", topic);
    Thread.sleep(1000);
    publisher.publish(message);
    Message receivedMessage = subscriber.receive();
    Assertions.assertThat(receivedMessage).isEqualTo(message);
    publisher.close();
    subscriber.close();
  }
}
