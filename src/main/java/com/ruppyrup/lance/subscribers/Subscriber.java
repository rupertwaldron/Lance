package com.ruppyrup.lance.subscribers;

public interface Subscriber {

  String getSubscriberName();

  int getPort();

  String toJsonString();
}
