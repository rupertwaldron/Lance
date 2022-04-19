package com.ruppyrup.lance.publisher;

import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.MessageUtils;
import com.ruppyrup.lance.models.Topic;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class LancePublisher implements Publisher {

  private static final Logger LOGGER = Logger.getLogger(LancePublisher.class.getName());
  private static final int port = 4445;
  private final DatagramSocket socket;
  private final InetAddress address;

  public LancePublisher() throws SocketException, UnknownHostException {
    this.socket = new DatagramSocket();
    this.address = InetAddress.getLocalHost();
  }

  public LancePublisher(DatagramSocket socket, InetAddress address) {
    this.socket = socket;
    this.address = address;
  }

  @Override
  public void publish(Message message) {
    try {
      byte[] dataToSend = MessageUtils.getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, port);
      LOGGER.info("Publisher sending message to Broker :: " + message);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
  }

  @Override
  public void closeSocket() {
    socket.close();
  }

  public static void main(String[] args) throws SocketException, UnknownHostException {
    LancePublisher publisher = new LancePublisher();
    IntStream.range(0, 9990)
        .mapToObj(i -> "Hello from publisher on monkey-topic " + i)
        .forEach(message -> publisher.publish(new DataMessage(new Topic("monkey-topic"), message)));
  }

}