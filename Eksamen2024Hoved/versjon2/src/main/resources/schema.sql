CREATE TABLE IF NOT EXIST book {
    isbn_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, -- Anbefalt Always
    title VARCHAR(50) NOT NULL CHECK (title ~ '(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)')
    author VARCHAR(50) NOT NULL CHECK (author ~ '(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)')
    publishing_year INTEGER NOT NULL CHECK (publishing_year >= 1000 AND publishing_year <= 2100)
    rating 
    }

/*
(?!\s)             # Negativ lookahead: tittel kan ikke starte med mellomrom
(?!.*\s{2})        # Negativ lookahead: ingen forekomst av to eller flere mellomrom på rad
[A-Za-z.\- ]{2,50} # Tillatte tegn: bokstaver, punktum, bindestrek og mellomrom. Lengde 2 til 50 tegn
(?<!\s)            # Negativ lookbehind: tittel kan ikke slutte med mellomrom
 */