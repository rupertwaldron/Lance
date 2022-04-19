package com.ruppyrup.lance.publisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.MessageUtils;
import com.ruppyrup.lance.models.Topic;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LancePublishTest {

  @Test
  void checkSocketAndAddressAreInjected() throws IOException {
    MockSocket socket = new MockSocket();
    InetAddress address = InetAddress.getLoopbackAddress();
    LancePublish publisher = new LancePublish(socket, address);
    Topic topic = new Topic("test1");
    Message message = new DataMessage(topic, "test1");
    byte[] dataToSend = MessageUtils.getMessageBytes(message);
    DatagramPacket expectedPacket = new DatagramPacket(dataToSend, dataToSend.length, address, 5555);

    publisher.publish(message);

    assertAll("Packet compare",
        () -> assertArrayEquals(expectedPacket.getData(), socket.packet.getData()),
        () -> assertEquals(expectedPacket.getLength(), socket.packet.getLength()),
        () -> assertEquals(expectedPacket.getAddress(), socket.packet.getAddress())
    );
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
