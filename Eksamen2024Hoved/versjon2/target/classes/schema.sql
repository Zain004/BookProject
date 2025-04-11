CREATE TABLE IF NOT EXISTS BOOKSQL (
    isbn_id BIGSERIAL PRIMARY KEY, -- Anbefalt Always
    title VARCHAR(50) NOT NULL CONSTRAINT title_format CHECK (title ~ '(?!\\s)(?!.*\\s{2})[A-Za-z.: -]{2,50}(?<!\\s)'),
    author VARCHAR(50) NOT NULL CONSTRAINT author_format CHECK (author ~ '(?!\\s)(?!.*\\s{2})[A-Za-z. -]{2,50}(?<!\\s)'),
    publishing_year SMALLINT NOT NULL CONSTRAINT valid_year CHECK (publishing_year >= 1000 AND publishing_year <= 2100),
    rating NUMERIC(5,2) NOT NULL CONSTRAINT valid_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE Default current_timestamp
    );

CREATE INDEX IF NOT EXISTS idx_title ON BOOKSQL (title);
CREATE INDEX IF NOT EXISTS idx_category ON BOOKSQL (category);
CREATE INDEX IF NOT EXISTS idx_publishingyear on BOOKSQL (publishing_year);
CREATE INDEX IF NOT EXISTS idx_category_year on BOOKSQL (category, publishing_year);

CREATE TRIGGER update_booksql_updated_at
    BEFORE UPDATE
    ON BOOKSQL
    FOR EACH ROW
    CALL "com.example.versjon2.UpdateTimestampTrigger";

/*
NUMERIC(5,2) betyr at man kan lagre opptil 5 sifre før komma og 2 etter komma.
(?!\s)             # Negativ lookahead: tittel kan ikke starte med mellomrom
(?!.*\s{2})        # Negativ lookahead: ingen forekomst av to eller flere mellomrom på rad
[A-Za-z.\- ]{2,50} # Tillatte tegn: bokstaver, punktum, bindestrek og mellomrom. Lengde 2 til 50 tegn
(?<!\s)            # Negativ lookbehind: tittel kan ikke slutte med mellomrom
 */