# Created by rupertwaldron at 11/04/2022
Feature: Publish and Subscribe Feature
  Check a class can publish to a topic sends data and then
  another class can receive the data for that topic

  Scenario: Can publish and receive data
    Given a udp message is created with data "test message" and topic "topic 1"
    When a publisher sends the message to Lance Broker
    Then Lance Broker will store the message under the correct topic

