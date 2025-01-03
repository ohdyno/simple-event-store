Feature: Validate an Append Token for the Stream

  Background:
    Given the event source system is setup for testing

  Rule: Validating an append token for a stream that does not exist fails

    Example: Validating an append token for a stream that does not exist fails
      Given a stream name for a stream that does not exist
      And any append token
      When validating the append token for the stream
      Then it fails because the stream does not exist
