Feature: Create an Event Stream

  Background:
    Given the event source system is setup for testing

  Scenario: Initializing a New Event Stream
    A stream cannot be empty. Therefore, a stream is always created with an event.

    Given a valid event of type A
    And the event occurred on 11/22/2023 12:34:56 PM UTC
    And a desired stream name of "stream one"
    When an attempt is made to create a new event stream with the desired stream name and the event
    Then the stream was successfully created
    And the stream contains the following events
      | Event Type | Occurred On                |
      | Type A     | 11/22/2023 12:34:56 PM UTC |

  Scenario: Attempting to Create an Existing Event Stream
