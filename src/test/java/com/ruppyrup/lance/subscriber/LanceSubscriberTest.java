package com.ruppyrup.lance.subscriber;

import static org.junit.jupiter.api.Assertions.*;

import com.ruppyrup.lance.models.Topic;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LanceSubscriberTest {
  private LanceSubscriber subscriber;
  private MockSocket mockSocket;
  private InetAddress inetAddress;

  @BeforeEach
  void setUp() throws SocketException {
    mockSocket = new MockSocket();
    inetAddress = InetAddress.getLoopbackAddress();
    subscriber = new LanceSubscriber(2222, mockSocket, inetAddress);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void subscribe() {
    String subscriberName = "SubName1";
    Topic topic = new Topic("topic1");
    subscriber.subscribe(subscriberName, topic);

  }

  @Test
  void receive() {
  }

  @Test
  void createUdpFlux() {
  }
}

class MockSocket extends DatagramSocket {
  public DatagramPacket packet;

  public MockSocket() throws SocketException {
  }

  @Override
  public void send(DatagramPacket packet) {
    this.packet = packet;
  }
}
