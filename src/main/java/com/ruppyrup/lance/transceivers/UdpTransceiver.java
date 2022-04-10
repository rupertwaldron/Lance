package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.List;

public class UdpTransceiver implements Transceiver {
  private final DatagramSocket socket;
  private InetAddress address;

  public UdpTransceiver(DatagramSocket socket, InetAddress address) {
    this.socket = socket;
    this.address = address;
  }

  @Override
  public void send(Message message, List<Subscriber> subscribers) {
    byte[] buffer = new byte[1024];
    subscribers.forEach(subscriber -> {
      try {
        socket.send(new DatagramPacket(buffer, 1024, address, subscriber.getPort()));
      } catch (IOException e) {
        throw new RuntimeException("Error sending packing for message :: " + message);
      }
    });
  }

  @Override
  public LanceMessage receive() {
    return null;
  }
}
