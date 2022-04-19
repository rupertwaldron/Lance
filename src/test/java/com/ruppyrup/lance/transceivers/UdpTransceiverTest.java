package com.ruppyrup.lance.transceivers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
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

  private MsgTransceiver msgTransceiver;
  private Message message;
  private MockDatagramSocket socket;
  private Topic topic;
  private byte[] buffer = new byte[1024];
  private List<SubscriberInfo> subscribes;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @BeforeEach
  void setUp() throws UnknownHostException, SocketException {
    socket = new MockDatagramSocket();
    msgTransceiver = new MsgTransceiver(socket, InetAddress.getLocalHost(), 6677);
    topic = new Topic("Topic-1");
    subscribes = List.of(new MockSubscribe(8899), new MockSubscribe(5678));
  }

  @Test
  void whenSendCalled_sendsMessagesToSubscribers() {
    message = new DataMessage(topic, "messageData");
    msgTransceiver.send(message, subscribes);
    assertEquals(2, socket.getSendCount());
  }

  @Test
  void whenSendCalled_sendsCorrectDatagramMessage() throws JsonProcessingException {
    message = new DataMessage(topic, "messageData");
    byte[] expectedData = objectMapper.writeValueAsBytes(message);
    msgTransceiver.send(message, subscribes);
    assertArrayEquals(expectedData, socket.getSendPacket().getData());
  }

  @Test
  void whenMessageSent_receiverGetsUpdatedData() throws JsonProcessingException {
    message = new DataMessage(topic, "messageData");
    msgTransceiver.send(message, subscribes);
    Message receivedMessage = msgTransceiver.receive().orElse(new DataMessage());
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

class MockSubscribe implements SubscriberInfo {
  private final int port;

  MockSubscribe(int port) {
    this.port = port;
  }

  @Override
  public String getSubscriberName() {
    return null;
  }

  @Override
  public int getPort() {
    return port;
  }

  @Override
  public String toJsonString() {
    return null;
  }
}
