package com.ruppyrup.lance;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.MsgTransceiver;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LanceApplication implements Closeable {

  private ScheduledExecutorService service;
  private CompletableFuture<Void> subscriberFuture;
  private CompletableFuture<Void> receiverFuture;

  public void start() throws SocketException, UnknownHostException {
    System.out.println("Starting main....");
    Broker broker = LanceBroker.getInstance();
    DatagramSocket socket = new DatagramSocket(4445);
    Transceiver transceiver = new MsgTransceiver(socket, InetAddress.getLocalHost(), 4445);
    broker.setMsgTransceiver(transceiver);
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446),
        InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);

    service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(() -> LanceBroker.getInstance().send(), 0, 1000, TimeUnit.MILLISECONDS);
    subscriberFuture = CompletableFuture.runAsync(
        () -> {
          while (LanceBroker.getInstance().isRunning())
            LanceBroker.getInstance().register();
        });
    receiverFuture = CompletableFuture.runAsync(
        () -> {
          while (LanceBroker.getInstance().isRunning())
            LanceBroker.getInstance().receive();
        });
  }

  public static void main(String[] args)
      throws SocketException, UnknownHostException, InterruptedException {

    LanceApplication app = new LanceApplication();
    app.start();
    Thread.sleep(360000);
    app.close();

  }

  @Override
  public void close() {
    LanceBroker.getInstance().close();
    service.shutdownNow();
    subscriberFuture.join();
    receiverFuture.join();
  }
}
