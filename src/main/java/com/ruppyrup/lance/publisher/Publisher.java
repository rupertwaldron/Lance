package com.ruppyrup.lance.publisher;

import com.ruppyrup.lance.Closeable;
import com.ruppyrup.lance.models.Message;
import java.net.SocketException;

public interface Publisher extends Closeable {

  void start() throws SocketException;

  void publish(Message message);
}
