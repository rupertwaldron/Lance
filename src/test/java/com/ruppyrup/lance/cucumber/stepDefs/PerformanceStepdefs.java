package com.ruppyrup.lance.cucumber.stepDefs;

import com.ruppyrup.lance.LanceApplication;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.publisher.LancePublisher;
import com.ruppyrup.lance.subscriber.LanceSubscriber;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class PerformanceStepdefs {

  @Before("@Performance")
  public void setup() {
    CompletableFuture<Void> lanceBrokerFuture = CompletableFuture.runAsync(
        () -> LanceApplication.main(new String[]{}));
    TestData.setData("lanceBrokerFuture", lanceBrokerFuture);
  }

  @After("@Performance")
  public void teardown() {
    CompletableFuture<Void> lanceBrokerFuture = TestData.getData("lanceBrokerFuture",
        CompletableFuture.class);
    try {
      lanceBrokerFuture.get(2, TimeUnit.SECONDS);
    } catch (Exception e) {
      System.out.println("Lance Broker stopped");
    }
    TestData.setData("lanceBrokerFuture", null);
    TestData.clear();
  }

  @Given("the lance broker is running")
  public void theLanceBrokerIsRunning() {


  }

  @And("a subscriber is created with listening port {int} with name {string} on subPort {int}")
  public void aSubscriberIsCreatedWithListeningPort(int port, String subscriberName, int subPort)
      throws UnknownHostException {
//    int subPort = TestData.getData("subPort", Integer.class);
    LanceSubscriber lanceSubscriber = new LanceSubscriber(port, subPort);
    lanceSubscriber.start();
    TestData.setData(subscriberName, lanceSubscriber);
    TestData.setData(subscriberName + "Port", port);
  }

  @When("{int} publishers send the message {string} to Lance Broker {int} time(s)")
  public void aPublisherSendsTheMessageToLanceBroker(int publisherCount, String messageData, int messageCount)
      throws InterruptedException {

    Message message = TestData.getData(messageData, Message.class);

    CountDownLatch readyThreadCounter = new CountDownLatch(publisherCount);
    CountDownLatch callingThreadBlocker = new CountDownLatch(1);
    CountDownLatch completedThreadCounter = new CountDownLatch(publisherCount);
    ExecutorService executorService = Executors.newFixedThreadPool(publisherCount);

    List<CompletableFuture<Void>> completableFutures = Stream.generate(
            () -> CompletableFuture.runAsync(
                () -> {
                  try {
                    readyThreadCounter.countDown();
                    callingThreadBlocker.await();
                    System.out.println("Started ... " + Thread.currentThread().getName());
                    var publisher = new LancePublisher(4445);
                    publisher.start();
                    for (int i = 0; i < messageCount; i++) {
                      Thread.sleep(10);
                      publisher.publish(message);
                    }
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  } finally {
                    completedThreadCounter.countDown();
                  }
                }, executorService))
        .limit(publisherCount)
        .toList();

    readyThreadCounter.await();
    System.out.println("Runnables ready");
    callingThreadBlocker.countDown();
    completedThreadCounter.await();
    completableFutures.forEach(CompletableFuture::join);

    executorService.shutdown();
  }
}
