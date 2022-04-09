package com.ruppyrup.lance.models;

public interface Message {

  String getContents();

  void setContents(String contents);

  Topic getTopic();
}
