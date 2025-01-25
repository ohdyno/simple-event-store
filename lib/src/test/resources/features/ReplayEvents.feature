Feature: Replay Events

  Background: Setup the event source
    Given the event source system is setup for testing
    And it has a stream "one" with 5 events of type "A"
    And it has a stream "two" with 10 events of type "B"

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
      Given an observer that observes only events of type "A"
      When events are replayed from the system
      Then the observer receives only events of type "A"

    Example: An observer defines many types of events to receive
      Given an observer that observes events of type "A" and type "B"
      When events are replayed from the system
      Then the observer receives events of type "A"
      And the observer receives events of type "B"
      And the observer receives 15 events

  Rule: An observer that receives events from a stream also receives a valid append token

    Example: An observer receives events from a stream
      Given an observer
      And the stream "one"
      When events are replayed from the stream
      Then the observer receives an append token for the stream

  Rule: An observer that receives events from the system also receives a timestamp representing the system state

    Example: An observer receives events from the system
      Given an observer
      When events are replayed from the system
      Then the observer receives a timestamp
