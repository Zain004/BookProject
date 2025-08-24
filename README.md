# Bibliotekssystem – Spring Boot Prosjekt

Dette er et bibliotekssystem utviklet i Java med Spring Boot og JPA. Prosjektet inkluderer session-basert brukerinnlogging, CRUD-operasjoner og ekstra funksjonalitet som bok-sletting og statistikk.

---

## Komme i gang

Før du kan teste systemet, må du logge inn:

1. Åpne følgende fil i nettleseren:  
   [Login-side](https://github.com/Zain004/BookProject/blob/main/Eksamen2024Hoved/versjon2/src/main/resources/static/Authentication_simple/loginPage.html)

2. Logg inn med følgende bruker:  
   - **Brukernavn:** `test.user-1`  
   - **Passord:** `Test123!`  

> Innlogging er session-basert, så enkelte funksjoner krever at du er logget inn.

---

## Test funksjonalitet

Etter innlogging kan du navigere manuelt i `static`-mappen for å teste de ulike sidene og funksjonene:  

- **CRUD-operasjoner** for bøker og brukere  
- **Slett bøker** utgitt etter år 2000 (krever innlogging)  
- **Statistikkfunksjon** som returnerer brukerdata via `HttpServletRequest`  

---

## Teknologier brukt

- Java  
- Spring Boot  
- JPA/Hibernate  
- MySQL (eller H2 hvis du bruker in-memory database)  
- HTML/CSS/JavaScript for frontend  

---

## Merknader

- Dette er et første prosjekt med fokus på Spring Boot og JPA.  
- Systemet er ment for testing og læring.  
- Vennligst logg inn først før du tester funksjonalitetene.

# Bibliotekssystem – Spring Boot Prosjekt

Dette er et bibliotekssystem utviklet i Java med Spring Boot og JPA. Prosjektet inkluderer session-basert brukerinnlogging, CRUD-operasjoner og ekstra funksjonalitet som bok-sletting og statistikk.

---

## Komme i gang

Før du kan teste systemet, må du logge inn:

1. Åpne følgende fil i nettleseren:  
   [Login-side](https://github.com/Zain004/BookProject/blob/main/Eksamen2024Hoved/versjon2/src/main/resources/static/Authentication_simple/loginPage.html)

2. Logg inn med følgende bruker:  
   - **Brukernavn:** `test.user-1`  
   - **Passord:** `Test123!`  

> Innlogging er session-basert, så enkelte funksjoner krever at du er logget inn.

---

## Test funksjonalitet

Etter innlogging kan du navigere manuelt i `static`-mappen for å teste de ulike sidene og funksjonene:  

- **CRUD-operasjoner** for bøker og brukere  
- **Slett bøker** utgitt etter år 2000 (krever innlogging)  
- **Statistikkfunksjon** som returnerer brukerdata via `HttpServletRequest`  


---------------------------------------------------------------------------------------------------
**PostGres SQL**
## Teknologier brukt

- Java  
- Spring Boot  
- JPA/Hibernate  
- PostgreSQL (egen pakke for migrering fra H2/JPA)  
- HTML/CSS/JavaScript for frontend  

---

## Konfigurasjon

`application.properties` inkluderer PostgreSQL-konfigurasjon:

```properties
spring.application.name=versjon2
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data.sql
logging.level.org.springframework.jdbc.datasource.init.ScriptUtils=DEBUG
spring.jpa.hibernate.ddl-auto=create
spring.datasource.url=jdbc:postgresql://localhost:5432/booksystem
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=postgres123
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
logging.level.org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration=DEBUG
