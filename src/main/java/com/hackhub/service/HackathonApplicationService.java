package com.hackhub.service;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Invitation;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;
import com.hackhub.domain.hackathon.ViolationReport;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import com.hackhub.domain.service.PaymentReceipt;
import com.hackhub.domain.service.PaymentService;
import com.hackhub.dto.CalendarBookingDto;
import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.InvitationDto;
import com.hackhub.dto.PrizePaymentDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.dto.TeamDto;
import com.hackhub.dto.ViolationReportDto;
import com.hackhub.repository.HackathonRepository;
import com.hackhub.repository.InvitationRepository;
import com.hackhub.repository.SubmissionRepository;
import com.hackhub.repository.TeamRepository;
import com.hackhub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class HackathonApplicationService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SubmissionRepository submissionRepository;
    private final InvitationRepository invitationRepository;
    private final CalendarService calendarService;
    private final PaymentService paymentService;

    public HackathonApplicationService(
            HackathonRepository hackathonRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            SubmissionRepository submissionRepository,
            InvitationRepository invitationRepository,
            CalendarService calendarService,
            PaymentService paymentService
    ) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.submissionRepository = submissionRepository;
        this.invitationRepository = invitationRepository;
        this.calendarService = calendarService;
        this.paymentService = paymentService;
    }

    public Hackathon createHackathon(
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
            Long organizerId
    ) {
        Organizer organizer = findOrganizer(organizerId);
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
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto createHackathonDto(
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
            Long organizerId
    ) {
        return HackathonDto.from(createHackathon(
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
                organizerId
        ));
    }

    public Hackathon registerTeamToHackathon(Long hackathonId, Long teamId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);

        hackathon.registerTeam(team);
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto registerTeamToHackathonDto(Long hackathonId, Long teamId) {
        return HackathonDto.from(registerTeamToHackathon(hackathonId, teamId));
    }

    public HackathonDto registerTeamToHackathonDto(Long hackathonId, Long teamId, Long memberId) {
        Team team = findTeam(teamId);
        User member = findUser(memberId);
        requireTeamMember(team, member);
        return HackathonDto.from(registerTeamToHackathon(hackathonId, teamId));
    }

    public Submission submitProject(Long hackathonId, Long teamId, String content) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);

        Submission submission = hackathon.submitProject(team, "Project submission", content);
        hackathonRepository.save(hackathon);
        return submission;
    }

    public SubmissionDto submitProjectDto(Long hackathonId, Long teamId, String content) {
        return SubmissionDto.from(submitProject(hackathonId, teamId, content));
    }

    public SubmissionDto submitProjectDto(Long hackathonId, Long teamId, Long memberId, String content) {
        Team team = findTeam(teamId);
        User member = findUser(memberId);
        requireTeamMember(team, member);
        return SubmissionDto.from(submitProject(hackathonId, teamId, content));
    }

    public Submission updateSubmission(Long hackathonId, Long teamId, String content) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);

        Submission submission = hackathon.updateSubmission(team, "Project submission", content);
        hackathonRepository.save(hackathon);
        return submission;
    }

    public SubmissionDto updateSubmissionDto(Long hackathonId, Long teamId, String content) {
        return SubmissionDto.from(updateSubmission(hackathonId, teamId, content));
    }

    public SubmissionDto updateSubmissionDto(Long hackathonId, Long teamId, Long memberId, String content) {
        Team team = findTeam(teamId);
        User member = findUser(memberId);
        requireTeamMember(team, member);
        return SubmissionDto.from(updateSubmission(hackathonId, teamId, content));
    }

    public Submission evaluateSubmission(
            Long hackathonId,
            Long judgeId,
            Long submissionId,
            int score,
            String comment
    ) {
        Hackathon hackathon = findHackathon(hackathonId);
        Judge judge = findJudge(judgeId);
        Submission submission = findSubmission(submissionId);

        hackathon.evaluateSubmission(judge, submission, score, comment);
        hackathonRepository.save(hackathon);
        return submission;
    }

    public SubmissionDto evaluateSubmissionDto(
            Long hackathonId,
            Long judgeId,
            Long submissionId,
            int score,
            String comment
    ) {
        return SubmissionDto.from(evaluateSubmission(hackathonId, judgeId, submissionId, score, comment));
    }

    public Hackathon disqualifyTeam(Long hackathonId, Long organizerId, Long teamId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Organizer organizer = findOrganizer(organizerId);
        Team team = findTeam(teamId);

        hackathon.disqualifyTeam(organizer, team);
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto disqualifyTeamDto(Long hackathonId, Long organizerId, Long teamId) {
        return HackathonDto.from(disqualifyTeam(hackathonId, organizerId, teamId));
    }

    public Hackathon assignMentor(Long hackathonId, Long mentorId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Mentor mentor = findMentor(mentorId);

        hackathon.addMentor(mentor);
        return hackathonRepository.save(hackathon);
    }

    public CalendarBooking proposeMentorCall(Long hackathonId, Long mentorId, Long teamId) {
        return proposeMentorCall(
                hackathonId,
                mentorId,
                teamId,
                LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0),
                LocalDateTime.now().plusDays(1).plusHours(1).withMinute(0).withSecond(0).withNano(0)
        );
    }

    public CalendarBooking proposeMentorCall(
            Long hackathonId,
            Long mentorId,
            Long teamId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        Hackathon hackathon = findHackathon(hackathonId);
        Mentor mentor = findMentor(mentorId);
        Team team = findTeam(teamId);

        CalendarBooking booking = hackathon.requestSupport(
                team,
                mentor,
                calendarService,
                start,
                end
        );
        hackathonRepository.save(hackathon);

        System.out.println("Strategy Pattern CalendarService invoked: " + booking.getExternalReference());
        return booking;
    }

    public CalendarBookingDto proposeMentorCallDto(
            Long hackathonId,
            Long mentorId,
            Long teamId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return CalendarBookingDto.from(proposeMentorCall(hackathonId, mentorId, teamId, start, end));
    }

    public ViolationReport reportViolation(Long hackathonId, Long mentorId, Long teamId, String description) {
        Hackathon hackathon = findHackathon(hackathonId);
        Mentor mentor = findMentor(mentorId);
        Team team = findTeam(teamId);

        ViolationReport report = hackathon.reportViolation(mentor, team, description);
        hackathonRepository.save(hackathon);
        return report;
    }

    public ViolationReportDto reportViolationDto(Long hackathonId, Long mentorId, Long teamId, String description) {
        return ViolationReportDto.from(reportViolation(hackathonId, mentorId, teamId, description));
    }

    public Hackathon proclaimWinner(Long hackathonId, Long organizerId, Long teamId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Organizer organizer = findOrganizer(organizerId);
        Team team = findTeam(teamId);

        Submission winningSubmission = hackathon.getSubmissions().stream()
                .filter(submission -> submission.getTeam().equals(team))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Team has no submission for this hackathon"));

        hackathon.proclaimWinner(organizer, winningSubmission);
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto proclaimWinnerDto(Long hackathonId, Long organizerId, Long teamId) {
        return HackathonDto.from(proclaimWinner(hackathonId, organizerId, teamId));
    }

    public PrizePaymentResult proclaimWinnerAndPayPrize(Long hackathonId, Long organizerId, Long teamId) {
        Hackathon hackathon = proclaimWinner(hackathonId, organizerId, teamId);
        PaymentReceipt receipt = hackathon.payPrize(paymentService);
        hackathonRepository.save(hackathon);

        System.out.println("Strategy Pattern PaymentService invoked: "
                + receipt.getExternalReference()
                + " amount=" + receipt.getAmount());

        return new PrizePaymentResult(hackathon.getWinningSubmission().getTeam().getId(), receipt.getExternalReference());
    }

    public PrizePaymentDto proclaimWinnerAndPayPrizeDto(Long hackathonId, Long organizerId, Long teamId) {
        PrizePaymentResult result = proclaimWinnerAndPayPrize(hackathonId, organizerId, teamId);
        return new PrizePaymentDto(result.winningTeamId(), result.paymentReference());
    }

    public Hackathon advanceHackathon(Long hackathonId, HackathonStatus nextStatus) {
        Hackathon hackathon = findHackathon(hackathonId);
        hackathon.advanceTo(nextStatus);
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto advanceHackathonDto(Long hackathonId, HackathonStatus nextStatus) {
        return HackathonDto.from(advanceHackathon(hackathonId, nextStatus));
    }

    public Hackathon createDemoHackathonIfMissing() {
        Optional<Hackathon> existingHackathon = hackathonRepository.findAll().stream()
                .min(Comparator.comparing(Hackathon::getId));

        if (existingHackathon.isPresent()) {
            return existingHackathon.get();
        }

        Hackathon hackathon = createHackathon(
                "CodeFest Camerino 2026: Sostenibilita Digitale",
                "Polo Informatico - Laboratorio Alan Turing, Camerino",
                "Hackathon universitario presso il Polo Informatico - Laboratorio Alan Turing.",
                LocalDate.of(2026, 3, 2),
                LocalDate.of(2026, 3, 9),
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 3, 12),
                LocalDate.of(2026, 3, 15),
                BigDecimal.valueOf(2500),
                3,
                1L
        );

        hackathon.addStaffMember(findOrganizer(1L), findJudge(2L));
        hackathon.addMentor(findMentor(3L));
        return hackathonRepository.save(hackathon);
    }

    public TeamDto createTeam(Long ownerId, String name, int maxSize) {
        User owner = findUser(ownerId);
        Team team = teamRepository.save(new Team(name, owner, maxSize));
        return TeamDto.from(team);
    }

    public InvitationDto inviteUserToTeam(Long invitedById, Long invitedUserId, Long teamId) {
        User invitedBy = findUser(invitedById);
        User invitedUser = findUser(invitedUserId);
        Team team = findTeam(teamId);

        Invitation invitation = invitedBy.inviteToTeam(invitedUser, team);
        return InvitationDto.from(invitationRepository.save(invitation));
    }

    public TeamDto acceptTeamInvitation(UUID invitationId, Long acceptingUserId) {
        Invitation invitation = findInvitation(invitationId);
        User acceptingUser = findUser(acceptingUserId);

        acceptingUser.acceptInvitation(invitation);
        invitationRepository.save(invitation);
        return TeamDto.from(teamRepository.save(invitation.getTeam()));
    }

    @Transactional(readOnly = true)
    public DashboardView getDashboard(Long hackathonId, Long teamId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);

        Optional<Submission> submission = hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getTeam().equals(team))
                .findFirst();
        long unevaluatedSubmissions = hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getEvaluations().isEmpty())
                .count();
        Optional<Submission> nextUnevaluatedSubmission = hackathon.getSubmissions().stream()
                .filter(candidate -> candidate.getEvaluations().isEmpty())
                .findFirst();

        return new DashboardView(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getLocation(),
                hackathon.getRules(),
                hackathon.getStatus(),
                hackathon.getRegisteredTeams().size(),
                hackathon.getSubmissions().size(),
                hackathon.getRegisteredTeams().contains(team),
                team.isDisqualified(),
                submission.map(Submission::getId).orElse(null),
                submission.map(Submission::getRepositoryUrl).orElse(""),
                submission.map(value -> value.getEvaluations().size()).orElse(0),
                submission.map(Submission::averageScore).orElse(0.0),
                (int) unevaluatedSubmissions,
                hackathon.getViolationReports().size(),
                nextUnevaluatedSubmission.map(Submission::getId).orElse(null),
                nextUnevaluatedSubmission.map(value -> value.getTeam().getName()).orElse(""),
                nextUnevaluatedSubmission.map(Submission::getRepositoryUrl).orElse(""),
                hackathon.getWinningSubmission() == null ? null : hackathon.getWinningSubmission().getTeam().getId()
        );
    }

    @Transactional(readOnly = true)
    public Hackathon getHackathon(Long hackathonId) {
        return findHackathon(hackathonId);
    }

    @Transactional(readOnly = true)
    public HackathonDto getHackathonDto(Long hackathonId) {
        return HackathonDto.from(findHackathon(hackathonId));
    }

    @Transactional(readOnly = true)
    public List<HackathonDto> listHackathons() {
        return hackathonRepository.findAll().stream()
                .map(HackathonDto::from)
                .toList();
    }

    private Hackathon findHackathon(Long id) {
        return hackathonRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hackathon not found: " + id));
    }

    private Team findTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Team not found: " + id));
    }

    private Submission findSubmission(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + id));
    }

    private Invitation findInvitation(UUID id) {
        return invitationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found: " + id));
    }

    private void requireTeamMember(Team team, User user) {
        if (!team.hasMember(user)) {
            throw new ValidationException("User does not belong to team: " + user.getId());
        }
    }

    private Organizer findOrganizer(Long id) {
        User user = findUser(id);
        if (user instanceof Organizer organizer) {
            return organizer;
        }
        throw new ValidationException("User is not an organizer: " + id);
    }

    private Judge findJudge(Long id) {
        User user = findUser(id);
        if (user instanceof Judge judge) {
            return judge;
        }
        throw new ValidationException("User is not a judge: " + id);
    }

    private Mentor findMentor(Long id) {
        User user = findUser(id);
        if (user instanceof Mentor mentor) {
            return mentor;
        }
        throw new ValidationException("User is not a mentor: " + id);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    public record DashboardView(
            Long hackathonId,
            String hackathonName,
            String location,
            String rules,
            HackathonStatus status,
            int registeredTeams,
            int submissions,
            boolean teamRegistered,
            boolean teamDisqualified,
            Long submissionId,
            String submissionContent,
            int evaluations,
            double averageScore,
            int unevaluatedSubmissions,
            int violationReports,
            Long nextUnevaluatedSubmissionId,
            String nextUnevaluatedTeamName,
            String nextUnevaluatedSubmissionContent,
            Long winningTeamId
    ) {
    }

    public record PrizePaymentResult(Long winningTeamId, String paymentReference) {
    }
}
