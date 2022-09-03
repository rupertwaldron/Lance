package com.ruppyrup.lance.subscriber;

import static com.ruppyrup.lance.models.MessageUtils.getMessageBytes;
import static com.ruppyrup.lance.utils.LanceLogger.LOGGER;

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
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.FluxSink.OverflowStrategy;
import reactor.core.scheduler.Schedulers;

public class LanceSubscriber implements Subscriber {

  //  private static final Logger LOGGER = Logger.getLogger(LanceSubscriber.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private final int lanceSubPort;
  private int receivePort;
  private DatagramSocket socket;
  private final InetAddress address;
  private volatile boolean isRunning;
  public AtomicInteger counter = new AtomicInteger(0);
  private final Queue<DatagramPacket> packets = new LinkedList<>();

  private volatile int count = 1;
  private CompletableFuture<Void> receivePacketFuture;

  public LanceSubscriber(int receivePort, DatagramSocket socket, InetAddress address,
      int lanceSubPort) {
    this.receivePort = receivePort;
    this.socket = socket;
    this.address = address;
    this.lanceSubPort = lanceSubPort;
    this.isRunning = true;
  }

  public LanceSubscriber(int receivePort, int lanceSubPort) throws UnknownHostException {
    this.receivePort = receivePort;
    address = InetAddress.getLocalHost();
    this.lanceSubPort = lanceSubPort;
    this.isRunning = true;
  }

  public LanceSubscriber(int lanceSubPort) throws UnknownHostException {
    this(getReceiveRandomPort(), lanceSubPort);
  }

  private static int getReceiveRandomPort() {
    return 4096 + new Random().nextInt(61439);
  }

  @Override
  public void start() {
    while (socket == null) {
      try {
        socket = new DatagramSocket(receivePort);
      } catch (SocketException e) {
        receivePort = getReceiveRandomPort();
        LOGGER.info("Port throws error - trying another");
      }
    }

    receivePacketFuture = CompletableFuture.runAsync(this::receivePacket);
  }

  private void receivePacket() {
    while (isRunning) {
      byte[] buffer = new byte[1024];
      try {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, receivePort);
        socket.receive(packet);
        packets.offer(packet);
//        LOGGER.info(count++ + " Lance Subscribe received and added to queue -> " + packet);
      } catch (IOException e) {
        LOGGER.warning("Error receiving datagram :: " + e.getMessage());
      }
    }
  }

  @Override
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

  @Override
  public void unsubscribe(String subscriberName, Topic topic) {
    subscribe(subscriberName, topic);
  }

  @Override
  public Message receive() {
    Message receivedMessage = null;
    try {
      while (packets.isEmpty() && isRunning) {
        Thread.sleep(100);
      }

      DatagramPacket packetFromQueue = packets.poll();
      if (packetFromQueue == null) {
        LOGGER.warning("Packet from queue in subscriber is empty :: " + packets.size());
        return null;
      }
      byte[] receivedBytes = new byte[packetFromQueue.getLength()];
      System.arraycopy(packetFromQueue.getData(), 0, receivedBytes, 0,
          packetFromQueue.getLength());
      receivedMessage = mapper.readValue(receivedBytes, DataMessage.class);
      LOGGER.info(count++ + " Lance Subscribe received -> " + receivedMessage);

    } catch (Exception e) {
      LOGGER.warning("Error reading receive queue :: " + e.getMessage());
    }
    return receivedMessage;
  }

  @Override
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

  @Override
  public void close() {
    System.out.println(this.getClass().getSimpleName() + " closed");
    isRunning = false;
    try {
      receivePacketFuture.get(1, TimeUnit.SECONDS);
    } catch (Exception e) {

    } finally {
      if (socket != null) {
        socket.close();
      }
    }
  }

  @Override
  public int getReceivePort() {
    return receivePort;
  }
  //  public static void main(String[] args) throws SocketException, UnknownHostException {
//    LanceSubscriber subscriber = new LanceSubscriber(6161);
//    Flux<Message> udpFlux = subscriber.createUdpFlux();
//    subscriber.subscribe("rubsub", new Topic("monkey-topic"));
//
//    udpFlux.subscribe(
//        subscriber::handleMessage,
//        err -> System.out.println("Error: " + err.getMessage()),
//        () -> {
//          System.out.println("Done!");
//          subscriber.close();
//        });
//  }
}
