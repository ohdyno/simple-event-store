Feature: Manipulating the Event Source Event Stream
  The event source is composed of multiple event streams.
  Each stream is identified by its name and is composed of multiple events.

  Background: 
    Given the event source is setup for testing

  Rule: A stream must have at least one event

    Example: Creating a new stream requires one event
      Given I have an event "FirstEvent"
      When I create a stream "ANewStream" with the event
      Then I am able to retrieve all the events for "ANewStream"
      And the only event I received is "FirstEvent"

    Example: Appending a new event to an existing stream
      Given I have an existing stream "ExistingStream" with the only event "FirstEvent"
      And I have a new event "SecondEvent"
      When I append the new event to the existing stream "ExistingStream"
      Then I am able to retrieve the following events for "ExistingStream"
        | FirstEvent  |
        | SecondEvent |

  Rule: When retrieved, the events are ordered chronologically based on when they occurred
