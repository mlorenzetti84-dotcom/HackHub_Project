# HackHub - Gestione Hackathon

## Dati Studente

- Nome: `MATTEO - RICCARDO - ALESSANDRO`
- Cognome: `LORENZETTI - COPPARI - MARUCCI`
- Matricola: `119774 - 119999 - 110194`
- Corso: `INFORMATICA`

## Descrizione

HackHub e una piattaforma backend per la gestione del ciclo di vita di un hackathon. Il progetto e sviluppato in Java 17 con Spring Boot 3.3.5 e usa Spring Web, Spring Data JPA e database H2 in memoria.

L'applicazione espone API REST, inizializza dati dimostrativi all'avvio e include una simulazione testuale da console per presentare i casi d'uso richiesti dalla traccia d'esame. Ogni hackathon gestisce nome, luogo, regolamento, finestre temporali, premio, dimensione massima dei team, staff, iscrizioni, sottomissioni, valutazioni, segnalazioni e proclamazione del vincitore.

Le regole applicative non sono duplicate nei controller: i controller ricevono le richieste HTTP, l'application service coordina transazioni e repository, mentre il dominio mantiene vincoli, transizioni di stato e logiche di business.

## Tecnologie

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- H2 Database
- Maven

## Architettura

Il codice segue un'architettura a layer:

- `com.hackhub`: classe di bootstrap Spring Boot.
- `com.hackhub.config`: inizializzazione dati demo e runner console con profilo `cli`.
- `com.hackhub.controller`: controller REST e gestione centralizzata degli errori HTTP.
- `com.hackhub.dto`: DTO usati dalle API REST.
- `com.hackhub.domain.actor`: utenti, ruoli di staff, team e inviti.
- `com.hackhub.domain.hackathon`: aggregate principale, sottomissioni, valutazioni e violazioni.
- `com.hackhub.domain.state`: State Pattern per il ciclo di vita dell'hackathon.
- `com.hackhub.domain.service`: Strategy Pattern per servizi esterni simulati di calendario e pagamento.
- `com.hackhub.domain.exception`: eccezioni di dominio.
- `com.hackhub.persistence`: converter JPA per persistere lo stato dell'hackathon.
- `com.hackhub.repository`: repository Spring Data JPA.
- `com.hackhub.service`: application service transazionale.
- `com.hackhub.domain.test`: simulazione console e test manuali di dominio.

## Diagrammi UML

I diagrammi UML del progetto sono inclusi nella cartella `.vpp/`, nel file:

```text
.vpp/HackHub_Progetto_Completo.vpp
```

Il modello UML documenta la struttura a layer, il dominio principale e i pattern usati nell'applicazione.

## Funzionalita Coperte

- Consultazione pubblica degli hackathon con stato, regolamento, luogo e statistiche.
- Creazione team, invito utenti e accettazione inviti.
- Iscrizione dei team durante lo stato `IN_ISCRIZIONE`.
- Caricamento e aggiornamento sottomissioni durante lo stato `IN_CORSO`.
- Prenotazione call mentore tramite servizio calendario esterno simulato.
- Segnalazione violazioni da parte del mentore.
- Valutazione delle sottomissioni da parte del giudice con voto 0-10 e giudizio scritto.
- Proclamazione del vincitore.
- Simulazione del pagamento premio tramite servizio esterno simulato.

## Design Pattern

### State Pattern

Il ciclo di vita dell'hackathon e rappresentato da quattro stati:

1. `IN_ISCRIZIONE`
2. `IN_CORSO`
3. `IN_VALUTAZIONE`
4. `CONCLUSO`

Le azioni disponibili cambiano in base allo stato corrente. Transizioni e blocchi sono gestiti dal dominio tramite classi di stato concrete, non dai controller.

Lo stato viene persistito su H2 tramite `HackathonStateConverter`, che salva il valore testuale dello stato e ricostruisce la classe concreta corretta in lettura.

### Strategy Pattern

I servizi esterni sono modellati come strategie sostituibili:

- `CalendarService`, implementato da `InMemoryCalendarService`.
- `PaymentService`, implementato da `FakePaymentService`.

Durante la prenotazione di una call viene invocato `CalendarService`. Quando viene proclamato il vincitore con pagamento, l'application service invoca `PaymentService` per simulare l'erogazione del premio.

## Database H2

Il progetto usa H2 in memoria.

- Console H2: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:hackhubdb`
- Username: `sa`
- Password: vuota

I dati dimostrativi vengono caricati automaticamente da `DemoDataInitializer` all'avvio del backend REST.

## Avvio Backend REST

Dalla root Maven del progetto:

```cmd
cd HackHub_Project
mvn spring-boot:run
```

Il backend espone le API REST sulla porta `8081`.

## Avvio da IDE

Aprire il progetto come progetto Maven e avviare la classe:

```text
com.hackhub.HackHubApplication
```

L'IDE deve usare Java 17.

## Simulazione Console

La simulazione interattiva puo essere avviata come classe Java standalone:

```cmd
mvn clean compile
java -cp target\classes com.hackhub.domain.test.SimulazioneInterattiva
```

In alternativa, puo essere eseguita tramite Spring Boot usando il profilo `cli`, che disattiva il web server:

```cmd
mvn spring-boot:run -Dspring-boot.run.profiles=cli
```

## Test di Dominio

```cmd
mvn clean compile
java -cp target\classes com.hackhub.domain.test.HackhubDomainTest
```

Gli 8 test verificano le blindature principali: appartenenza univoca al team, voto 0-10, blocchi di stato, squalifica team, collezioni non modificabili e regole di proclamazione.

## Pulizia per Consegna

```cmd
mvn clean
```

Il comando rimuove gli output temporanei Maven generati nella cartella `target/`.

La root consegnabile deve contenere solo:

```text
src/
.git/
.vpp/
pom.xml
README.md
```
