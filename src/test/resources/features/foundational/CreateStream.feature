Feature: Create Stream
  Each event in the store is associated with an event stream.

  Background:
    Given the event store is operational
    And it has no events

  Rule: A stream must have at least one event

    Example: Creating a new stream
      Given the stream name "one"
      And an event
      When creating the stream with the event
      Then a stream with the specified name is created
      And it contains the specified event

  Rule: Two streams cannot have the same name.
