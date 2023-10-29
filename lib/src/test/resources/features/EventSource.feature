Feature: Manipulating the Event Source Event Stream

  The event source is composed of multiple event streams.
  Each stream is identified by its name and is composed of multiple events.

  Background:
    Given the event source is setup for testing

  Scenario: Creating A New Stream

  A stream must have at least one event.

    Given I have an event "FirstEvent"
    When I create a stream "ANewStream" with the event
    Then I am able to retrieve all the events for "ANewStream"
    And the only event I received is "FirstEvent"
