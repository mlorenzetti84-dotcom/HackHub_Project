# HackHub

**Progetto di Ingegneria del Software – Corso di Informatica**

**Componenti del gruppo**

| Studente | Matricola |
|---|---|
| Matteo Lorenzetti | 119774 |
| Riccardo Coppari | 119999 |
| Alessandro Marucci | 110194 |

## Il progetto

HackHub è un'applicazione per gestire gli hackathon, dalla creazione dell'evento fino alla valutazione delle sottomissioni e alla proclamazione del team vincitore. Il progetto è stato realizzato in Java e portato su Spring Boot, con API REST e una simulazione da console per mostrare le operazioni principali.

Sono previsti diversi ruoli: visitatore, utente registrato, membro del team, organizzatore, giudice e mentore. Le operazioni disponibili dipendono dal ruolo, dall'appartenenza allo staff dell'evento e dalla fase in cui si trova l'hackathon.

## Funzionalità principali

- Registrazione e autenticazione degli utenti.
- Creazione dei team, invio e gestione degli inviti.
- Creazione degli hackathon e assegnazione di organizzatore, giudice e mentori.
- Iscrizione dei team agli eventi durante la fase prevista.
- Invio e aggiornamento delle sottomissioni entro la scadenza.
- Richieste di supporto, prenotazione delle call con i mentori e segnalazioni di violazioni.
- Valutazione delle sottomissioni con voto da 0 a 10 e commento.
- Proclamazione del vincitore ed erogazione simulata del premio.

Gli hackathon attraversano quattro stati: `IN_ISCRIZIONE`, `IN_CORSO`, `IN_VALUTAZIONE` e `CONCLUSO`.

## Tecnologie utilizzate

- Java 17
- Spring Boot 3.3.5 (Spring Web e Spring Data JPA)
- Maven
- H2, database in memoria
- JUnit per i test automatici

## Organizzazione del progetto

```text
HackHub_Project/
├── models/
│   └── HackHub_Finale.vpp
├── src/
│   ├── main/
│   │   ├── java/com/hackhub/
│   │   └── resources/
│   └── test/
├── pom.xml
├── README.md
└── .gitignore
```

Il codice distingue i controller REST, i servizi applicativi, il dominio, i repository e gli oggetti DTO. Le regole relative a ruoli, sottomissioni, transizioni di stato e valutazioni sono gestite soprattutto nel dominio.

Il modello Visual Paradigm si trova in `models/HackHub_Finale.vpp`. Comprende i casi d'uso, i diagrammi delle classi di analisi e di progetto, i diagrammi di sequenza e un diagramma dei componenti. Il lavoro è organizzato in cinque iterazioni: utenti e team; creazione e iscrizione agli hackathon; sottomissioni e supporto; mentoring e violazioni; valutazione e conclusione. Nel VPP sono incluse anche le specifiche dettagliate dei casi d'uso.

## Design pattern

**State Pattern.** È utilizzato per gestire il ciclo di vita degli hackathon. L'interfaccia `HackathonState` e le sue implementazioni (`InRegistrationState`, `InProgressState`, `InEvaluationState`, `CompletedState`) determinano quali operazioni sono consentite in ogni fase.

**Strategy Pattern.** È utilizzato per la prenotazione delle call di mentoring tramite `CalendarService`. Sono presenti due implementazioni:

- `InMemoryCalendarService`: controlla la validità degli appuntamenti e impedisce sovrapposizioni per lo stesso mentore.
- `BusinessHoursCalendarService`: applica anche il vincolo dei giorni feriali e della fascia oraria 09:00–18:00.

La strategia si configura con `hackhub.calendar.strategy`, impostata di default su `in-memory`; l'alternativa è `business-hours`.

Il servizio di pagamento è rappresentato dall'interfaccia `PaymentService` e dall'implementazione dimostrativa `FakePaymentService`. Non vengono effettuati pagamenti reali. Anche il calendario è simulato localmente e non si collega a un provider esterno.

## Avvio

Per compilare ed eseguire il progetto servono **JDK 17** e **Maven**. Aprire un terminale nella cartella contenente `pom.xml`.

**Avviare il backend REST:**

```bash
mvn spring-boot:run
```

L'applicazione è disponibile sulla porta `8081`. La console H2 è accessibile all'indirizzo `http://localhost:8081/h2-console`, con URL JDBC `jdbc:h2:mem:hackhubdb`, utente `sa` e password vuota.

All'avvio vengono caricati alcuni dati dimostrativi. Per esempio, è possibile utilizzare l'account organizzatore `a.rossi@unicam.it` con password `demo-password`. Il database H2 è in memoria: i dati vengono ricreati al riavvio.

**Avviare la simulazione da console:**

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=cli"
```

La simulazione permette di ripercorrere le operazioni principali senza utilizzare le API REST.

**Eseguire i test:**

```bash
mvn clean test
```

Nell'ultima esecuzione locale del 16 novembre 2026 sono stati superati **33 test su 33**, senza fallimenti né errori.

## API REST

Le API sono organizzate principalmente nei percorsi `/api/auth`, `/api/users`, `/api/teams` e `/api/hackathons`.

La registrazione e il login sono accessibili senza autenticazione; per le operazioni riservate è necessario inviare l'header `X-Access-Token` ottenuto dopo l'accesso. I controlli verificano il ruolo e l'eventuale appartenenza al team o allo staff dello specifico hackathon.

Il progetto utilizza dati e servizi dimostrativi, pensati per l'esecuzione e la valutazione in ambito universitario.
