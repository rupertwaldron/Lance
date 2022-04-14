package com.ruppyrup.lance.cucumber.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.Message;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public final class LanceSubscribe {

  private static final Logger LOGGER = Logger.getLogger(LanceSubscribe.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int port = 4446;

  public static void subscribe(Message message) {
    try (DatagramSocket socket = new DatagramSocket()) {
      InetAddress address = InetAddress.getByName("localhost");
      byte[] dataToSend = getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, port);
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

}
