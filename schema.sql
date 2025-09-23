-- This script creates the database schema for the language-service application.

-- The UUID extension is required for generating UUID primary keys.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: sessions
-- Description: Stores information about each language learning session.

CREATE TABLE sessions (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
user_id UUID NOT NULL,
language VARCHAR(50) NOT NULL,
started_at TIMESTAMP WITH TIME ZONE NOT NULL,
completed BOOLEAN NOT NULL DEFAULT FALSE,
completed_at TIMESTAMP WITH TIME ZONE
);

-- Index on user_id to speed up queries for a user's sessions.
CREATE INDEX idx_sessions_user_id ON sessions (user_id);

-- Table: chat_messages
-- Description: Stores chat messages between the user and the AI.

CREATE TABLE chat_messages (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
session_id UUID NOT NULL,
user_id UUID NOT NULL,
content TEXT NOT NULL,
role VARCHAR(20) NOT NULL,
timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
CONSTRAINT fk_chat_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

-- Index on session_id to optimize queries for a specific session's chat history.
CREATE INDEX idx_chat_messages_session_id ON chat_messages (session_id);

-- Table: user_interactions
-- Description: Stores individual word interactions within a session.

CREATE TABLE user_interactions (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
session_id UUID NOT NULL,
user_id UUID NOT NULL,
word VARCHAR(255) NOT NULL,
is_correct BOOLEAN NOT NULL,
CONSTRAINT fk_interaction_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
);

-- Index on session_id to speed up queries for a session's interactions.
CREATE INDEX idx_user_interactions_session_id ON user_interactions (session_id);

-- Table: user_words
-- Description: Tracks a user's words for spaced repetition.

CREATE TABLE user_words (
id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
user_id UUID NOT NULL,
word VARCHAR(255) NOT NULL,
box INTEGER NOT NULL DEFAULT 1,
review_date TIMESTAMP WITH TIME ZONE NOT NULL,
-- Unique constraint to ensure a user has only one entry for each word.
CONSTRAINT uc_user_word UNIQUE (user_id, word)
);

-- Index on the unique key to enforce uniqueness and speed up lookups.
CREATE INDEX idx_user_words_user_id_word ON user_words (user_id, word);
