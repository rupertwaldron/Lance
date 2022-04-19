package com.ruppyrup.lance.subscribers;

public interface SubscriberInfo {

  String getSubscriberName();

  int getPort();

  String toJsonString();
}
