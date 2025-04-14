BEGIN;
INSERT INTO users (first_name, last_name, dob, phone, email) VALUES
    ('Ola', 'Nordmann', '1990-01-01', '12345678', 'ola.nordmann@example.com'), -- Ola 1
    ('Kari', 'Nordmann', '1985-05-15', '23456789', 'kari.nordmann@example.com'), -- Kari 1
    ('Per', 'Hansen', '1992-11-22', '34567890', 'per.hansen@example.com'),
    ('Lise', 'Johansen', '1980-07-10', '45678901', 'lise.johansen@example.com'),

    ('Victoria', 'Ottesen', '2000-01-19', '44556677', 'victoria.ottesen@example.com'),
    ('Magnus', 'Gregersen', '1971-10-01', '55667788', 'magnus.gregersen@example.com'),
    ('Natalie', 'Knudsen', '1983-06-24', '66778899', 'natalie.knudsen@example.com'),
    ('Filip', 'Eriksen', '1997-03-03', '77889900', 'filip.eriksen@example.com'),
    ('Isabella', 'Berg', '1979-09-10', '88990011', 'isabella.berg@example.com'),

    ('Ola', 'Jensen', '1995-08-20', '98765432', 'ola.jensen@example.com'),  -- Ola 2 (samme fornavn som Ola 1, men annen email)
    ('Kari', 'Pettersen', '1978-02-10', '87654321', 'kari.iettersen@example.com'), -- Kari 2 (samme fornavn som Kari 1, men annen email)
    ('Thea', 'Solberg', '1981-07-23', '00112233', 'thea.solberg@example.com');


INSERT INTO BOOKSQL (title, author, publishing_year, rating, category) VALUES
('The Sun and Her Flowers', 'Rupi Kaur', 2017, 4.5, 'Poetry'),
('The Sun and flower', 'Rupi Kaur', 2017, 4.5, 'Poetry'),
('To Kill a Mockingbird', 'Harper Lee', 1960, 4.2, 'Fiction'),
('Milk and Honey', 'Rupi Kaur', 2014, 4.3, 'Poetry'),
('Citizen: An American Lyric', 'Claudia Rankine', 2014, 4.2, 'Poetry'),
('Dearly', 'Margaret Atwood', 2020, 4.1, 'Poetry'),
('Pride and Prejudice', 'Jane Austen', 1813, 4.3, 'Romance'),
('A Thousand Mornings', 'Mary Oliver', 2012, 4.4, 'Poetry'),
('The Lord of the Rings', 'J.R.R. Tolkien', 1954, 4.6, 'Fantasy'),
('Devotions', 'Mary Oliver', 2017, 4.6, 'Poetry'),
('The Carrying', 'Ada Limón', 2000, 4.5, 'Poetry'),
('War and Peace', 'Leo Tolstoy', 1869, 4.4, 'Historical Fiction'),
('Bright Dead Things', 'Ada Limón', 2015, 4.4, 'Poetry'),
('Magical Negro', 'Morgan Parker', 1999, 4.3, 'Poetry'),
('The Catcher in the Rye', 'J.D. Salinger', 1951, 4.0, 'Fiction'),
('When My Brother Was an Aztec', 'Natalie Diaz', 2012, 4.4, 'Poetry'),
('American Sonnets for My Past and Future Assassin', 'Terrance Hayes', 2018, 4.2, 'Poetry'),
('Hamlet', 'William Shakespeare', 1603, 4.7, 'Tragedy'),
('Macbeth', 'William Shakespeare', 1606, 4.6, 'Tragedy');
COMMIT;




