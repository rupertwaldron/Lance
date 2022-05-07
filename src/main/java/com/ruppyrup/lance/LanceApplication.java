package com.ruppyrup.lance;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.MsgTransceiver;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LanceApplication implements Closeable {

  private ScheduledExecutorService service;
  private CompletableFuture<Void> subscriberFuture;
  private CompletableFuture<Void> receiverFuture;
  private final ExecutorService executorService = Executors.newFixedThreadPool(3);

  public void start() throws SocketException, UnknownHostException {
    Broker broker = LanceBroker.getInstance();
    DatagramSocket socket = new DatagramSocket(4445);
    Transceiver transceiver = new MsgTransceiver(socket, InetAddress.getLocalHost(), 4445);
    broker.setMsgTransceiver(transceiver);
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446),
        InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);

    service = Executors.newSingleThreadScheduledExecutor();

    service.scheduleAtFixedRate(() -> LanceBroker.getInstance().send(), 0, 1000,
        TimeUnit.MILLISECONDS);
    subscriberFuture = CompletableFuture.runAsync(
        () -> {
          while (LanceBroker.getInstance().isRunning()) {
            LanceBroker.getInstance().register();
          }
        }, executorService);
    receiverFuture = CompletableFuture.runAsync(
        () -> {
          while (LanceBroker.getInstance().isRunning()) {
            LanceBroker.getInstance().receive();
          }
        }, executorService);
  }

  public static void main(String[] args)
      throws ExecutionException, InterruptedException, TimeoutException {
    Scanner scanner = new Scanner(System.in);
    LanceApplication app = new LanceApplication();

    CompletableFuture<Void> mainappcf = CompletableFuture.runAsync(() -> {
      try {
        app.start();
      } catch (SocketException | UnknownHostException e) {
        System.out.println("app start exception" + e.getMessage());
      }
    }, app.executorService);

    System.out.println("Enter q to quit");

    while (true) {
      String input = scanner.next();
      if (input.equals("q")) {
        break;
      }
    }

    mainappcf.get(1, TimeUnit.SECONDS);
    app.close();

  }

  @Override
  public void close() {
    System.out.println("Closing Lance");
    LanceBroker.getInstance().close();
    try {
      subscriberFuture.get(1, TimeUnit.SECONDS);
      receiverFuture.get(1, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      System.out.println("Failure during close" + e.getMessage());
    }
    executorService.shutdownNow();
    service.shutdownNow();
  }
}
