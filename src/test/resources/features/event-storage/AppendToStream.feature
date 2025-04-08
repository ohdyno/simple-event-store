Feature: Append To Stream
  Every event in the system is associated with an event stream. Because events are immutable (no update or delete is allowed),
  appending to a stream is the only mutation method. To handle concurrent append operations to the event stream, a "version"
  must be provided for the operation. The version can thought of as a pointer to the end of the stream. At the time of the operation,
  if the version is still pointing at the end of the stream, then the operation is successful. Otherwise, an appropriate failure
  is raised.

  Rule: Create a new stream if the version indicates so.

    Example: How to create a new stream

    Example: How to fail creating a stream due to duplicate stream names

  Rule: Version must reflect the current state of the event stream.

    Example: How to append to a stream with an up-to-date version

    Example: How to fail appending to a stream with an out-of-date version

    Example: How to fail appending to a stream if the stream does not exist
