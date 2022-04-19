package com.ruppyrup.lance.publisher;

import com.ruppyrup.lance.Closeable;
import com.ruppyrup.lance.models.Message;

public interface Publisher extends Closeable {

  void publish(Message message);
}
