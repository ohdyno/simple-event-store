Feature: Create an Event Stream

  Background:
    Given the event source system is setup for testing

  Rule: The event stream must have at least one event

    Example: Creating a new stream successfully
      Given an event
      And when the event occurred
      And a new stream name
      When creating a stream with this information
      Then the new stream exists in the system
      And the stream contains only the event
      And the stream captures when the event occurred

  Rule: The event stream name must be unique

    Example: Creating two streams with the same name fails

  Rule: The same event can exist in multiple streams

    Example: Creating two different streams with the same event successfully

      Given an event
      And when the event occurred
      And the event already exists in another stream
      Given a new stream name
      When creating a stream with this information
      Then the new stream exists in the system
      And the stream contains only the event
      And the stream captures when the event occurred

  Rule: Creating a stream successfully returns an append token
