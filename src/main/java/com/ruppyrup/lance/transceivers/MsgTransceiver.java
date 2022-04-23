package com.ruppyrup.lance.transceivers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class MsgTransceiver implements Transceiver {

  private static final Logger LOGGER = Logger.getLogger(MsgTransceiver.class.getName());
  private final DatagramSocket socket;
  private final InetAddress address;
  private final int port;
  private final ObjectMapper mapper = new ObjectMapper();

  public MsgTransceiver(DatagramSocket socket, InetAddress address, int port) {
    this.socket = socket;
    this.address = address;
    this.port = port;
  }

  @Override
  public void send(Message message, List<SubscriberInfo> subscribes) {
    if (subscribes == null) return;
    byte[] messageBytes = getMessageBytes(message);
    subscribes.forEach(subscriber -> {
      try {
        socket.send(new DatagramPacket(messageBytes, messageBytes.length, address, subscriber.getPort()));
      } catch (IOException e) {
        LOGGER.warning("Error sending packing for message :: " + message);
      }
    });
  }

  @Override
  public Optional<Message> receive() {
    byte[] buffer = new byte[1024];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
    Message receivedMessage = null;
    try {
      socket.receive(packet);
      byte[] receivedBytes = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), 0, receivedBytes, 0, packet.getLength());
      receivedMessage = mapper.readValue(receivedBytes, DataMessage.class);
    } catch (IOException e) {
      LOGGER.warning("Error receiving datagram :: " + e.getMessage());
    }
    return Optional.ofNullable(receivedMessage);
  }

  @Override
  public void close() {
    if (socket != null)
      socket.close();
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
