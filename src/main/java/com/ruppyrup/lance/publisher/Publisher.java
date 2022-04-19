package com.ruppyrup.lance.publisher;

import com.ruppyrup.lance.models.Message;

public interface Publisher {

  void publish(Message message);

  void closeSocket();
}
