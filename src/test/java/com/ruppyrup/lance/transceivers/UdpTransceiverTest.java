package com.ruppyrup.lance.transceivers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UdpTransceiverTest {

  private UdpTransceiver udpTransceiver;
  private Message message;
  private MockDatagramSocket socket;
  private Topic topic;
  private byte[] buffer = new byte[1024];
  private List<Subscriber> subscribers;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws UnknownHostException, SocketException {
    socket = new MockDatagramSocket();
    udpTransceiver = new UdpTransceiver(socket, InetAddress.getLocalHost(), 6677);
    topic = new Topic("Topic-1");
    subscribers = List.of(new MockSubscriber(8899), new MockSubscriber(5678));
  }

  @Test
  void whenSendCalled_sendsMessagesToSubscribers() {
    message = new LanceMessage(topic, "messageData");
    udpTransceiver.send(message, subscribers);
    assertEquals(2, socket.getSendCount());
  }

  @Test
  void whenSendCalled_sendsCorrectDatagramMessage() throws JsonProcessingException {
    message = new LanceMessage(topic, "messageData");
    byte[] expectedData = objectMapper.writeValueAsBytes(message);
    udpTransceiver.send(message, subscribers);
    assertArrayEquals(expectedData, socket.getSendPacket().getData());
  }

  @Test
  void whenMessageSent_receiverGetsUpdatedData() throws JsonProcessingException {
    message = new LanceMessage(topic, "messageData");
    udpTransceiver.send(message, subscribers);
    Message receivedMessage = udpTransceiver.receive().orElse(new LanceMessage());
    assertEquals(message, receivedMessage);
  }
}


class MockDatagramSocket extends DatagramSocket {
  private int sendCount;
  private DatagramPacket sendPacket;
  private int receiveCount;

  public MockDatagramSocket() throws SocketException {
  }

  @Override
  public void send(DatagramPacket p) throws IOException {
    sendCount++;
    sendPacket = p;
  }

  @Override
  public void receive(DatagramPacket p) throws IOException {
    receiveCount++;
    byte[] sendData = sendPacket.getData();
    System.arraycopy(sendData, 0, p.getData(), 0, sendPacket.getLength());
  }

  public int getSendCount() {
    return sendCount;
  }

  public int getReceiveCount() {
    return receiveCount;
  }

  public DatagramPacket getSendPacket() {
    return sendPacket;
  }

  @Override
  public int getPort() {
    return 6666;
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
