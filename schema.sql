-- Tailored schema for language-service based on current JPA entities
-- Postgres dialect

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Reference tables
CREATE TABLE IF NOT EXISTS languages (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(10) NOT NULL UNIQUE,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS boxes (
  id SERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE,
  interval_days INT NOT NULL
);

-- Words and related
CREATE TABLE IF NOT EXISTS words (
  id BIGSERIAL PRIMARY KEY,
  word VARCHAR(255) NOT NULL,
  language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE RESTRICT,
  level SMALLINT,
  CONSTRAINT uq_words_word_level UNIQUE (word, level)
);
CREATE INDEX IF NOT EXISTS idx_words_language ON words(language_id);
CREATE INDEX IF NOT EXISTS idx_words_word ON words(word);

CREATE TABLE IF NOT EXISTS word_forms (
  id BIGSERIAL PRIMARY KEY,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  type VARCHAR(50) NOT NULL,
  form VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_word_forms_word_id ON word_forms(word_id);
CREATE INDEX IF NOT EXISTS idx_word_forms_type ON word_forms(type);

CREATE TABLE IF NOT EXISTS word_conjugations (
  id BIGSERIAL PRIMARY KEY,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  person VARCHAR(50),
  form VARCHAR(255) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_word_conj_word_id ON word_conjugations(word_id);
CREATE INDEX IF NOT EXISTS idx_word_conj_form ON word_conjugations(form);

CREATE TABLE IF NOT EXISTS word_examples (
  id BIGSERIAL PRIMARY KEY,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  form_type VARCHAR(50),
  sentence TEXT
);
CREATE INDEX IF NOT EXISTS idx_word_examples_word_id ON word_examples(word_id);

CREATE TABLE IF NOT EXISTS word_tips (
  id BIGSERIAL PRIMARY KEY,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  tip TEXT
);
CREATE INDEX IF NOT EXISTS idx_word_tips_word_id ON word_tips(word_id);

CREATE TABLE IF NOT EXISTS word_relations (
  id BIGSERIAL PRIMARY KEY,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  related_word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  relation_type VARCHAR(50),
  CONSTRAINT chk_word_relation_not_self CHECK (word_id <> related_word_id)
);
CREATE INDEX IF NOT EXISTS idx_word_relations_word_id ON word_relations(word_id);
CREATE INDEX IF NOT EXISTS idx_word_relations_related_word_id ON word_relations(related_word_id);

CREATE TABLE IF NOT EXISTS word_translations (
  id BIGSERIAL PRIMARY KEY,
  source_word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  target_word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  source_language_id BIGINT REFERENCES languages(id) ON DELETE CASCADE,
  target_language_id BIGINT REFERENCES languages(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
  UNIQUE (source_word_id, target_language_id)
  CONSTRAINT uq_word_translations UNIQUE (source_word_id, target_word_id),
  CONSTRAINT chk_word_translation_not_self CHECK (source_word_id <> target_word_id)
);
CREATE INDEX IF NOT EXISTS idx_word_translations_source ON word_translations(source_word_id);
CREATE INDEX IF NOT EXISTS idx_word_translations_target ON word_translations(target_word_id);
CREATE INDEX idx_source_lang ON word_translations(source_language_id);
CREATE INDEX idx_target_lang ON word_translations(target_language_id);

-- Spaced repetition and user linkage
CREATE TABLE IF NOT EXISTS user_words (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  word_id BIGINT NOT NULL REFERENCES words(id) ON DELETE CASCADE,
  language_id BIGINT NOT NULL REFERENCES languages(id) ON DELETE RESTRICT,
  level SMALLINT,
  box_id INT REFERENCES boxes(id) ON DELETE SET NULL,
  last_seen TIMESTAMPTZ NOT NULL,
  accuracy DOUBLE PRECISION NOT NULL,
  created_at DOUBLE PRECISION NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_user_words_user ON user_words(user_id);
CREATE INDEX IF NOT EXISTS idx_user_words_word ON user_words(word_id);
CREATE INDEX IF NOT EXISTS idx_user_words_language ON user_words(language_id);
CREATE INDEX IF NOT EXISTS idx_user_words_box ON user_words(box_id);

-- Paragraphs and per-user paragraph state
CREATE TABLE IF NOT EXISTS paragraphs (
  id BIGSERIAL PRIMARY KEY,
  content TEXT NOT NULL,
  level SMALLINT,
  topic VARCHAR(255),
  length SMALLINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS user_paragraphs (
  id BIGSERIAL PRIMARY KEY,
  user_id UUID NOT NULL,
  paragraph_id BIGINT NOT NULL REFERENCES paragraphs(id) ON DELETE CASCADE,
  seen BOOLEAN NOT NULL DEFAULT FALSE,
  refreshed_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_user_paragraphs_user ON user_paragraphs(user_id);
CREATE INDEX IF NOT EXISTS idx_user_paragraphs_paragraph ON user_paragraphs(paragraph_id);

CREATE TABLE IF NOT EXISTS user_paragraph_words (
  user_paragraph_id BIGINT NOT NULL REFERENCES user_paragraphs(id) ON DELETE CASCADE,
  word VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_paragraph_id, word)
);

-- Sessions, chat messages, interactions, activities
CREATE TABLE IF NOT EXISTS sessions (
  id UUID PRIMARY KEY,
  user_id UUID NOT NULL,
  language VARCHAR(50) NOT NULL,
  started_at TIMESTAMPTZ NOT NULL,
  completed BOOLEAN NOT NULL,
  completed_at TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON sessions(user_id);

CREATE TABLE IF NOT EXISTS chat_messages (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  user_id UUID NOT NULL,
  content TEXT NOT NULL,
  role VARCHAR(20) NOT NULL,
  timestamp TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_chat_messages_session_id ON chat_messages(session_id);

CREATE TABLE IF NOT EXISTS user_interactions (
  id UUID PRIMARY KEY,
  session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE,
  user_id UUID NOT NULL,
  word VARCHAR(255) NOT NULL,
  is_correct BOOLEAN NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_user_interactions_session_id ON user_interactions(session_id);

CREATE TABLE IF NOT EXISTS user_activities (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  session_id UUID NOT NULL,
  user_id UUID NOT NULL,
  action_type VARCHAR(100) NOT NULL,
  action_details TEXT,
  timestamp TIMESTAMPTZ NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_user_activities_session_id ON user_activities(session_id);
