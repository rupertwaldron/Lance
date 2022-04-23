package com.ruppyrup.lance.publisher;

import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.MessageUtils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class LancePublisher implements Publisher {

  private static final Logger LOGGER = Logger.getLogger(LancePublisher.class.getName());
  private static final int port = 4445;
  private DatagramSocket socket;
  private final InetAddress address;

  public LancePublisher() throws SocketException, UnknownHostException {
    this.address = InetAddress.getLocalHost();
  }

  public LancePublisher(DatagramSocket socket, InetAddress address) {
    this.socket = socket;
    this.address = address;
  }

  @Override
  public void start() throws SocketException {
    socket = new DatagramSocket();
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
  public void close() {
    if (socket != null)
      socket.close();
  }

//  public static void main(String[] args) throws SocketException, UnknownHostException {
//    LancePublisher publisher = new LancePublisher();
//    IntStream.range(0, 990)
//        .mapToObj(i -> "Hello from publisher on monkey-topic " + i)
//        .forEach(message -> publisher.publish(new DataMessage(new Topic("monkey-topic"), message)));
//    publisher.close();
//  }

}
