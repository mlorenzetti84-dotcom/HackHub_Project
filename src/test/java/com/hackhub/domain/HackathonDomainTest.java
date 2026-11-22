package com.hackhub.domain;

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
import com.hackhub.domain.service.FakePaymentService;
import com.hackhub.domain.service.InMemoryCalendarService;
import com.hackhub.domain.support.SupportRequest;
import com.hackhub.domain.support.SupportRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HackathonDomainTest {

    private Organizer organizer;
    private Judge judge;
    private Mentor mentor;
    private User member;
    private Team team;
    private Hackathon hackathon;

    @BeforeEach
    void setUp() {
        organizer = new Organizer("organizer", "organizer@example.org");
        judge = new Judge("judge", "judge@example.org");
        mentor = new Mentor("mentor", "mentor@example.org");
        member = new User("member", "member@example.org");
        team = new Team("Team One", member, 3);

        LocalDate today = LocalDate.now();
        hackathon = new Hackathon(
                "HackHub Test", "Camerino", "Rules",
                today.minusDays(3), today,
                today, today.plusDays(2), today.plusDays(4),
                BigDecimal.valueOf(1000), 3, organizer
        );
        hackathon.addStaffMember(organizer, judge);
        hackathon.addMentor(mentor);
        hackathon.registerTeam(team);
    }

    @Test
    void teamCannotRemoveItsLastMember() {
        assertThrows(ValidationException.class, () -> team.removeMember(member));

        assertEquals(1, team.getMembers().size());
        assertTrue(team.hasMember(member));
        assertSame(team, member.getCurrentTeam());
    }

    @Test
    void teamCanRemoveMemberWhenAnotherMemberRemains() {
        User secondMember = new User("second", "second@example.org");
        team.addMember(secondMember);

        assertDoesNotThrow(() -> team.removeMember(secondMember));

        assertEquals(1, team.getMembers().size());
        assertFalse(team.hasMember(secondMember));
        assertNull(secondMember.getCurrentTeam());
        assertSame(team, member.getCurrentTeam());
    }

    @Test
    void teamMembersCollectionIsReadOnly() {
        assertThrows(UnsupportedOperationException.class,
                () -> team.getMembers().add(new User("external", "external@example.org")));
    }

    @Test
    void unassignedMentorCannotReportViolationsDuringHackathon() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Mentor outsider = new Mentor("outsider", "outsider@example.org");
        assertThrows(ValidationException.class,
                () -> hackathon.reportViolation(outsider, team, "Violation"));
    }

    @Test
    void disqualifiedTeamCannotUpdateAnExistingSubmission() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        hackathon.submitProject(team, "Project", "https://example.org/initial");
        hackathon.disqualifyTeam(organizer, team);
        assertThrows(ValidationException.class,
                () -> hackathon.updateSubmission(team, "Project updated", "https://example.org/updated"));
    }

    @Test
    void mentorCannotBeAddedDuringEvaluationOrAfterCompletion() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Submission submission = hackathon.submitProject(team, "Project", "https://example.org/project");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);
        assertThrows(InvalidStateTransitionException.class,
                () -> hackathon.addMentor(new Mentor("late1", "late1@example.org")));

        hackathon.evaluateSubmission(judge, submission, 9, "Good project");
        hackathon.proclaimWinner(organizer, submission);
        assertEquals(HackathonStatus.CONCLUSO, hackathon.getStatus());
        assertThrows(InvalidStateTransitionException.class,
                () -> hackathon.addMentor(new Mentor("late2", "late2@example.org")));
    }

    @Test
    void submissionBeforeHackathonStartIsRejected() {
        LocalDate today = LocalDate.now();
        Hackathon future = new Hackathon(
                "Future hackathon", "Camerino", "Rules",
                today.minusDays(1), today.plusDays(1),
                today.plusDays(2), today.plusDays(4), today.plusDays(6),
                BigDecimal.valueOf(100), 3, organizer
        );
        future.registerTeam(team);
        future.advanceTo(HackathonStatus.IN_CORSO);
        assertThrows(ValidationException.class,
                () -> future.submitProject(team, "Project", "https://example.org/project"));
    }

    @Test
    void userCannotBelongToTwoTeams() {
        assertThrows(ValidationException.class, () -> new Team("Other", member, 3));
    }

    @Test
    void invalidStateTransitionIsRejected() {
        assertThrows(
                InvalidStateTransitionException.class,
                () -> hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE)
        );
    }

    @Test
    void teamCannotBeRegisteredTwice() {
        assertThrows(ValidationException.class, () -> hackathon.registerTeam(team));
    }

    @Test
    void teamCannotRegisterBeforeRegistrationOpens() {
        LocalDate today = LocalDate.now();
        Hackathon upcoming = new Hackathon(
                "Future hackathon", "Camerino", "Rules",
                today.plusDays(1), today.plusDays(2), today.plusDays(3), today.plusDays(4), today.plusDays(5),
                BigDecimal.valueOf(100), 3, organizer
        );
        assertThrows(ValidationException.class, () -> upcoming.registerTeam(team));
    }

    @Test
    void teamCannotRegisterAfterRegistrationCloses() {
        LocalDate today = LocalDate.now();
        Hackathon expired = new Hackathon(
                "Past hackathon", "Camerino", "Rules",
                today.minusDays(4), today.minusDays(2), today.minusDays(1), today, today.plusDays(1),
                BigDecimal.valueOf(100), 3, organizer
        );
        assertThrows(ValidationException.class, () -> expired.registerTeam(team));
    }

    @Test
    void violationCanBeReportedOnlyWhileHackathonIsInProgress() {
        assertThrows(
                ValidationException.class,
                () -> hackathon.reportViolation(mentor, team, "Rule violation")
        );

        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        assertNotNull(hackathon.reportViolation(mentor, team, "Rule violation"));
    }

    @Test
    void disqualifiedTeamCannotSubmit() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        hackathon.disqualifyTeam(organizer, team);
        assertThrows(
                ValidationException.class,
                () -> hackathon.submitProject(team, "Project", "https://example.org/project")
        );
    }

    @Test
    void teamCanBeDisqualifiedOnlyWhileHackathonIsInProgress() {
        assertThrows(ValidationException.class, () -> hackathon.disqualifyTeam(organizer, team));
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        hackathon.disqualifyTeam(organizer, team);
        assertTrue(team.isDisqualified());
    }

    @Test
    void supportCallCannotBeProposedAfterHackathonLeavesInProgressState() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        SupportRequest request = hackathon.createSupportRequest(team, member, "Architecture review");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        assertThrows(ValidationException.class, () -> hackathon.proposeCall(
                request, mentor, new InMemoryCalendarService(), start, start.plusHours(1)
        ));
    }

    @Test
    void registrationChecksActualTeamSizeRatherThanConfiguredCapacity() {
        User flexibleMember = new User("flexible", "flexible@example.org");
        Team flexibleTeam = new Team("Flexible Team", flexibleMember, 10);

        assertDoesNotThrow(() -> hackathon.registerTeam(flexibleTeam));
    }

    @Test
    void supportRequestMustComeFromRegisteredTeamMember() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        User outsider = new User("outsider", "outsider@example.org");

        assertThrows(
                ValidationException.class,
                () -> hackathon.createSupportRequest(team, outsider, "Need help")
        );
    }

    @Test
    void assignedMentorCanProposeCallForOpenSupportRequest() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        SupportRequest request = hackathon.createSupportRequest(team, member, "Architecture review");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        var booking = hackathon.proposeCall(
                request, mentor, new InMemoryCalendarService(), start, start.plusHours(1)
        );

        assertNotNull(booking.getExternalReference());
        assertEquals(SupportRequestStatus.CALL_PROPOSED, request.getStatus());
        assertEquals(mentor, request.getAssignedMentor());
    }

    @Test
    void calendarRejectsOverlappingCallsForTheSameMentor() {
        InMemoryCalendarService calendar = new InMemoryCalendarService();
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        calendar.bookMentoringCall(team, mentor, start, start.plusHours(1));

        assertThrows(
                IllegalArgumentException.class,
                () -> calendar.bookMentoringCall(
                        team, mentor, start.plusMinutes(30), start.plusHours(2)
                )
        );
    }

    @Test
    void scoreMustBeBetweenZeroAndTen() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Submission submission = hackathon.submitProject(team, "Project", "https://example.org/project");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);

        assertThrows(
                ValidationException.class,
                () -> hackathon.evaluateSubmission(judge, submission, 11, "Invalid score")
        );
    }

    @Test
    void winnerRequiresAllSubmissionsToBeEvaluated() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Submission submission = hackathon.submitProject(team, "Project", "https://example.org/project");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);

        assertThrows(ValidationException.class, () -> hackathon.proclaimWinner(organizer, submission));
    }

    @Test
    void completeLifecycleAllowsOnePrizePayment() {
        hackathon.advanceTo(HackathonStatus.IN_CORSO);
        Submission submission = hackathon.submitProject(team, "Project", "https://example.org/project");
        hackathon.advanceTo(HackathonStatus.IN_VALUTAZIONE);
        hackathon.evaluateSubmission(judge, submission, 9, "Very good");
        hackathon.proclaimWinner(organizer, submission);

        assertEquals(HackathonStatus.CONCLUSO, hackathon.getStatus());
        assertSame(submission, hackathon.getWinningSubmission());
        assertNotNull(hackathon.payPrize(new FakePaymentService()));
        assertThrows(ValidationException.class, () -> hackathon.payPrize(new FakePaymentService()));
    }

    @Test
    void exposedCollectionsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> hackathon.getRegisteredTeams().clear());
        assertThrows(UnsupportedOperationException.class, () -> hackathon.getStaffMembers().clear());
    }
}
