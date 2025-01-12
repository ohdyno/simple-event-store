Feature: Retrieve Events from System

  Background:
    Given the event source system is setup for testing

  Rule: Retrieved events are in the order of when they happened

    Example: Retrieving from a system with a single stream
      Given a stream named "one"
      And it has many events that are not in chronological order
      When retrieving events from the system
      Then the events are retrieved in the same order as when they happened

    Example: Retrieving from a system with multiple streams
      Given a stream named "one"
      And it already has many events
      Given a stream named "two"
      And it already has many events
      When retrieving events from the system
      Then the events are retrieved in the same order as when they happened

  Rule: Events can be retrieved by their type

    Example: Retrieving one event type
      Given a stream named "one"
      And it has type "A" events
      Given a stream named "two"
      And it has type "B" events
      Given we want type "A" events
      When retrieving events from the system
      Then both event types are retrieved

    Example: Retrieving multiple event type
      Given a stream named "one"
      And it has type "A" events
      Given a stream named "two"
      And it has type "B" events
      And it has type "C" events
      Given we want type "A" events
      And we want type "B" events
      When retrieving events from the system
      Then both event types are retrieved

  Rule: Retrieving events from an empty system returns no events

    Example: Retrieving from an empty system
      Given there are no events in the system
      When retrieving events from the system
      Then no events are retrieved
