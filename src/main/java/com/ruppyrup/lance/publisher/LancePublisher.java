package com.ruppyrup.lance.publisher;

import static com.ruppyrup.lance.utils.LanceLogger.LOGGER;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.MessageUtils;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class LancePublisher implements Publisher {

  private final int port;
  private DatagramSocket socket;
  private final InetAddress address;

  private static AtomicInteger counter = new AtomicInteger(1);

  public LancePublisher(int port) throws SocketException, UnknownHostException {
    this.port = port;
    this.address = InetAddress.getLocalHost();
  }

  public LancePublisher(int port, DatagramSocket socket, InetAddress address) {
    this.port = port;
    this.socket = socket;
    this.address = address;
  }

  @Override
  public void start() throws SocketException {
    if (socket == null) {
      socket = new DatagramSocket();
    }
  }
  @Override
  public void publish(Message message) {
    try {
      byte[] dataToSend = MessageUtils.getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, port);
      LOGGER.info(counter.getAndIncrement() + " Publisher sending message to Broker:: " + message);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
  }

  @Override
  public void close() {
    System.out.println(this.getClass().getSimpleName() + " closed");
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
