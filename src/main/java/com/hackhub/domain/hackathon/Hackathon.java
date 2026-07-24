package com.hackhub.domain.hackathon;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.StaffMember;
import com.hackhub.domain.actor.StaffRole;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.service.PaymentReceipt;
import com.hackhub.domain.service.PaymentService;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import com.hackhub.domain.state.HackathonState;
import com.hackhub.domain.state.InRegistrationState;
import com.hackhub.persistence.HackathonStateConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Entity
@Table(name = "hackathon")
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;
    @Column(length = 2000)
    private String rules;
    private LocalDate registrationStart;
    private LocalDate registrationEnd;
    private LocalDate hackathonStart;
    private LocalDate hackathonEnd;
    private LocalDate evaluationEnd;
    private BigDecimal prizeAmount;
    private int maxTeamSize;

    @Convert(converter = HackathonStateConverter.class)
    private HackathonState state;

    @ManyToMany
    @JoinTable(
            name = "hackathon_registered_teams",
            joinColumns = @JoinColumn(name = "hackathon_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private Set<Team> registeredTeams = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "hackathon_staff_members",
            joinColumns = @JoinColumn(name = "hackathon_id"),
            inverseJoinColumns = @JoinColumn(name = "staff_member_id")
    )
    private Set<StaffMember> staffMembers = new HashSet<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Submission> submissions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "hackathon_id")
    private List<ViolationReport> violationReports = new ArrayList<>();

    @Transient
    private List<CalendarBooking> mentoringCalls = new ArrayList<>();

    @OneToOne
    private Submission winningSubmission;

    @Transient
    private PaymentReceipt prizePaymentReceipt;

    protected Hackathon() {
        // For future ORM mapping.
    }

    public Hackathon(
            String name,
            String location,
            String rules,
            LocalDate registrationStart,
            LocalDate registrationEnd,
            LocalDate hackathonStart,
            LocalDate hackathonEnd,
            LocalDate evaluationEnd,
            BigDecimal prizeAmount,
            int maxTeamSize,
            Organizer organizer
    ) {
        validateDates(registrationStart, registrationEnd, hackathonStart, hackathonEnd, evaluationEnd);
        if (prizeAmount == null || prizeAmount.signum() <= 0) {
            throw new ValidationException("Prize amount must be positive");
        }
        if (maxTeamSize < 1) {
            throw new ValidationException("Max team size must be at least 1");
        }

        this.name = requireText(name, "name");
        this.location = requireText(location, "location");
        this.rules = requireText(rules, "rules");
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.hackathonStart = hackathonStart;
        this.hackathonEnd = hackathonEnd;
        this.evaluationEnd = evaluationEnd;
        this.prizeAmount = prizeAmount;
        this.maxTeamSize = maxTeamSize;
        this.state = new InRegistrationState();
        this.staffMembers.add(Objects.requireNonNull(organizer, "organizer cannot be null"));
    }

    public void registerTeam(Team team) {
        state.registerTeam(this, team);
    }

    public Submission submitProject(Team team, String projectName, String repositoryUrl) {
        return state.submitProject(this, team, projectName, repositoryUrl);
    }

    public Submission updateSubmission(Team team, String projectName, String repositoryUrl) {
        return state.updateSubmission(this, team, projectName, repositoryUrl);
    }

    public void addMentor(Mentor mentor) {
        state.addMentor(this, mentor);
    }

    public CalendarBooking requestSupport(
            Team team,
            Mentor mentor,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return state.requestSupport(this, team, mentor, calendarService, start, end);
    }

    public void evaluateSubmission(Judge judge, Submission submission, int score, String comment) {
        state.evaluateSubmission(this, judge, submission, score, comment);
    }

    public void proclaimWinner(Organizer organizer, Submission winningSubmission) {
        state.proclaimWinner(this, organizer, winningSubmission);
    }

    public PaymentReceipt payPrize(PaymentService paymentService) {
        if (winningSubmission == null) {
            throw new ValidationException("Cannot pay prize before a winner is proclaimed");
        }
        if (prizePaymentReceipt != null) {
            throw new ValidationException("Prize has already been paid");
        }
        prizePaymentReceipt = paymentService.payPrize(
                winningSubmission.getTeam(),
                prizeAmount,
                "Prize for hackathon " + name
        );
        return prizePaymentReceipt;
    }

    public void advanceTo(HackathonStatus nextStatus) {
        state.transitionTo(this, nextStatus);
    }

    public void addStaffMember(Organizer organizer, StaffMember staffMember) {
        requireStaffRole(organizer, StaffRole.ORGANIZER);
        Objects.requireNonNull(staffMember, "staffMember cannot be null");
        staffMembers.add(staffMember);
    }

    public void disqualifyTeam(Organizer organizer, Team team) {
        requireStaffRole(organizer, StaffRole.ORGANIZER);
        requireRegisteredTeam(team);
        team.disqualify();
    }

    public ViolationReport reportViolation(Mentor mentor, Team team, String description) {
        requireStaffRole(mentor, StaffRole.MENTOR);
        requireRegisteredTeam(team);

        ViolationReport report = new ViolationReport(mentor, team, description);
        violationReports.add(report);
        return report;
    }

    public void registerTeamInternal(Team team) {
        Objects.requireNonNull(team, "team cannot be null");
        if (team.getMaxSize() > maxTeamSize) {
            throw new ValidationException("Team max size exceeds hackathon max team size");
        }
        registeredTeams.add(team);
    }

    public void addMentorInternal(Mentor mentor) {
        Objects.requireNonNull(mentor, "mentor cannot be null");
        staffMembers.add(mentor);
        mentor.assignTo(this);
    }

    public Submission submitProjectInternal(Team team, String projectName, String repositoryUrl) {
        requireSubmissionWindow();
        requireRegisteredTeam(team);
        team.ensureCanSubmit();
        if (findSubmissionByTeam(team).isPresent()) {
            throw new ValidationException("Team has already submitted a project");
        }

        Submission submission = new Submission(this, team, projectName, repositoryUrl);
        submissions.add(submission);
        return submission;
    }

    public Submission updateSubmissionInternal(Team team, String projectName, String repositoryUrl) {
        requireSubmissionWindow();
        requireRegisteredTeam(team);
        team.ensureCanSubmit();

        Submission submission = findSubmissionByTeam(team)
                .orElseThrow(() -> new ValidationException("Team has no submission to update"));
        submission.update(projectName, repositoryUrl);
        return submission;
    }

    private void requireSubmissionWindow() {
        LocalDate today = LocalDate.now();
        HackathonStatus status = getStatus();

        if (status == HackathonStatus.IN_ISCRIZIONE) {
            if (today.isBefore(registrationStart) || today.isAfter(registrationEnd)) {
                throw new ValidationException("Submission is allowed during the registration period only");
            }
            return;
        }

        if (status == HackathonStatus.IN_CORSO) {
            if (today.isBefore(hackathonStart) || today.isAfter(hackathonEnd)) {
                throw new ValidationException("Submission is allowed during the hackathon period only");
            }
            return;
        }

        throw new ValidationException("Submission is allowed only while hackathon is in registration or in progress");
    }

    public CalendarBooking requestSupportInternal(
            Team team,
            Mentor mentor,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    ) {
        requireRegisteredTeam(team);
        requireStaffRole(mentor, StaffRole.MENTOR);

        CalendarBooking booking = mentor.proposeCall(team, calendarService, start, end);
        mentoringCalls.add(booking);
        return booking;
    }

    public void evaluateSubmissionInternal(Judge judge, Submission submission, int score, String comment) {
        requireStaffRole(judge, StaffRole.JUDGE);
        requireSubmissionBelongsToHackathon(submission);
        submission.addEvaluation(judge, score, comment);
    }

    public void proclaimWinnerInternal(Organizer organizer, Submission winningSubmission) {
        requireStaffRole(organizer, StaffRole.ORGANIZER);
        requireSubmissionBelongsToHackathon(winningSubmission);
        requireAllSubmissionsEvaluated();
        this.winningSubmission = winningSubmission;
    }

    public void changeStateInternal(HackathonState newState) {
        this.state = Objects.requireNonNull(newState, "newState cannot be null");
    }

    private void requireRegisteredTeam(Team team) {
        if (!registeredTeams.contains(team)) {
            throw new ValidationException("Team is not registered for this hackathon");
        }
    }

    private void requireSubmissionBelongsToHackathon(Submission submission) {
        if (!submissions.contains(submission) || !Objects.equals(submission.getHackathon(), this)) {
            throw new ValidationException("Submission does not belong to this hackathon");
        }
    }

    private void requireStaffRole(StaffMember staffMember, StaffRole requiredRole) {
        if (!staffMembers.contains(staffMember) || staffMember.getRole() != requiredRole) {
            throw new ValidationException("Required staff role: " + requiredRole);
        }
    }

    private Optional<Submission> findSubmissionByTeam(Team team) {
        return submissions.stream()
                .filter(submission -> submission.getTeam().equals(team))
                .findFirst();
    }

    private void requireAllSubmissionsEvaluated() {
        boolean hasUnevaluatedSubmission = submissions.stream()
                .anyMatch(submission -> submission.getEvaluations().isEmpty());

        if (hasUnevaluatedSubmission) {
            throw new ValidationException("Cannot proclaim a winner while there are unevaluated submissions");
        }
    }

    private static void validateDates(
            LocalDate registrationStart,
            LocalDate registrationEnd,
            LocalDate hackathonStart,
            LocalDate hackathonEnd,
            LocalDate evaluationEnd
    ) {
        Objects.requireNonNull(registrationStart, "registrationStart cannot be null");
        Objects.requireNonNull(registrationEnd, "registrationEnd cannot be null");
        Objects.requireNonNull(hackathonStart, "hackathonStart cannot be null");
        Objects.requireNonNull(hackathonEnd, "hackathonEnd cannot be null");
        Objects.requireNonNull(evaluationEnd, "evaluationEnd cannot be null");

        if (registrationEnd.isBefore(registrationStart)) {
            throw new ValidationException("Registration end cannot be before registration start");
        }
        if (hackathonStart.isBefore(registrationEnd)) {
            throw new ValidationException("Hackathon start cannot be before registration end");
        }
        if (hackathonEnd.isBefore(hackathonStart)) {
            throw new ValidationException("Hackathon end cannot be before hackathon start");
        }
        if (evaluationEnd.isBefore(hackathonEnd)) {
            throw new ValidationException("Evaluation end cannot be before hackathon end");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getRules() {
        return rules;
    }

    public LocalDate getRegistrationStart() {
        return registrationStart;
    }

    public LocalDate getRegistrationEnd() {
        return registrationEnd;
    }

    public LocalDate getHackathonStart() {
        return hackathonStart;
    }

    public LocalDate getHackathonEnd() {
        return hackathonEnd;
    }

    public LocalDate getEvaluationEnd() {
        return evaluationEnd;
    }

    public BigDecimal getPrizeAmount() {
        return prizeAmount;
    }

    public int getMaxTeamSize() {
        return maxTeamSize;
    }

    public HackathonStatus getStatus() {
        return state.getStatus();
    }

    public Set<Team> getRegisteredTeams() {
        return Collections.unmodifiableSet(registeredTeams);
    }

    public Set<StaffMember> getStaffMembers() {
        return Collections.unmodifiableSet(staffMembers);
    }

    public List<Submission> getSubmissions() {
        return Collections.unmodifiableList(submissions);
    }

    public List<ViolationReport> getViolationReports() {
        return Collections.unmodifiableList(violationReports);
    }

    public List<CalendarBooking> getMentoringCalls() {
        return Collections.unmodifiableList(mentoringCalls);
    }

    public Submission getWinningSubmission() {
        return winningSubmission;
    }

    public PaymentReceipt getPrizePaymentReceipt() {
        return prizePaymentReceipt;
    }
}
