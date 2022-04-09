package com.ruppyrup.lance.transceivers;

import com.ruppyrup.lance.models.Message;

public interface Transceiver {

  void send(Message message);
  Message receive();

}
