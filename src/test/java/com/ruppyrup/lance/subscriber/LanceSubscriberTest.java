package com.ruppyrup.lance.subscriber;

import static org.junit.jupiter.api.Assertions.*;

import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.MessageUtils;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriberInfo;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class LanceSubscriberTest {

  private LanceSubscriber subscriber;
  private MockSocket socket;
  private InetAddress address;
  private int receivePort;

  @BeforeEach
  void setUp() throws SocketException {
    socket = new MockSocket();
    address = InetAddress.getLoopbackAddress();
    receivePort = 4422;
    subscriber = new LanceSubscriber(receivePort, socket, address);
  }

  @AfterEach
  void tearDown() {
  }

  @Test
  void testSubscribeSendsCorrectMessage() {
    String subscriberName = "SubName1";
    SubscriberInfo subscriberInfo = new LanceSubscriberInfo(subscriberName, receivePort);
    Topic topic = new Topic("topic1");
    Message message = new DataMessage(topic, subscriberInfo.toJsonString());
    byte[] dataToSend = MessageUtils.getMessageBytes(message);
    DatagramPacket expectedPacket = new DatagramPacket(dataToSend, dataToSend.length, address,
        4446);

    subscriber.subscribe(subscriberName, topic);

    assertAll("Packet compare",
        () -> assertArrayEquals(expectedPacket.getData(), socket.packet.getData()),
        () -> assertEquals(expectedPacket.getLength(), socket.packet.getLength()),
        () -> assertEquals(expectedPacket.getAddress(), socket.packet.getAddress())
    );
  }

  @Test
  void testUnSubscribeSendsCorrectMessage() {
    String subscriberName = "SubName1";
    SubscriberInfo subscriberInfo = new LanceSubscriberInfo(subscriberName, receivePort);
    Topic topic = new Topic("topic1");
    Message message = new DataMessage(topic, subscriberInfo.toJsonString());
    byte[] dataToSend = MessageUtils.getMessageBytes(message);
    DatagramPacket expectedPacket = new DatagramPacket(dataToSend, dataToSend.length, address,
        4446);

    subscriber.unsubscribe(subscriberName, topic);

    assertAll("Packet compare",
        () -> assertArrayEquals(expectedPacket.getData(), socket.packet.getData()),
        () -> assertEquals(expectedPacket.getLength(), socket.packet.getLength()),
        () -> assertEquals(expectedPacket.getAddress(), socket.packet.getAddress())
    );
  }

  @Test
  void testReceiveReturnsMessage() {
    Message message = new DataMessage(new Topic("topic1"), "Hello");
    byte[] messageBytes = MessageUtils.getMessageBytes(message);
    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address,
        receivePort);
    socket.setPacket(packet);
    Message receivedMessage = subscriber.receive();
    assertEquals(message, receivedMessage);
  }

  @Test
  void testFluxOfMessagesIsCreated() {
    Message message = new DataMessage(new Topic("topic1"), "Hello");
    byte[] messageBytes = MessageUtils.getMessageBytes(message);
    DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, address,
        receivePort);
    socket.setPacket(packet);
    Flux<Message> udpFlux = subscriber.createUdpFlux();
    udpFlux.take(1).subscribe(mess -> assertEquals(message, mess),
        System.out::println,
        () -> subscriber.stop());
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

  @Override
  public void receive(DatagramPacket p) throws IOException {
    byte[] sendData = packet.getData();
    System.arraycopy(sendData, 0, p.getData(), 0, packet.getLength());
  }

  public void setPacket(DatagramPacket packet) {
    this.packet = packet;
  }
}
