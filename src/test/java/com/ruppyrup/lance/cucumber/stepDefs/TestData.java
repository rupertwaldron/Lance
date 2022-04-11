package com.ruppyrup.lance.cucumber.stepDefs;

import java.util.HashMap;
import java.util.Map;

public class TestData {
    private Map<String, Object> senarioData = new HashMap<>();

    public void setData(String key, Object value) {
        senarioData.put(key, value);
    }

    public <T> T getData(String key, Class<T> clazz) {
        return clazz.cast(senarioData.get(key));
    }
}
