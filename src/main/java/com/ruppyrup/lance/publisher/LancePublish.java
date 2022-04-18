package com.ruppyrup.lance.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public final class LancePublish {

  private static final Logger LOGGER = Logger.getLogger(LancePublish.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int port = 4445;

  public void publish(Message message) {
    try (DatagramSocket socket = new DatagramSocket()) {
      InetAddress address = InetAddress.getByName("localhost");
      byte[] dataToSend = getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, port);
      LOGGER.info("Publisher sending message to Broker :: " + message);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
  }

  private static byte[] getMessageBytes(Message message) {
    try {
      return mapper.writeValueAsBytes(message);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return new byte[0];
  }

  public static void main(String[] args) throws InterruptedException {
    LancePublish publisher = new LancePublish();
    IntStream.range(0, 9990)
        .mapToObj(i -> "Hello from publisher on monkey-topic " + i)
        .forEach(message -> publisher.publish(new DataMessage(new Topic("monkey-topic"), message)));
  }

}
