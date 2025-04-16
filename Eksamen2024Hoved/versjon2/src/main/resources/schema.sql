CREATE TABLE IF NOT EXISTS BOOKSQL (
    isbn_id BIGSERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL CONSTRAINT title_format CHECK (title ~ '(?!\\s)(?!.*\\s{2})[A-Za-z.: -]{2,50}(?<!\\s)'),
    author VARCHAR(50) NOT NULL CONSTRAINT author_format CHECK (author ~ '(?!\\s)(?!.*\\s{2})[A-Za-z. -]{2,50}(?<!\\s)'),
    publishing_year SMALLINT NOT NULL CONSTRAINT valid_year CHECK (publishing_year >= 1000 AND publishing_year <= 2100),
    rating NUMERIC(5,2) NOT NULL CONSTRAINT valid_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    category VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
    );

CREATE INDEX IF NOT EXISTS idx_title ON BOOKSQL (title);
CREATE INDEX IF NOT EXISTS idx_category ON BOOKSQL (category);
CREATE INDEX IF NOT EXISTS idx_publishingyear on BOOKSQL (publishing_year);
CREATE INDEX IF NOT EXISTS idx_category_year on BOOKSQL (category, publishing_year);

CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS '
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Slett triggern hvis den eksisterer
DROP TRIGGER IF EXISTS booksql_updateat ON BOOKSQL;

-- Opprett triggern
CREATE TRIGGER booksql_updateat
    BEFORE UPDATE ON BOOKSQL
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();




/*
NUMERIC(5,2) betyr at man kan lagre opptil 5 sifre før komma og 2 etter komma.
(?!\s)             # Negativ lookahead: tittel kan ikke starte med mellomrom
(?!.*\s{2})        # Negativ lookahead: ingen forekomst av to eller flere mellomrom på rad
[A-Za-z.\- ]{2,50} # Tillatte tegn: bokstaver, punktum, bindestrek og mellomrom. Lengde 2 til 50 tegn
(?<!\s)            # Negativ lookbehind: tittel kan ikke slutte med mellomrom
 */

-- Table: users
CREATE TABLE USERSDB (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    phone VARCHAR(8) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_firstname ON USERSDB (first_name);
CREATE INDEX IF NOT EXISTS idx_lastname ON USERSDB (last_name);
CREATE INDEX IF NOT EXISTS idx_email on USERSDB (email);

CREATE OR REPLACE FUNCTION update_timestamp_Users()
RETURNS TRIGGER AS '
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
' LANGUAGE plpgsql;

-- Slett triggern hvis den eksisterer, for å sikre at den er kompatibel
-- med den nye funksjonsdefinisjonen.
DROP TRIGGER IF EXISTS USERSDB_updateAt ON USERSDB;

-- Opprett triggern
CREATE TRIGGER USERSDB_updateAt
    BEFORE UPDATE ON USERSDB
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp_Users();