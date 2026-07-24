package com.hackhub.domain.test;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.exception.InvalidStateTransitionException;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

import java.math.BigDecimal;
import java.time.LocalDate;

public class HackhubDomainTest {

    public static void main(String[] args) {
        run("Test 1 - user cannot belong to two teams", HackhubDomainTest::userCannotBelongToTwoTeams);
        run("Test 2 - judge score must be between 0 and 10", HackhubDomainTest::judgeScoreMustBeBetweenZeroAndTen);
        run("Test 3 - submission follows registration deadline", HackhubDomainTest::submissionFollowsRegistrationDeadline);
        run("Test 4 - winner requires all submissions evaluated", HackhubDomainTest::winnerRequiresAllSubmissionsEvaluated);
        run("Test 5 - getter collections are unmodifiable", HackhubDomainTest::getterCollectionsAreUnmodifiable);
        run("Test 6 - unassigned mentor cannot report violations", HackhubDomainTest::unassignedMentorCannotReportViolations);
        run("Test 7 - disqualified team cannot submit or update", HackhubDomainTest::disqualifiedTeamCannotSubmitOrUpdate);
        run("Test 8 - mentor cannot be added during evaluation or after completion", HackhubDomainTest::mentorCannotBeAddedDuringEvaluationOrAfterCompletion);

        System.out.println("All HackHub domain tests passed.");
    }

    private static void userCannotBelongToTwoTeams() {
        User sharedUser = new User("shared", "shared@hackhub.test");
        User secondOwner = new User("owner2", "owner2@hackhub.test");

        new Team("Alpha", sharedUser, 3);
        Team beta = new Team("Beta", secondOwner, 3);

        expectThrows(ValidationException.class, () -> beta.addMember(sharedUser));
    }

    private static void judgeScoreMustBeBetweenZeroAndTen() {
        Organizer organizer = new Organizer("organizer1", "organizer1@hackhub.test");
        Judge judge = new Judge("judge1", "judge1@hackhub.test");
        Hackathon hackathon = newHackathon(organizer);
        organizer.assignStaff(hackathon, judge);

        Team team = new Team("Alpha", new User("alpha-owner", "alpha-owner@hackhub.test"), 3);
        hackathon.registerTeam(team);
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Submission submission = hackathon.submitProject(team, "Project Alpha", "https://example.test/alpha");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);

        expectThrows(ValidationException.class, () ->
                judge.evaluate(hackathon, submission, 11, "Score outside valid range")
        );
    }

    private static void submissionFollowsRegistrationDeadline() {
        Organizer organizer = new Organizer("organizer2", "organizer2@hackhub.test");
        Hackathon hackathon = activeRegistrationHackathon(organizer);
        Team team = new Team("Alpha", new User("alpha-owner-2", "alpha-owner-2@hackhub.test"), 3);

        hackathon.registerTeam(team);
        hackathon.submitProject(team, "Project Alpha", "https://example.test/alpha");

        Hackathon expiredHackathon = expiredRegistrationHackathon(organizer);
        Team expiredTeam = new Team("Beta", new User("beta-owner-2", "beta-owner-2@hackhub.test"), 3);
        expiredHackathon.registerTeam(expiredTeam);

        expectThrows(ValidationException.class, () ->
                expiredHackathon.submitProject(expiredTeam, "Project Beta", "https://example.test/beta")
        );
    }

    private static void winnerRequiresAllSubmissionsEvaluated() {
        Organizer organizer = new Organizer("organizer3", "organizer3@hackhub.test");
        Judge judge = new Judge("judge2", "judge2@hackhub.test");
        Hackathon hackathon = newHackathon(organizer);
        organizer.assignStaff(hackathon, judge);

        Team alpha = new Team("Alpha", new User("alpha-owner-3", "alpha-owner-3@hackhub.test"), 3);
        Team beta = new Team("Beta", new User("beta-owner-3", "beta-owner-3@hackhub.test"), 3);

        hackathon.registerTeam(alpha);
        hackathon.registerTeam(beta);
        hackathon.advanceTo(HackathonStatus.IN_CORSO);

        Submission alphaSubmission = hackathon.submitProject(alpha, "Project Alpha", "https://example.test/alpha");
        hackathon.submitProject(beta, "Project Beta", "https://example.test/beta");

        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);
        judge.evaluate(hackathon, alphaSubmission, 9, "Strong delivery");

        expectThrows(ValidationException.class, () ->
                organizer.proclaimWinner(hackathon, alphaSubmission)
        );
    }

    private static void getterCollectionsAreUnmodifiable() {
        Team team = new Team("Alpha", new User("alpha-owner-4", "alpha-owner-4@hackhub.test"), 3);
        User externalUser = new User("external", "external@hackhub.test");

        expectThrows(UnsupportedOperationException.class, () ->
                team.getMembers().add(externalUser)
        );
    }

    private static void unassignedMentorCannotReportViolations() {
        Organizer organizer = new Organizer("organizer4", "organizer4@hackhub.test");
        Mentor unassignedMentor = new Mentor("mentor1", "mentor1@hackhub.test");
        Hackathon hackathon = newHackathon(organizer);
        Team team = new Team("Alpha", new User("alpha-owner-5", "alpha-owner-5@hackhub.test"), 3);

        hackathon.registerTeam(team);

        expectThrows(ValidationException.class, () ->
                unassignedMentor.reportViolation(hackathon, team, "Suspected rules violation")
        );
    }

    private static void disqualifiedTeamCannotSubmitOrUpdate() {
        Organizer organizer = new Organizer("organizer5", "organizer5@hackhub.test");
        Hackathon hackathon = newHackathon(organizer);

        Team alpha = new Team("Alpha", new User("alpha-owner-6", "alpha-owner-6@hackhub.test"), 3);
        Team beta = new Team("Beta", new User("beta-owner-6", "beta-owner-6@hackhub.test"), 3);

        hackathon.registerTeam(alpha);
        hackathon.registerTeam(beta);
        hackathon.advanceTo(HackathonStatus.IN_CORSO);

        hackathon.submitProject(alpha, "Project Alpha", "https://example.test/alpha");
        hackathon.disqualifyTeam(organizer, alpha);
        hackathon.disqualifyTeam(organizer, beta);

        expectThrows(ValidationException.class, () ->
                hackathon.updateSubmission(alpha, "Project Alpha Updated", "https://example.test/alpha-updated")
        );
        expectThrows(ValidationException.class, () ->
                hackathon.submitProject(beta, "Project Beta", "https://example.test/beta")
        );
    }

    private static void mentorCannotBeAddedDuringEvaluationOrAfterCompletion() {
        Organizer evaluationOrganizer = new Organizer("organizer6", "organizer6@hackhub.test");
        Hackathon inEvaluationHackathon = newHackathon(evaluationOrganizer);
        inEvaluationHackathon.advanceTo(HackathonStatus.IN_CORSO);
        inEvaluationHackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);

        expectThrows(InvalidStateTransitionException.class, () ->
                inEvaluationHackathon.addMentor(new Mentor("mentor2", "mentor2@hackhub.test"))
        );

        Organizer completedOrganizer = new Organizer("organizer7", "organizer7@hackhub.test");
        Hackathon completedHackathon = newHackathon(completedOrganizer);
        completedHackathon.advanceTo(HackathonStatus.IN_CORSO);
        completedHackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);
        completedHackathon.advanceTo(HackathonStatus.CONCLUSO);

        expectThrows(InvalidStateTransitionException.class, () ->
                completedHackathon.addMentor(new Mentor("mentor3", "mentor3@hackhub.test"))
        );
    }

    private static Hackathon newHackathon(Organizer organizer) {
        LocalDate today = LocalDate.now();
        LocalDate registrationStart = today.minusDays(5);
        LocalDate registrationEnd = today.minusDays(1);
        LocalDate hackathonStart = today;
        LocalDate hackathonEnd = today.plusDays(2);
        LocalDate evaluationEnd = today.plusDays(5);

        return new Hackathon(
                "HackHub Test",
                "Laboratorio test",
                "Build a working prototype.",
                registrationStart,
                registrationEnd,
                hackathonStart,
                hackathonEnd,
                evaluationEnd,
                BigDecimal.valueOf(1000),
                4,
                organizer
        );
    }

    private static Hackathon activeRegistrationHackathon(Organizer organizer) {
        LocalDate today = LocalDate.now();

        return new Hackathon(
                "HackHub Registration Test",
                "Laboratorio test",
                "Build a working prototype.",
                today.minusDays(1),
                today.plusDays(1),
                today.plusDays(2),
                today.plusDays(3),
                today.plusDays(5),
                BigDecimal.valueOf(1000),
                4,
                organizer
        );
    }

    private static Hackathon expiredRegistrationHackathon(Organizer organizer) {
        LocalDate today = LocalDate.now();

        return new Hackathon(
                "HackHub Expired Test",
                "Laboratorio test",
                "Build a working prototype.",
                today.minusDays(5),
                today.minusDays(4),
                today.plusDays(1),
                today.plusDays(2),
                today.plusDays(4),
                BigDecimal.valueOf(1000),
                4,
                organizer
        );
    }

    private static void run(String testName, TestCase testCase) {
        try {
            testCase.execute();
            System.out.println("[PASS] " + testName);
        } catch (Throwable throwable) {
            System.err.println("[FAIL] " + testName);
            throwable.printStackTrace();
            System.exit(1);
        }
    }

    private static <T extends Throwable> void expectThrows(Class<T> expectedType, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expectedType.isInstance(actual)) {
                return;
            }
            throw new AssertionError(
                    "Expected " + expectedType.getSimpleName() + " but got " + actual.getClass().getSimpleName(),
                    actual
            );
        }

        throw new AssertionError("Expected " + expectedType.getSimpleName() + " but no exception was thrown");
    }

    @FunctionalInterface
    private interface TestCase {
        void execute();
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Throwable;
    }
}
