package com.ruppyrup.lance.utils;

import java.util.logging.Logger;

public class LanceLogger {

  public static Logger LOGGER = null;

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format",
        "[%1$tF %1$tT %1$tL] [%4$-7s] %5$s %n");
    LOGGER = Logger.getLogger(LanceLogger.class.getName());
  }

}
