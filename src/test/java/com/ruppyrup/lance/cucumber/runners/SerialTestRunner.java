package com.ruppyrup.lance.cucumber.runners;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        plugin = {"pretty", "html:target/cucumber-reports.html"},
        features = "classpath:features",
        glue = {"com.ruppyrup.lance.cucumber.stepDefs"}
)
public class SerialTestRunner {

}
