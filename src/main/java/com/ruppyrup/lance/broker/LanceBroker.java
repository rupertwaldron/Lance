package com.ruppyrup.lance.broker;

import static com.ruppyrup.lance.utils.LanceLogger.LOGGER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruppyrup.lance.models.Message;
import com.ruppyrup.lance.models.Topic;
import com.ruppyrup.lance.subscribers.LanceSubscriberInfo;
import com.ruppyrup.lance.subscribers.SubscriberInfo;
import com.ruppyrup.lance.transceivers.Transceiver;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LanceBroker implements Broker {

    // todo need a de-register method

    private static final ObjectMapper mapper = new ObjectMapper();
    private static LanceBroker lanceBrokerInstance;
    private Transceiver msgTransceiver;
    private Transceiver subTransceiver;
    private volatile boolean stopped = false;

    private final Map<Topic, Queue<Message>> receivedMessages = new HashMap<>();

    private final Map<Topic, List<SubscriberInfo>> subscribers = new HashMap<>();

    Semaphore full = new Semaphore(0);
    Semaphore empty = new Semaphore(1);

    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    Lock readLock = reentrantReadWriteLock.readLock();
    Lock writeLock = reentrantReadWriteLock.writeLock();

    private volatile int count = 1;
    private volatile int pubCount = 1;
    private boolean remove;

    private LanceBroker() {
    }

    public static LanceBroker getInstance() {
        if (null == lanceBrokerInstance) {
            lanceBrokerInstance = new LanceBroker();
        }
        return lanceBrokerInstance;
    }

    @Override
    public void receive() {
        Optional<Message> optionalMessage = msgTransceiver.receive();
        if (optionalMessage.isEmpty()) {
            return;
        }

        Message message = optionalMessage.get();
        LOGGER.info(pubCount++ + " Message received from publisher :: " + message.getContents());
        Topic topic = message.getTopic();

        try {
            empty.acquire();
        } catch (InterruptedException e) {
            LOGGER.warning("receiver could not acquire lock empty");
        }

        if (receivedMessages.containsKey(topic)) {
            receivedMessages.get(topic).add(message);
        } else {
            Queue<Message> topicMessages = new LinkedList<>();
            topicMessages.add(message);
            receivedMessages.put(topic, topicMessages);
        }

        full.release(); // release the lock to say there is a message
    }

    @Override
    public void send() {
        try {
            full.acquire();
            for (var entry : receivedMessages.entrySet()) {
                while (!entry.getValue().isEmpty()) {
                    Message message = entry.getValue().peek();
                    readLock.lock();
                    try {
                        List<SubscriberInfo> subList = new ArrayList<>(subscribers.get(message.getTopic()));
                        LOGGER.info(count++ + " Sending message to following subscribers :: " + subList);
                        msgTransceiver.send(message, subList);
                        entry.getValue().poll();
                    } catch (NullPointerException npe) {
                        // ignore as no subscribers
                    } finally {
                        readLock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            LOGGER.warning("sender could not acquire full lock");
        } finally {
//      full.release(); // only here if no subscribers and messages left
            empty.release();
        }
    }

    @Override
    public void register() {
        Optional<Message> optionalMessage = subTransceiver.receive();
        if (optionalMessage.isEmpty()) {
            return;
        }

        Message message = optionalMessage.get();
        String stringSubscriber = message.getContents();
        LOGGER.info("Registered :: " + stringSubscriber);
        Topic topic = message.getTopic();

        SubscriberInfo subscriberInfo;
        try {
            subscriberInfo = mapper.readValue(stringSubscriber, LanceSubscriberInfo.class);
        } catch (JsonProcessingException e) {
            LOGGER.warning("Can't convert string to subscriber" + e.getMessage());
            return;
        }

        if (subscribers.containsKey(topic)) {
            readLock.lock();
            List<SubscriberInfo> prevRegisterSubscribersWithSameName;
            try {
                prevRegisterSubscribersWithSameName = subscribers.get(topic).stream()
                        .filter(sub -> sub.getSubscriberName().equals(subscriberInfo.getSubscriberName()))
                        .toList();
            } finally {
                readLock.unlock();
            }

            try {
                writeLock.lock();
                prevRegisterSubscribersWithSameName.forEach(removeSub -> subscribers.get(topic).remove(removeSub));

                subscribers.get(topic).add(subscriberInfo);
            } finally {
                writeLock.unlock();
            }
        } else {
            List<SubscriberInfo> subscribeList = new ArrayList<>();
            try {
                writeLock.lock();
                subscribeList.add(subscriberInfo);
                subscribers.put(topic, subscribeList);
            } finally {
                writeLock.unlock();
            }
        }
    }

    private boolean alreadyRegisteredThenDeRegister(Topic topic, SubscriberInfo subscriberInfo) {
        try {
            readLock.lock();
            remove = subscribers.get(topic).remove(subscriberInfo);
        } finally {
            writeLock.unlock();
        }
        return remove;
    }

    @Override
    public void close() {
        if (subTransceiver != null) {
            subTransceiver.close();
        }
        if (msgTransceiver != null) {
            msgTransceiver.close();
        }
        stopped = true;
        while (!receivedMessages.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        writeLock.lock();
        try {
            subscribers.clear();
        } finally {
            writeLock.unlock();
        }
    }

    public boolean isRunning() {
        return !stopped;
    }

    @Override
    public void clearMessages() {
        receivedMessages.clear();
    }

    @Override
    public void clearSubscribers() {
        subscribers.clear();
    }

    @Override
    public void setSubTransceiver(Transceiver subTransceiver) {
        this.subTransceiver = subTransceiver;
    }

    @Override
    public void setMsgTransceiver(Transceiver msgTransceiver) {
        this.msgTransceiver = msgTransceiver;
    }

    @Override
    public Optional<Message> getNextMessageForTopic(Topic topic) {
        return Optional.ofNullable(receivedMessages.get(topic).poll());
    }

    @Override
    public List<SubscriberInfo> getSubscribersByTopic(Topic topic) {
        readLock.lock();
        List<SubscriberInfo> subscriberInfos = null;
        try {
            subscriberInfos = new ArrayList<>(subscribers.get(topic));
        } catch (NullPointerException npe) {
            // ignore because no subscribers
        } finally {
            readLock.unlock();
        }
        return subscriberInfos;
    }

    public void listTopics() {
        readLock.lock();
        subscribers.forEach((key, value) -> {
            System.out.println(key);
            value.forEach(System.out::println);
        });
        readLock.unlock();
    }

    public void setFull(Semaphore full) {
        this.full = full;
    }

    public void setEmpty(Semaphore empty) {
        this.empty = empty;
    }
}
