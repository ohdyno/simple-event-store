Feature: Append to Stream

  Background:
    Given the event source system is setup for testing

  Rule: Appending to a stream requires a valid append token for that stream

    Example: Appending to a stream successfully
      Given a stream name
      And the stream already exists in the system
      And an event
      And when the event occurred
      And a valid append token for the stream
      When appending the event to the stream
      Then the stream contains the new event
      And the stream captures when the event occurred

    Example: Appending to a stream with an invalid append token fails
      Given a stream name
      And the stream already exists in the system
      And an event
      And when the event occurred
      And an invalid append token
      When appending the event to the stream
      Then it fails because the append token is invalid

  Rule: The Append Token is valid only for a single successful append operation

    Example: Appending to the stream twice with the same append token fails
      Given a stream name
      And the stream already exists in the system
      And an event
      And when the event occurred
      And a valid append token for the stream
      And the append token has been used to append to the stream
      When appending the event to the stream again with the same append token
      Then it fails because the append token is invalid

  Rule: Appending to a stream successfully returns a new append token

    Example: Appending to a stream successfully
      Given a stream name
      And the stream already exists in the system
      And an event
      And when the event occurred
      And a valid append token for the stream
      When appending the event to the stream
      Then a valid append token for the stream is returned

  Rule: Creating a stream successfully returns an append token

    Example: Creating a stream successfully returns an append token
      Given an event
      And when the event occurred
      And a new stream name
      When creating a stream with this information
      Then a valid append token for the stream is returned
