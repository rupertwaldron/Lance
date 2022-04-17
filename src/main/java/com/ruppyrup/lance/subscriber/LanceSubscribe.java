package com.ruppyrup.lance.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriber;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Optional;
import java.util.logging.Logger;

public final class LanceSubscribe {

  private static final Logger LOGGER = Logger.getLogger(LanceSubscribe.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int lanceSubPort = 4446;
  private static final String LOCALHOST = "localhost";
  private final int receivePort;

  public LanceSubscribe(int receivePort) {
    this.receivePort = receivePort;
  }

  public void subscribe(String subscriberName, Topic topic) {
    Subscriber subscriber = new LanceSubscriber(subscriberName, receivePort);
    Message message = new DataMessage(topic, subscriber.toJsonString());
    try (DatagramSocket socket = new DatagramSocket()) {
      InetAddress address = InetAddress.getByName(LOCALHOST);
      byte[] dataToSend = getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, lanceSubPort);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
  }

  public Optional<Message> receive() {
    byte[] buffer = new byte[1024];
    Message receivedMessage = null;
    try (DatagramSocket socket = new DatagramSocket(receivePort)) {
      InetAddress address = InetAddress.getByName(LOCALHOST);
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, receivePort);
      socket.receive(packet);
      byte[] receivedBytes = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), 0, receivedBytes, 0, packet.getLength());
      receivedMessage = mapper.readValue(receivedBytes, DataMessage.class);
      LOGGER.info("Lance Subscribe received -> %s" + receivedMessage);
    } catch (IOException e) {
      LOGGER.warning("Error receiving datagram :: " + e.getMessage());
    }
    return Optional.ofNullable(receivedMessage);
  }

  private static byte[] getMessageBytes(Message message) {
    try {
      return mapper.writeValueAsBytes(message);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }

//  private void emit(FluxSink<byte[]> emitter) {
//    int count = 0;
//    byte[] buf = new byte[256];
//
//    while (true) {
//      DatagramPacket packet
//          = new DatagramPacket(buf, buf.length);
//      try {
//        socket.receive(packet);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//
//      String received
//          = new String(packet.getData(), 0, packet.getLength());
//      System.out.println("Emitting :: " + received);
//      emitter.next(packet.getData());
//    }

  public static void main(String[] args) {
    LanceSubscribe subscriber = new LanceSubscribe( 6161);
    subscriber.subscribe("rubsub", new Topic("monkey-topic"));
    while (true) {
      subscriber.receive();
    }
  }

}
