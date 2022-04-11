package com.ruppyrup.lance.cucumber.stepDefs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TestData {

  private static final Map<String, Object> senarioData = new HashMap<>();

  static void setData(String key, Object value) {
    senarioData.put(key, value);
  }

  static <T> T getData(String key, Class<T> clazz) {
    return clazz.cast(senarioData.get(key));
  }

  public static void clear() {
    senarioData.values().stream()
        .filter(val -> val instanceof CompletableFuture<?>)
        .forEach(val -> ((CompletableFuture<?>) val).join());
    senarioData.clear();
  }
}
