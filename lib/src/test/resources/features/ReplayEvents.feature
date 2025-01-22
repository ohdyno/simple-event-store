Feature: Replay Events

  Background: Setup the event source
    Given the event source system is setup for testing
    And it has a stream "one" with 5 events
    And it has a stream "two" with 10 events

  Scenario: An observer receives events replayed from a stream
    Given an observer that observes all events
    And the stream "one"
    When events are replayed from the stream
    Then the observer receives all the events from the stream in the order

  Scenario: An observer receives events replayed from the system
    Given an observer that observes all events
    When events are replayed from the system
    Then the observer receives all the events from all streams

  Rule: An observer can define types of events it wants to receive

    Example: An observer defines one type of events to receive

    Example: An observer defines many types of events to receive

  Rule: An observer that receives events from a stream also receives a valid append token

  Rule: An observer that receives events from the system also receives a timestamp representing the system state
