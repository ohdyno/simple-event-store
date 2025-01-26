Feature: Retrieve the Append Token for a Stream

  Background:
    Given the event source system is setup for testing

  Rule: The stream must exist to successfully retrieve the append token

    Example: Retrieving an append token for a stream that does not exist fails
      Given a stream name for a stream that does not exist
      When retrieving the append token for the stream
      Then it fails because the stream does not exist
