package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UdpTransceiverTest {

  private UdpTransceiver udpTransceiver;
  private Message message;
  private MockDatagramSocket socket;
  private Topic topic;
  private byte[] buffer = new byte[1024];
  private List<Subscriber> subscribers;

  @BeforeEach
  void setUp() throws UnknownHostException, SocketException {
    socket = new MockDatagramSocket();
    udpTransceiver = new UdpTransceiver(socket, InetAddress.getLocalHost());
    topic = new Topic("Topic-1");
    subscribers = List.of(new MockSubscriber(1234), new MockSubscriber(5678));
  }

  @Test
  void whenSendCalled_sendsMessagesToSubscribers() {
    message = new LanceMessage(topic, "messageData");
    udpTransceiver.send(message, subscribers);
    Assertions.assertEquals(2, socket.getSendCount());
  }

  @Test
  void receive() {
  }
}


class MockDatagramSocket extends DatagramSocket {
  private int sendCount;

  public MockDatagramSocket() throws SocketException {
  }

  @Override
  public void send(DatagramPacket p) throws IOException {
    sendCount++;
  }

  @Override
  public void receive(DatagramPacket p) throws IOException {

  }

  public int getSendCount() {
    return sendCount;
  }
}

class MockSubscriber implements Subscriber {
  private final int port;

  MockSubscriber(int port) {
    this.port = port;
  }

  @Override
  public int getPort() {
    return port;
  }
}
