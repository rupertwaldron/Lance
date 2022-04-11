package com.ruppyrup.lance;

import com.ruppyrup.lance.broker.Broker;
import com.ruppyrup.lance.broker.LanceBroker;
import com.ruppyrup.lance.transceivers.Transceiver;
import com.ruppyrup.lance.transceivers.UdpTransceiver;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class LanceApplication {

  public static void main(String[] args) throws SocketException, UnknownHostException {
    System.out.println("Starting main....");
    Broker broker = LanceBroker.getInstance();
    DatagramSocket socket = new DatagramSocket(4445);
    Transceiver transceiver = new UdpTransceiver(socket, InetAddress.getByName("localhost"), 4445);
    broker.setTransceiver(transceiver);
    broker.receive();
  }

}
