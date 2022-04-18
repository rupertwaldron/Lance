package com.ruppyrup.lance.subscriber;

import static com.ruppyrup.lance.models.MessageUtils.getMessageBytes;

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

public final class LanceSubscribe {

  //todo need unsubscribe method to unsubscribe and close the socket

  private static final Logger LOGGER = Logger.getLogger(LanceSubscribe.class.getName());
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int lanceSubPort = 4446;
  private final int receivePort;
  private final ExecutorService service;
  private final DatagramSocket socket;
  private final InetAddress address;
  public AtomicInteger counter = new AtomicInteger(0);

  public LanceSubscribe(int receivePort) throws SocketException, UnknownHostException {
    service = Executors.newFixedThreadPool(20);
    this.receivePort = receivePort;
    socket = new DatagramSocket(receivePort);
    address = InetAddress.getLocalHost();
  }


  public void subscribe(String subscriberName, Topic topic) {
    Subscriber subscriber = new LanceSubscriber(subscriberName, receivePort);
    Message message = new DataMessage(topic, subscriber.toJsonString());
    try {
      byte[] dataToSend = getMessageBytes(message);
      DatagramPacket packet = new DatagramPacket(dataToSend, dataToSend.length, address, lanceSubPort);
      socket.send(packet);
    } catch (Exception ex) {
      LOGGER.warning("Publish has failed ... \n" + ex.getMessage());
    }
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
        .publishOn(Schedulers.fromExecutorService(service));
  }

  private void emit(FluxSink<Message> emitter) {
    while (true) {
      emitter.next(receive());
    }
  }

  private void handleMessage(Message message) {
    System.out.println(counter.incrementAndGet());
    System.out.println(Thread.currentThread().getName() + " -> Received topic :: " + message.getTopic());
    System.out.println(Thread.currentThread().getName() + " -> Received message :: " + message.getContents());
  }



  public static void main(String[] args) throws SocketException, UnknownHostException {
    LanceSubscribe subscriber = new LanceSubscribe( 6161);
    Flux<Message> udpFlux = subscriber.createUdpFlux();
    subscriber.subscribe("rubsub", new Topic("monkey-topic"));

//    while (true) {
//      subscriber.receive().ifPresent(LanceSubscribe::handleMessage);
//    }



        udpFlux.subscribe(
            subscriber::handleMessage,
            err -> System.out.println("Error: " + err.getMessage()),
            () -> System.out.println("Done!"));

  }



}
