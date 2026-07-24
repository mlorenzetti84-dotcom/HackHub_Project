package com.hackhub.domain.test;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.hackathon.Evaluation;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

public class SimulazioneInterattiva {

    private static final String RED = "\u001B[31;1m";
    private static final String GREEN = "\u001B[32;1m";
    private static final String YELLOW = "\u001B[33;1m";
    private static final String RESET = "\u001B[0m";

    private final Scanner scanner = new Scanner(System.in);
    private final Organizer organizer = new Organizer("organizzatore", "organizzatore@hackhub.test");
    private final Judge judge = new Judge("giudice", "giudice@hackhub.test");
    private final Mentor mentor = new Mentor("mentore", "mentore@hackhub.test");
    private final Team team = new Team("Team Sviluppatori", new User("sviluppatore", "dev@hackhub.test"), 4);
    private final LocalDate today = LocalDate.now();
    private final Hackathon hackathon = new Hackathon(
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

    public SimulazioneInterattiva() {
        organizer.assignStaff(hackathon, judge);
        hackathon.addMentor(mentor);
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
                case "1" -> execute(this::showCurrentStatus);
                case "2" -> execute(this::registerTeam);
                case "3" -> execute(this::submitProject);
                case "4" -> execute(this::evaluateSubmission);
                case "5" -> execute(this::disqualifyTeam);
                case "6" -> execute(this::advanceHackathonState);
                case "7" -> execute(this::proclaimWinner);
                case "0" -> running = false;
                default -> System.out.println(YELLOW + "Scelta non valida. Inserisci un numero del menu." + RESET);
            }
        }

        System.out.println("Simulazione terminata.");
    }

    private void printHeader() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println(" HackHub - Simulazione Cliente / Esame");
        System.out.println(" Hackathon pre-creato: AI Innovation 2026");
        System.out.println(" Staff: Organizzatore, Giudice, Mentore");
        System.out.println(" Team pronto: Team Sviluppatori");
        System.out.println("==============================================");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("[1] Visualizza stato corrente dell'hackathon");
        System.out.println("[2] Iscrivi il team all'hackathon");
        System.out.println("[3] Carica la sottomissione del progetto");
        System.out.println("[4] Inserisci il voto del giudice");
        System.out.println("[5] Squalifica il team");
        System.out.println("[6] Avanza lo stato dell'hackathon");
        System.out.println("[7] Proclama il vincitore");
        System.out.println("[0] Esci dalla simulazione");
        System.out.print("Scelta: ");
    }

    private void showCurrentStatus() {
        System.out.println();
        System.out.println("Nome: " + hackathon.getName());
        System.out.println("Luogo: " + hackathon.getLocation());
        System.out.println("Stato: " + hackathon.getStatus());
        System.out.println("Team iscritti: " + hackathon.getRegisteredTeams().size());
        System.out.println("Team pronto: " + team.getName() + " | Squalificato: " + yesNo(team.isDisqualified()));
        System.out.println("Sottomissioni: " + hackathon.getSubmissions().size());

        for (Submission submission : hackathon.getSubmissions()) {
            System.out.println("- " + submission.getProjectName()
                    + " | Repository: " + submission.getRepositoryUrl()
                    + " | Valutazioni: " + submission.getEvaluations().size()
                    + " | Media: " + submission.averageScore());
            for (Evaluation evaluation : submission.getEvaluations()) {
                System.out.println("  Voto: " + evaluation.getScore() + " | Giudizio: " + evaluation.getComment());
            }
        }

        if (hackathon.getWinningSubmission() != null) {
            System.out.println("Vincitore: " + hackathon.getWinningSubmission().getTeam().getName());
        } else {
            System.out.println("Vincitore: non proclamato");
        }
    }

    private void registerTeam() {
        hackathon.registerTeam(team);
        printSuccess();
    }

    private void submitProject() {
        hackathon.submitProject(
                team,
                "Assistente AI per Hackathon",
                "https://example.test/team-sviluppatori/ai-hackathon"
        );
        printSuccess();
    }

    private void evaluateSubmission() {
        Submission submission = currentSubmission()
                .orElseThrow(() -> new IllegalStateException("Non esiste ancora una sottomissione da valutare"));

        System.out.print("Inserisci voto del giudice (0-10): ");
        int score = Integer.parseInt(scanner.nextLine().trim());
        judge.evaluate(hackathon, submission, score, "Valutazione inserita dalla simulazione interattiva.");
        printSuccess();
    }

    private void disqualifyTeam() {
        hackathon.disqualifyTeam(organizer, team);
        printSuccess();
    }

    private void advanceHackathonState() {
        HackathonStatus current = hackathon.getStatus();
        HackathonStatus next = switch (current) {
            case IN_ISCRIZIONE -> HackathonStatus.IN_CORSO;
            case IN_CORSO -> HackathonStatus.IN_VALUTAZIONE;
            case IN_VALUTAZIONE -> HackathonStatus.CONCLUSO;
            case CONCLUSO -> throw new IllegalStateException("L'hackathon e gia concluso");
        };

        hackathon.advanceTo(next);
        System.out.println(GREEN + "[OK] Stato avanzato a: " + next + RESET);
    }

    private void proclaimWinner() {
        Submission submission = currentSubmission()
                .orElseThrow(() -> new IllegalStateException("Non esiste alcuna sottomissione da proclamare vincitrice"));

        organizer.proclaimWinner(hackathon, submission);
        printSuccess();
    }

    private Optional<Submission> currentSubmission() {
        return hackathon.getSubmissions().stream()
                .filter(submission -> submission.getTeam().equals(team))
                .findFirst();
    }

    private void execute(Action action) {
        try {
            action.run();
        } catch (Exception exception) {
            System.out.println(RED + "[BLOCCATA] AZIONE BLOCCATA DALLA BLINDATURA: "
                    + exception.getMessage() + RESET);
        }
    }

    private void printSuccess() {
        System.out.println(GREEN + "[OK] Operazione eseguita con successo!" + RESET);
    }

    private String yesNo(boolean value) {
        return value ? "SI" : "NO";
    }

    @FunctionalInterface
    private interface Action {
        void run();
    }
}
