package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.models.LanceMessage;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.subscribers.Subscriber;
import java.util.List;

public class UdpTransceiver implements Transceiver {

  @Override
  public void send(Message message, List<Subscriber> subscribers) {

  }

  @Override
  public LanceMessage receive() {
    return null;
  }
}
