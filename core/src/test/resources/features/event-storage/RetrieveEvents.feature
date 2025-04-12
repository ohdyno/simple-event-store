Feature: Retrieve Events
  Once events are appended to the event streams, they can be retrieved. There are different types of retrievals:
  - retrieving from a single stream
  - retrieving from multiple streams
  The differences are small but significant. When retrieving from a single stream, the version property reflecting the current
  state of the stream is also returned. This version, in turn, can be used to append another event to the event stream.
  On the other hand, when retrieving from multiple streams, version not longer makes sense to return since it is only associated
  with a single stream. Instead, two other properties are returned:
  - the last event's id
  - the last event's timestamp
  Like version, the last event's id reflects the current state of the system. The timestamp can be used to communicate to others
  roughly when the system was last updated.

  Scenario: Retrieve events from a single stream.

  Scenario: Retrieve all events from the system.

  Scenario: Retrieve events with a combination of restrictions.

  Rule: Events can be restricted to specific types.

  Rule: Events can be restricted to belong to specific streams.

  Rule: Events can be restricted to be within a range for their versions.

  Rule: Events can be restricted to be within a range for their ids.
