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

public class LanceApplication {

  public static void main(String[] args)
      throws SocketException, UnknownHostException, InterruptedException {
    System.out.println("Starting main....");
    Broker broker = LanceBroker.getInstance();
    DatagramSocket socket = new DatagramSocket(4445);
    Transceiver transceiver = new MsgTransceiver(socket, InetAddress.getLocalHost(), 4445);
    broker.setMsgTransceiver(transceiver);
    Transceiver subTransceiver = new MsgTransceiver(new DatagramSocket(4446),
        InetAddress.getLocalHost(), 4446);
    LanceBroker.getInstance().setSubTransceiver(subTransceiver);


    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    service.scheduleAtFixedRate(() -> LanceBroker.getInstance().send(), 5, 3, TimeUnit.MILLISECONDS);
    CompletableFuture<Void> subscriberFuture = CompletableFuture.runAsync(
        () -> {
          while (true)
            LanceBroker.getInstance().register();
        });
    CompletableFuture<Void> receiverFuture = CompletableFuture.runAsync(
        () -> {
          while (true)
            LanceBroker.getInstance().receive();
        });

    Thread.sleep(120000);

    LanceBroker.getInstance().closeSockets();
    service.shutdownNow();
    subscriberFuture.join();
    receiverFuture.join();


  }
}
