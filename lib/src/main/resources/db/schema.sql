DROP
    SCHEMA IF EXISTS eventsource CASCADE;

CREATE
    SCHEMA eventsource;

CREATE
    TABLE
        eventsource.events(
            stream_name text NOT NULL,
            version INTEGER NOT NULL,
            event_id text NOT NULL,
            event_type text NOT NULL,
            event_data jsonb NOT NULL,
            occurred_on TIMESTAMP WITH TIME ZONE,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY(
                stream_name,
                version
            )
        );

CREATE
    VIEW eventsource.append_tokens AS SELECT
        stream_name,
        COUNT(*) AS append_token
    FROM
        eventsource.events
    GROUP BY
        stream_name;

CREATE
    INDEX index_event_type ON
    eventsource.events(event_type);

CREATE
    INDEX index_event_id ON
    eventsource.events(event_id);

CREATE
    INDEX index_stream_name ON
    eventsource.events(stream_name);

CREATE
    INDEX index_occurred_on ON
    eventsource.events(occurred_on);

CREATE
    INDEX index_created_at ON
    eventsource.events(created_at)
