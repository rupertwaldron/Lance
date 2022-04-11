package com.ruppyrup.lance.transceivers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class UdpTransceiver implements Transceiver {

  private static final Logger LOGGER = Logger.getLogger(UdpTransceiver.class.getName());
  private final DatagramSocket socket;
  private final InetAddress address;
  private final int port;
  private final ObjectMapper mapper = new ObjectMapper();

  public UdpTransceiver(DatagramSocket socket, InetAddress address, int port) {
    this.socket = socket;
    this.address = address;
    this.port = port;
  }

  @Override
  public void send(Message message, List<Subscriber> subscribers) {
    byte[] messageBytes = getMessageBytes(message);
    subscribers.forEach(subscriber -> {
      try {
        socket.send(new DatagramPacket(messageBytes, messageBytes.length, address, subscriber.getPort()));
      } catch (IOException e) {
        LOGGER.warning("Error sending packing for message :: " + message);
      }
    });
  }

  @Override
  public Optional<Message> receive() {
    byte[] buffer = new byte[58];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
    Message receivedMessage = null;
    try {
      socket.receive(packet);
      byte[] receivedBytes = new byte[packet.getLength()];
      LOGGER.info("Received packet -> " + Arrays.toString(receivedBytes));
      System.arraycopy(packet.getData(), 0, receivedBytes, 0, packet.getLength());
      receivedMessage = mapper.readValue(receivedBytes, LanceMessage.class);
    } catch (IOException e) {
      LOGGER.warning("Error receiving datagram");
    }
    return Optional.ofNullable(receivedMessage);
  }

  private byte[] getMessageBytes(Message message) {
    try {
      return mapper.writeValueAsBytes(message);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }
}
