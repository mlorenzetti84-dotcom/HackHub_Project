package com.hackhub.domain.test;

import com.hackhub.domain.actor.Invitation;
import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.StaffMember;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.hackathon.Evaluation;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;
import com.hackhub.domain.hackathon.ViolationReport;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import com.hackhub.domain.service.FakePaymentService;
import com.hackhub.domain.service.InMemoryCalendarService;
import com.hackhub.domain.service.PaymentReceipt;
import com.hackhub.domain.service.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class SimulazioneInterattiva {

    private static final String RED = "\u001B[31;1m";
    private static final String GREEN = "\u001B[32;1m";
    private static final String YELLOW = "\u001B[33;1m";
    private static final String CYAN = "\u001B[36;1m";
    private static final String RESET = "\u001B[0m";

    private final Scanner scanner = new Scanner(System.in);
    private final CalendarService calendarService = new InMemoryCalendarService();
    private final PaymentService paymentService = new FakePaymentService();

    // Registri in memoria: simulano gli id usati nei path dei Controller REST.
    private final Map<Long, User> users = new LinkedHashMap<>();
    private final Map<Long, Team> teams = new LinkedHashMap<>();
    private final Map<Long, Hackathon> hackathons = new LinkedHashMap<>();
    private final Map<String, Invitation> invitations = new LinkedHashMap<>();

    private long nextUserId = 1;
    private long nextTeamId = 1;
    private long nextHackathonId = 1;

    private Long activeHackathonId;

    public SimulazioneInterattiva() {
        seedDemoData();
    }

    public static void main(String[] args) {
        new SimulazioneInterattiva().start();
    }

    public void start() {
        printHeader();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> execute(this::listHackathons);
                case "2" -> execute(this::selectActiveHackathon);
                case "3" -> execute(this::showCurrentStatus);
                case "4" -> execute(this::createHackathon);
                case "5" -> execute(this::createUser);
                case "6" -> execute(this::createTeam);
                case "7" -> execute(this::inviteUserToTeam);
                case "8" -> execute(this::answerInvitation);
                case "9" -> execute(this::registerTeam);
                case "10" -> execute(this::submitProject);
                case "11" -> execute(this::updateSubmission);
                case "12" -> execute(this::assignStaffMember);
                case "13" -> execute(this::assignMentor);
                case "14" -> execute(this::proposeMentorCall);
                case "15" -> execute(this::reportViolation);
                case "16" -> execute(this::evaluateSubmission);
                case "17" -> execute(this::disqualifyTeam);
                case "18" -> execute(this::advanceHackathonState);
                case "19" -> execute(this::proclaimWinner);
                case "20" -> execute(this::payPrizeToWinner);
                case "21" -> execute(this::proclaimWinnerAndPayPrize);
                case "22" -> execute(this::showDashboard);
                case "0" -> running = false;
                default -> System.out.println(YELLOW + "Scelta non valida. Inserisci un numero del menu." + RESET);
            }
        }

        System.out.println("Simulazione terminata.");
    }

    private void seedDemoData() {
        Organizer organizer = registerUser(new Organizer("organizzatore", "organizzatore@hackhub.test"));
        Judge judge = registerUser(new Judge("giudice", "giudice@hackhub.test"));
        Mentor mentor = registerUser(new Mentor("mentore", "mentore@hackhub.test"));
        User developer = registerUser(new User("sviluppatore", "dev@hackhub.test"));
        User guest = registerUser(new User("ospite", "guest@hackhub.test"));

        Team team = registerTeamLocal(new Team("Team Sviluppatori", developer, 4));
        registerTeamLocal(new Team("Team Ospite", guest, 4));

        LocalDate today = LocalDate.now();
        Hackathon hackathon = new Hackathon(
                "AI Innovation 2026",
                "Laboratorio demo",
                "Costruire un prototipo funzionante rispettando il regolamento.",
                today.minusDays(5),
                today.minusDays(1),
                today,
                today.plusDays(2),
                today.plusDays(5),
                BigDecimal.valueOf(5000),
                4,
                organizer
        );

        organizer.assignStaff(hackathon, judge);
        hackathon.addMentor(mentor);
        activeHackathonId = registerHackathonLocal(hackathon);

        System.out.println("Dataset demo caricato. Team principale: " + team.getName());
    }

    private void printHeader() {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(" HackHub - Simulazione Interattiva CLI all-in-one");
        System.out.println(" Copre i flussi dei Controller REST e le validazioni dominio");
        System.out.println("============================================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println(CYAN + "Hackathon attivo: " + describeActiveHackathon() + RESET);
        System.out.println("[1]  Lista hackathon");
        System.out.println("[2]  Seleziona hackathon attivo");
        System.out.println("[3]  Visualizza dettaglio hackathon attivo");
        System.out.println("[4]  Crea nuovo hackathon (Organizer)");
        System.out.println("[5]  Crea utente / Organizer / Giudice / Mentore");
        System.out.println("[6]  Crea team");
        System.out.println("[7]  Invita utente nel team");
        System.out.println("[8]  Accetta o rifiuta invito");
        System.out.println("[9]  Iscrivi team all'hackathon");
        System.out.println("[10] Carica sottomissione progetto");
        System.out.println("[11] Aggiorna sottomissione progetto");
        System.out.println("[12] Assegna staff (Giudice o Organizer)");
        System.out.println("[13] Assegna Mentore");
        System.out.println("[14] Proponi call di supporto con Mentore");
        System.out.println("[15] Segnala violazione verso team");
        System.out.println("[16] Valuta sottomissione");
        System.out.println("[17] Squalifica team");
        System.out.println("[18] Avanza stato hackathon");
        System.out.println("[19] Proclama vincitore");
        System.out.println("[20] Paga premio al vincitore");
        System.out.println("[21] Proclama vincitore e paga premio");
        System.out.println("[22] Dashboard sintetica team/hackathon");
        System.out.println("[0]  Esci");
        System.out.print("Scelta: ");
    }

    private void listHackathons() {
        System.out.println();
        hackathons.forEach((id, hackathon) -> System.out.println(formatHackathon(id, hackathon)));
    }

    private void selectActiveHackathon() {
        listHackathons();
        activeHackathonId = readExistingHackathonId("ID hackathon da attivare: ");
        printSuccess("Hackathon attivo aggiornato.");
    }

    private void showCurrentStatus() {
        Hackathon hackathon = activeHackathon();

        System.out.println();
        System.out.println("Nome: " + hackathon.getName());
        System.out.println("Luogo: " + hackathon.getLocation());
        System.out.println("Regole: " + hackathon.getRules());
        System.out.println("Stato: " + hackathon.getStatus());
        System.out.println("Date: iscrizione " + hackathon.getRegistrationStart() + " -> " + hackathon.getRegistrationEnd()
                + " | hackathon " + hackathon.getHackathonStart() + " -> " + hackathon.getHackathonEnd()
                + " | valutazione fino a " + hackathon.getEvaluationEnd());
        System.out.println("Premio: " + hackathon.getPrizeAmount());
        System.out.println("Dimensione massima team: " + hackathon.getMaxTeamSize());

        printStaff(hackathon);
        printTeams(hackathon);
        printSubmissions(hackathon);
        printMentorCalls(hackathon);
        printViolations(hackathon);
        printPayment(hackathon);
    }

    private void createHackathon() {
        Organizer organizer = readOrganizer("Organizer creatore");
        String name = readText("Nome hackathon: ");
        String location = readText("Luogo: ");
        String rules = readText("Regole: ");
        LocalDate registrationStart = readDate("Inizio iscrizioni (yyyy-MM-dd): ");
        LocalDate registrationEnd = readDate("Fine iscrizioni (yyyy-MM-dd): ");
        LocalDate hackathonStart = readDate("Inizio hackathon (yyyy-MM-dd): ");
        LocalDate hackathonEnd = readDate("Fine hackathon (yyyy-MM-dd): ");
        LocalDate evaluationEnd = readDate("Fine valutazione (yyyy-MM-dd): ");
        BigDecimal prizeAmount = readBigDecimal("Importo premio: ");
        int maxTeamSize = readInt("Dimensione massima team: ");

        Hackathon hackathon = new Hackathon(
                name,
                location,
                rules,
                registrationStart,
                registrationEnd,
                hackathonStart,
                hackathonEnd,
                evaluationEnd,
                prizeAmount,
                maxTeamSize,
                organizer
        );
        activeHackathonId = registerHackathonLocal(hackathon);
        printSuccess("Hackathon creato con ID CLI " + activeHackathonId + ".");
    }

    private void createUser() {
        System.out.println("[1] User  [2] Organizer  [3] Judge  [4] Mentor");
        String type = readText("Tipo utente: ");
        String username = readText("Username: ");
        String email = readText("Email: ");

        User user = switch (type) {
            case "1" -> new User(username, email);
            case "2" -> new Organizer(username, email);
            case "3" -> new Judge(username, email);
            case "4" -> new Mentor(username, email);
            default -> throw new ValidationException("Tipo utente non valido");
        };

        long id = idOf(registerUser(user));
        printSuccess("Utente creato con ID CLI " + id + ".");
    }

    private void createTeam() {
        printUsers();
        User owner = readUser("ID owner team: ");
        String name = readText("Nome team: ");
        int maxSize = readInt("Dimensione massima team: ");

        Team team = registerTeamLocal(new Team(name, owner, maxSize));
        printSuccess("Team creato con ID CLI " + idOf(team) + ".");
    }

    private void inviteUserToTeam() {
        printTeams();
        Team team = readTeam("ID team: ");
        printUsers();
        User invitedBy = readUser("ID utente invitante (deve essere membro del team): ");
        User invitedUser = readUser("ID utente invitato: ");

        Invitation invitation = invitedBy.inviteToTeam(invitedUser, team);
        invitations.put(invitation.getId().toString(), invitation);

        printSuccess("Invito creato. ID invito: " + invitation.getId());
    }

    private void answerInvitation() {
        printInvitations();
        Invitation invitation = readInvitation("ID invito: ");
        User invitedUser = invitation.getInvitedUser();

        System.out.println("[A] Accetta  [R] Rifiuta");
        String answer = readText("Risposta: ").toUpperCase();
        if ("A".equals(answer)) {
            invitedUser.acceptInvitation(invitation);
            printSuccess("Invito accettato. Team aggiornato: " + invitation.getTeam().getName() + ".");
            return;
        }
        if ("R".equals(answer)) {
            invitation.decline(invitedUser);
            printSuccess("Invito rifiutato.");
            return;
        }
        throw new ValidationException("Risposta invito non valida");
    }

    private void registerTeam() {
        Hackathon hackathon = activeHackathon();
        printTeams();
        Team team = readTeam("ID team da iscrivere: ");

        System.out.print("Vuoi simulare il controllo TeamRestController sul membro? (s/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            printUsers(team);
            User member = readUser("ID membro richiedente: ");
            requireTeamMember(team, member);
        }

        hackathon.registerTeam(team);
        printSuccess("Team iscritto all'hackathon.");
    }

    private void submitProject() {
        Hackathon hackathon = activeHackathon();
        Team team = readRegisteredTeam(hackathon, "ID team che sottomette: ");
        optionallyCheckSubmittingMember(team);
        String projectName = readText("Nome progetto: ");
        String repositoryUrl = readText("URL repository/contenuto: ");

        Submission submission = hackathon.submitProject(team, projectName, repositoryUrl);
        printSuccess("Sottomissione creata. ID locale: " + indexOfSubmission(hackathon, submission) + ".");
    }

    private void updateSubmission() {
        Hackathon hackathon = activeHackathon();
        Team team = readRegisteredTeam(hackathon, "ID team che aggiorna: ");
        optionallyCheckSubmittingMember(team);
        String projectName = readText("Nuovo nome progetto: ");
        String repositoryUrl = readText("Nuovo URL repository/contenuto: ");

        hackathon.updateSubmission(team, projectName, repositoryUrl);
        printSuccess("Sottomissione aggiornata.");
    }

    private void assignStaffMember() {
        Hackathon hackathon = activeHackathon();
        Organizer organizer = readOrganizer("Organizer autorizzante");
        printUsers();
        User selected = readUser("ID utente da assegnare come staff: ");
        if (!(selected instanceof Judge || selected instanceof Organizer)) {
            throw new ValidationException("Lo staff assegnabile da questa voce deve essere Judge o Organizer");
        }

        organizer.assignStaff(hackathon, (StaffMember) selected);
        printSuccess("Staff assegnato: " + selected.getUsername() + ".");
    }

    private void assignMentor() {
        Hackathon hackathon = activeHackathon();
        printUsers();
        User selected = readUser("ID mentor da assegnare: ");
        if (!(selected instanceof Mentor mentor)) {
            throw new ValidationException("User is not a mentor");
        }

        hackathon.addMentor(mentor);
        printSuccess("Mentore assegnato all'hackathon.");
    }

    private void proposeMentorCall() {
        Hackathon hackathon = activeHackathon();
        Team team = readRegisteredTeam(hackathon, "ID team: ");
        Mentor mentor = readMentor("Mentore");
        LocalDateTime start = readDateTime("Inizio call (yyyy-MM-ddTHH:mm): ");
        LocalDateTime end = readDateTime("Fine call (yyyy-MM-ddTHH:mm): ");

        CalendarBooking booking = hackathon.requestSupport(team, mentor, calendarService, start, end);
        printSuccess("Call prenotata: " + booking.getExternalReference()
                + " | " + booking.getStart() + " -> " + booking.getEnd() + ".");
    }

    private void reportViolation() {
        Hackathon hackathon = activeHackathon();
        Team team = readRegisteredTeam(hackathon, "ID team segnalato: ");
        Mentor mentor = readMentor("Mentore/staff segnalante");
        String description = readText("Descrizione violazione: ");

        ViolationReport report = hackathon.reportViolation(mentor, team, description);
        printSuccess("Violazione registrata il " + report.getReportedAt() + ".");
    }

    private void evaluateSubmission() {
        Hackathon hackathon = activeHackathon();
        Submission submission = readSubmission(hackathon, "ID sottomissione da valutare: ");
        Judge judge = readJudge("Giudice");
        int score = readInt("Voto del giudice (0-10): ");
        String comment = readText("Commento: ");

        judge.evaluate(hackathon, submission, score, comment);
        printSuccess("Valutazione inserita.");
    }

    private void disqualifyTeam() {
        Hackathon hackathon = activeHackathon();
        Organizer organizer = readOrganizer("Organizer autorizzante");
        Team team = readRegisteredTeam(hackathon, "ID team da squalificare: ");

        hackathon.disqualifyTeam(organizer, team);
        printSuccess("Team squalificato.");
    }

    private void advanceHackathonState() {
        Hackathon hackathon = activeHackathon();
        System.out.println("Stato corrente: " + hackathon.getStatus());
        System.out.println("[1] IN_ISCRIZIONE  [2] IN_CORSO  [3] IN_VALUTAZIONE  [4] CONCLUSO  [N] prossimo valido");
        String choice = readText("Nuovo stato: ").toUpperCase();

        HackathonStatus next = switch (choice) {
            case "1" -> HackathonStatus.IN_ISCRIZIONE;
            case "2" -> HackathonStatus.IN_CORSO;
            case "3" -> HackathonStatus.IN_VALUTAZIONE;
            case "4" -> HackathonStatus.CONCLUSO;
            case "N" -> nextStatus(hackathon.getStatus());
            default -> throw new ValidationException("Stato richiesto non valido");
        };

        hackathon.advanceTo(next);
        printSuccess("Stato avanzato a " + next + ".");
    }

    private void proclaimWinner() {
        Hackathon hackathon = activeHackathon();
        Organizer organizer = readOrganizer("Organizer autorizzante");
        Submission submission = readSubmission(hackathon, "ID sottomissione vincitrice: ");

        organizer.proclaimWinner(hackathon, submission);
        printSuccess("Vincitore proclamato: " + submission.getTeam().getName() + ".");
    }

    private void payPrizeToWinner() {
        Hackathon hackathon = activeHackathon();
        PaymentReceipt receipt = hackathon.payPrize(paymentService);

        printSuccess("Premio pagato. Reference: " + receipt.getExternalReference()
                + " | importo: " + receipt.getAmount() + ".");
    }

    private void proclaimWinnerAndPayPrize() {
        proclaimWinner();
        payPrizeToWinner();
    }

    private void showDashboard() {
        Hackathon hackathon = activeHackathon();
        printTeams();
        Team team = readTeam("ID team dashboard: ");

        Optional<Submission> submission = hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getTeam().equals(team))
                .findFirst();
        Optional<Submission> nextUnevaluated = hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getEvaluations().isEmpty())
                .findFirst();

        System.out.println();
        System.out.println("Hackathon: " + hackathon.getName());
        System.out.println("Team registrato: " + yesNo(hackathon.getRegisteredTeams().contains(team)));
        System.out.println("Team squalificato: " + yesNo(team.isDisqualified()));
        System.out.println("Submission ID: " + submission.map(value -> String.valueOf(indexOfSubmission(hackathon, value))).orElse("nessuna"));
        System.out.println("Valutazioni team: " + submission.map(value -> String.valueOf(value.getEvaluations().size())).orElse("0"));
        System.out.println("Media team: " + submission.map(Submission::averageScore).orElse(0.0));
        System.out.println("Sottomissioni non valutate: " + hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getEvaluations().isEmpty())
                .count());
        System.out.println("Prossima da valutare: " + nextUnevaluated
                .map(value -> indexOfSubmission(hackathon, value) + " | " + value.getTeam().getName())
                .orElse("nessuna"));
        System.out.println("Segnalazioni violazione: " + hackathon.getViolationReports().size());
        System.out.println("Winning team ID: " + Optional.ofNullable(hackathon.getWinningSubmission())
                .map(value -> String.valueOf(idOf(value.getTeam())))
                .orElse("non proclamato"));
    }

    private void printStaff(Hackathon hackathon) {
        System.out.println();
        System.out.println("Staff:");
        if (hackathon.getStaffMembers().isEmpty()) {
            System.out.println("- nessuno");
            return;
        }
        hackathon.getStaffMembers().forEach(staff ->
                System.out.println("- userId=" + idOf(staff) + " | " + staff.getRole() + " | " + staff.getUsername()));
    }

    private void printTeams(Hackathon hackathon) {
        System.out.println();
        System.out.println("Team iscritti:");
        if (hackathon.getRegisteredTeams().isEmpty()) {
            System.out.println("- nessuno");
            return;
        }
        hackathon.getRegisteredTeams().forEach(team ->
                System.out.println("- teamId=" + idOf(team) + " | " + team.getName()
                        + " | membri=" + team.getMembers().size()
                        + " | squalificato=" + yesNo(team.isDisqualified())));
    }

    private void printSubmissions(Hackathon hackathon) {
        System.out.println();
        System.out.println("Sottomissioni:");
        if (hackathon.getSubmissions().isEmpty()) {
            System.out.println("- nessuna");
            return;
        }
        for (int i = 0; i < hackathon.getSubmissions().size(); i++) {
            Submission submission = hackathon.getSubmissions().get(i);
            System.out.println("- submissionId=" + (i + 1)
                    + " | team=" + submission.getTeam().getName()
                    + " | progetto=" + submission.getProjectName()
                    + " | repository=" + submission.getRepositoryUrl()
                    + " | media=" + submission.averageScore());
            for (Evaluation evaluation : submission.getEvaluations()) {
                System.out.println("  voto=" + evaluation.getScore()
                        + " | giudice=" + evaluation.getJudge().getUsername()
                        + " | commento=" + evaluation.getComment());
            }
        }

        if (hackathon.getWinningSubmission() != null) {
            System.out.println("Vincitore: " + hackathon.getWinningSubmission().getTeam().getName());
        }
    }

    private void printMentorCalls(Hackathon hackathon) {
        System.out.println();
        System.out.println("Call mentoring:");
        if (hackathon.getMentoringCalls().isEmpty()) {
            System.out.println("- nessuna");
            return;
        }
        hackathon.getMentoringCalls().forEach(call ->
                System.out.println("- " + call.getExternalReference() + " | " + call.getStart() + " -> " + call.getEnd()));
    }

    private void printViolations(Hackathon hackathon) {
        System.out.println();
        System.out.println("Segnalazioni violazione:");
        if (hackathon.getViolationReports().isEmpty()) {
            System.out.println("- nessuna");
            return;
        }
        hackathon.getViolationReports().forEach(report ->
                System.out.println("- team=" + report.getTeam().getName()
                        + " | mentor=" + report.getMentor().getUsername()
                        + " | " + report.getDescription()));
    }

    private void printPayment(Hackathon hackathon) {
        PaymentReceipt receipt = hackathon.getPrizePaymentReceipt();
        System.out.println();
        if (receipt == null) {
            System.out.println("Pagamento premio: non eseguito");
            return;
        }
        System.out.println("Pagamento premio: " + receipt.getExternalReference()
                + " | importo=" + receipt.getAmount()
                + " | data=" + receipt.getPaidAt());
    }

    private void printUsers() {
        System.out.println();
        System.out.println("Utenti:");
        users.forEach((id, user) -> System.out.println("- userId=" + id
                + " | " + roleOf(user)
                + " | " + user.getUsername()
                + " | " + user.getEmail()
                + " | team=" + Optional.ofNullable(user.getCurrentTeam()).map(Team::getName).orElse("nessuno")));
    }

    private void printUsers(Team team) {
        System.out.println();
        System.out.println("Membri team " + team.getName() + ":");
        team.getMembers().forEach(user -> System.out.println("- userId=" + idOf(user) + " | " + user.getUsername()));
    }

    private void printTeams() {
        System.out.println();
        System.out.println("Team:");
        teams.forEach((id, team) -> System.out.println("- teamId=" + id
                + " | " + team.getName()
                + " | membri=" + team.getMembers().size() + "/" + team.getMaxSize()
                + " | squalificato=" + yesNo(team.isDisqualified())));
    }

    private void printInvitations() {
        System.out.println();
        System.out.println("Inviti:");
        if (invitations.isEmpty()) {
            System.out.println("- nessuno");
            return;
        }
        invitations.forEach((id, invitation) -> System.out.println("- invitationId=" + id
                + " | team=" + invitation.getTeam().getName()
                + " | invitato=" + invitation.getInvitedUser().getUsername()
                + " | invitante=" + invitation.getInvitedBy().getUsername()
                + " | stato=" + invitation.getStatus()));
    }

    private void optionallyCheckSubmittingMember(Team team) {
        System.out.print("Vuoi simulare il controllo TeamRestController sul membro? (s/N): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
            printUsers(team);
            User member = readUser("ID membro richiedente: ");
            requireTeamMember(team, member);
        }
    }

    private Organizer readOrganizer(String label) {
        printUsers();
        User user = readUser(label + " - ID organizer: ");
        if (user instanceof Organizer organizer) {
            return organizer;
        }
        throw new ValidationException("User is not an organizer");
    }

    private Judge readJudge(String label) {
        printUsers();
        User user = readUser(label + " - ID judge: ");
        if (user instanceof Judge judge) {
            return judge;
        }
        throw new ValidationException("User is not a judge");
    }

    private Mentor readMentor(String label) {
        printUsers();
        User user = readUser(label + " - ID mentor: ");
        if (user instanceof Mentor mentor) {
            return mentor;
        }
        throw new ValidationException("User is not a mentor");
    }

    private Hackathon activeHackathon() {
        if (activeHackathonId == null || !hackathons.containsKey(activeHackathonId)) {
            throw new ValidationException("Nessun hackathon attivo");
        }
        return hackathons.get(activeHackathonId);
    }

    private Long readExistingHackathonId(String prompt) {
        long id = readLong(prompt);
        if (!hackathons.containsKey(id)) {
            throw new ValidationException("Hackathon not found: " + id);
        }
        return id;
    }

    private User readUser(String prompt) {
        long id = readLong(prompt);
        User user = users.get(id);
        if (user == null) {
            throw new ValidationException("User not found: " + id);
        }
        return user;
    }

    private Team readTeam(String prompt) {
        long id = readLong(prompt);
        Team team = teams.get(id);
        if (team == null) {
            throw new ValidationException("Team not found: " + id);
        }
        return team;
    }

    private Team readRegisteredTeam(Hackathon hackathon, String prompt) {
        printTeams(hackathon);
        Team team = readTeam(prompt);
        if (!hackathon.getRegisteredTeams().contains(team)) {
            throw new ValidationException("Team is not registered for this hackathon");
        }
        return team;
    }

    private Submission readSubmission(Hackathon hackathon, String prompt) {
        printSubmissions(hackathon);
        int index = readInt(prompt);
        if (index < 1 || index > hackathon.getSubmissions().size()) {
            throw new ValidationException("Submission not found: " + index);
        }
        return hackathon.getSubmissions().get(index - 1);
    }

    private Invitation readInvitation(String prompt) {
        String id = readText(prompt);
        Invitation invitation = invitations.get(id);
        if (invitation == null) {
            throw new ValidationException("Invitation not found: " + id);
        }
        return invitation;
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        if (value.isBlank()) {
            throw new ValidationException("Valore obbligatorio");
        }
        return value;
    }

    private int readInt(String prompt) {
        try {
            return Integer.parseInt(readText(prompt));
        } catch (NumberFormatException exception) {
            throw new ValidationException("Numero intero non valido");
        }
    }

    private long readLong(String prompt) {
        try {
            return Long.parseLong(readText(prompt));
        } catch (NumberFormatException exception) {
            throw new ValidationException("ID numerico non valido");
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        try {
            return new BigDecimal(readText(prompt));
        } catch (NumberFormatException exception) {
            throw new ValidationException("Importo non valido");
        }
    }

    private LocalDate readDate(String prompt) {
        try {
            return LocalDate.parse(readText(prompt));
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Data non valida. Usa il formato yyyy-MM-dd");
        }
    }

    private LocalDateTime readDateTime(String prompt) {
        try {
            return LocalDateTime.parse(readText(prompt));
        } catch (DateTimeParseException exception) {
            throw new ValidationException("Data/ora non valida. Usa il formato yyyy-MM-ddTHH:mm");
        }
    }

    private void requireTeamMember(Team team, User user) {
        if (!team.hasMember(user)) {
            throw new ValidationException("User does not belong to team: " + idOf(user));
        }
    }

    private HackathonStatus nextStatus(HackathonStatus current) {
        return switch (current) {
            case IN_ISCRIZIONE -> HackathonStatus.IN_CORSO;
            case IN_CORSO -> HackathonStatus.IN_VALUTAZIONE;
            case IN_VALUTAZIONE -> HackathonStatus.CONCLUSO;
            case CONCLUSO -> throw new ValidationException("L'hackathon e gia concluso");
        };
    }

    private Organizer registerUser(Organizer organizer) {
        users.put(nextUserId++, organizer);
        return organizer;
    }

    private Judge registerUser(Judge judge) {
        users.put(nextUserId++, judge);
        return judge;
    }

    private Mentor registerUser(Mentor mentor) {
        users.put(nextUserId++, mentor);
        return mentor;
    }

    private User registerUser(User user) {
        users.put(nextUserId++, user);
        return user;
    }

    private Team registerTeamLocal(Team team) {
        teams.put(nextTeamId++, team);
        return team;
    }

    private Long registerHackathonLocal(Hackathon hackathon) {
        long id = nextHackathonId++;
        hackathons.put(id, hackathon);
        return id;
    }

    private long idOf(User user) {
        return users.entrySet().stream()
                .filter(entry -> entry.getValue() == user)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1L);
    }

    private long idOf(Team team) {
        return teams.entrySet().stream()
                .filter(entry -> entry.getValue() == team)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(-1L);
    }

    private int indexOfSubmission(Hackathon hackathon, Submission submission) {
        return hackathon.getSubmissions().indexOf(submission) + 1;
    }

    private String describeActiveHackathon() {
        if (activeHackathonId == null || !hackathons.containsKey(activeHackathonId)) {
            return "nessuno";
        }
        return formatHackathon(activeHackathonId, hackathons.get(activeHackathonId));
    }

    private String formatHackathon(Long id, Hackathon hackathon) {
        return "hackathonId=" + id
                + " | " + hackathon.getName()
                + " | stato=" + hackathon.getStatus()
                + " | team=" + hackathon.getRegisteredTeams().size()
                + " | submissions=" + hackathon.getSubmissions().size();
    }

    private String roleOf(User user) {
        if (user instanceof StaffMember staffMember) {
            return staffMember.getRole().name();
        }
        return "USER";
    }

    private void execute(Action action) {
        try {
            action.run();
        } catch (Exception exception) {
            System.out.println(RED + "[BLOCCATA] AZIONE BLOCCATA DALLA BLINDATURA: "
                    + exception.getMessage() + RESET);
        }
    }

    private void printSuccess(String message) {
        System.out.println(GREEN + "[OK] " + message + RESET);
    }

    private String yesNo(boolean value) {
        return value ? "SI" : "NO";
    }

    @FunctionalInterface
    private interface Action {
        void run();
    }
}
