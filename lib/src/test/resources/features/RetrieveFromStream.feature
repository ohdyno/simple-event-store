Feature: Retrieve Events from A Stream

  Background:
    Given the event source system is setup for testing

  Rule: Events retrieved from a stream are ordered by their version

  Rule: The stream must exist in order to retrieve from the stream

    Example: Retrieving from a stream that does not exist
      Given a stream name for a stream that does not exist
      When retrieving events from the stream
      Then it fails because the stream does not exist

  Rule: Events can be retrieved by type

    Example: Successfully retrieving a single type of event

    Example: Successfully retrieving multiple types of events

    Example: Retrieving an event type that does not exist returns no events
