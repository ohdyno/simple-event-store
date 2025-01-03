Feature: Append to Stream

  Background:
    Given the event source system is setup for testing

  Rule: Appending to a stream requires a valid append token for that stream

  Rule: Appending to a stream successfully returns a new append token

  Rule: Creating a stream successfully returns an append token

    Example: Creating a stream successfully returns an append token
      Given an event
      And when the event occurred
      And a new stream name
      When creating a stream with this information
      Then a valid append token for the stream is returned
