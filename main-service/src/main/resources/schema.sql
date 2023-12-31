CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
    );

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT pk_cat PRIMARY KEY (id),
    CONSTRAINT uq_category UNIQUE (name)
    );

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL,
    CONSTRAINT pk_location PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    annotation VARCHAR NOT NULL,
    cat_id BIGINT NOT NULL,
    created_on TIMESTAMP,
    description VARCHAR,
    event_date TIMESTAMP NOT NULL,
    initiator_id BIGINT NOT NULL,
    loc_id BIGINT NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit BIGINT,
    published_on TIMESTAMP,
    request_moderation BOOLEAN,
    state_event VARCHAR,
    title VARCHAR NOT NULL,
    CONSTRAINT pk_event PRIMARY KEY (id),
    CONSTRAINT fk_events_to_users FOREIGN KEY (initiator_id) REFERENCES users(id),
    CONSTRAINT fk_events_to_categories FOREIGN KEY (cat_id) REFERENCES categories(id),
    CONSTRAINT fk_events_to_locations FOREIGN KEY (loc_id) REFERENCES locations(id)
    );

CREATE TABLE IF NOT EXISTS requests (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    created TIMESTAMP,
    event_id BIGINT,
    requester_id BIGINT,
    status VARCHAR,
    CONSTRAINT pk_request PRIMARY KEY (id),
    CONSTRAINT uq_request UNIQUE (event_id, requester_id),
    CONSTRAINT fk_requests_to_events FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fk_requests_to_users FOREIGN KEY (requester_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    pinned BOOLEAN NOT NULL,
    title VARCHAR NOT NULL,
    CONSTRAINT pk_compilation PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS compilations_events (
    compilation_id BIGINT,
    event_id BIGINT,
    CONSTRAINT pk_compilations_event PRIMARY KEY (compilation_id, event_id),
    CONSTRAINT fk_compilations_events_to_compilations FOREIGN KEY (compilation_id) REFERENCES compilations(id),
    CONSTRAINT fk_compilations_events_to_events FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS comments (
    id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    author_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    text VARCHAR(5000) NOT NULL,
    created TIMESTAMP,
    CONSTRAINT fk_comments_to_users FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_comments_to_events FOREIGN KEY (event_id) REFERENCES events(id)
);

