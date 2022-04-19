package com.ruppyrup.lance.subscriber;

import static com.ruppyrup.lance.models.MessageUtils.getMessageBytes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.DataMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriberInfo;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.scheduler.Schedulers;

public class LanceSubscriber {
  private static final Logger LOGGER = Logger.getLogger(LanceSubscriber.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int lanceSubPort = 4446;
  private final int receivePort;
  private final DatagramSocket socket;
  private final InetAddress address;
  private boolean isRunning;
  public AtomicInteger counter = new AtomicInteger(0);

  public LanceSubscriber(int receivePort, DatagramSocket socket, InetAddress address) {
    this.receivePort = receivePort;
    this.socket = socket;
    this.address = address;
    this.isRunning = true;
  }

  public LanceSubscriber(int receivePort) throws SocketException, UnknownHostException {
    this.receivePort = receivePort;
    socket = new DatagramSocket(receivePort);
    address = InetAddress.getLocalHost();
    this.isRunning = true;
  }

  public void subscribe(String subscriberName, Topic topic) {
    SubscriberInfo subscriberInfo = new LanceSubscriberInfo(subscriberName, receivePort);
    Message message = new DataMessage(topic, subscriberInfo.toJsonString());
    try {
      byte[] dataToSend = getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address,
          lanceSubPort);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
  }

  public void unsubscribe(String subscriberName, Topic topic) {
    subscribe(subscriberName, topic);
  }

  public Message receive() {
    byte[] buffer = new byte[1024];
    Message receivedMessage = null;
    try {
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, receivePort);
      socket.receive(packet);
      byte[] receivedBytes = new byte[packet.getLength()];
      System.arraycopy(packet.getData(), 0, receivedBytes, 0, packet.getLength());
      receivedMessage = mapper.readValue(receivedBytes, DataMessage.class);
      LOGGER.info("Lance Subscribe received -> %s" + receivedMessage);
    } catch (IOException e) {
      LOGGER.warning("Error receiving datagram :: " + e.getMessage());
    }
    return receivedMessage;
  }

  public Flux<Message> createUdpFlux() {
    return Flux.create(this::emit, OverflowStrategy.BUFFER)
        .publishOn(Schedulers.boundedElastic());
  }

  private void emit(FluxSink<Message> emitter) {
    while (isRunning) {
      emitter.next(receive());
    }
  }

  private void handleMessage(Message message) {
    System.out.println(counter.incrementAndGet());
    System.out.println(
        Thread.currentThread().getName() + " -> Received topic :: " + message.getTopic());
    System.out.println(
        Thread.currentThread().getName() + " -> Received message :: " + message.getContents());
  }

  public void stop() {
    isRunning = false;
    socket.close();
  }

  public static void main(String[] args) throws SocketException, UnknownHostException {
    LanceSubscriber subscriber = new LanceSubscriber(6161);
    Flux<Message> udpFlux = subscriber.createUdpFlux();
    subscriber.subscribe("rubsub", new Topic("monkey-topic"));

    udpFlux.subscribe(
        subscriber::handleMessage,
        err -> System.out.println("Error: " + err.getMessage()),
        () -> {
          System.out.println("Done!");
          subscriber.stop();
        });
  }
}
