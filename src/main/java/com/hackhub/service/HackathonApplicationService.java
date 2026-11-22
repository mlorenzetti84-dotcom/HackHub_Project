package com.hackhub.service;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Invitation;
import com.hackhub.domain.actor.InvitationStatus;
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
import com.hackhub.domain.support.SupportRequest;
import com.hackhub.dto.CalendarBookingDto;
import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.InvitationDto;
import com.hackhub.dto.PrizePaymentDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.dto.SupportRequestDto;
import com.hackhub.dto.TeamDto;
import com.hackhub.dto.ViolationReportDto;
import com.hackhub.repository.HackathonRepository;
import com.hackhub.repository.InvitationRepository;
import com.hackhub.repository.SubmissionRepository;
import com.hackhub.repository.SupportRequestRepository;
import com.hackhub.repository.TeamRepository;
import com.hackhub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HackathonApplicationService {

    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SubmissionRepository submissionRepository;
    private final InvitationRepository invitationRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final CalendarService calendarService;
    private final PaymentService paymentService;

    public HackathonApplicationService(
            HackathonRepository hackathonRepository,
            UserRepository userRepository,
            TeamRepository teamRepository,
            SubmissionRepository submissionRepository,
            InvitationRepository invitationRepository,
            SupportRequestRepository supportRequestRepository,
            CalendarService calendarService,
            PaymentService paymentService
    ) {
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.submissionRepository = submissionRepository;
        this.invitationRepository = invitationRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.calendarService = calendarService;
        this.paymentService = paymentService;
    }

    public HackathonDto createHackathonWithStaffDto(
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
            Long organizerId,
            Long judgeId,
            List<Long> mentorIds
    ) {
        if (mentorIds == null || mentorIds.isEmpty()) {
            throw new ValidationException("At least one mentor is required");
        }
        Organizer organizer = findOrganizer(organizerId);
        Judge judge = findJudge(judgeId);
        Hackathon hackathon = new Hackathon(
                name, location, rules, registrationStart, registrationEnd,
                hackathonStart, hackathonEnd, evaluationEnd, prizeAmount, maxTeamSize, organizer
        );
        hackathon.addStaffMember(organizer, judge);
        mentorIds.stream().distinct().map(this::findMentor).forEach(hackathon::addMentor);
        return HackathonDto.from(hackathonRepository.save(hackathon));
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

    public HackathonDto assignMentorDto(Long hackathonId, Long organizerId, Long mentorId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Organizer organizer = findOrganizer(organizerId);
        if (!hackathon.hasAssignedStaff(organizer)) {
            throw new ValidationException("Organizer is not assigned to this hackathon");
        }
        Mentor mentor = findMentor(mentorId);
        hackathon.addMentor(mentor);
        return HackathonDto.from(hackathonRepository.save(hackathon));
    }

    public SupportRequestDto createSupportRequestDto(
            Long hackathonId,
            Long teamId,
            Long memberId,
            String description
    ) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);
        User member = findUser(memberId);
        requireTeamMember(team, member);
        SupportRequest request = hackathon.createSupportRequest(team, member, description);
        hackathonRepository.save(hackathon);
        return SupportRequestDto.from(supportRequestRepository.save(request));
    }

    public SupportRequestDto cancelSupportRequestDto(
            Long hackathonId, Long teamId, Long requestId, Long memberId
    ) {
        Hackathon hackathon = findHackathon(hackathonId);
        Team team = findTeam(teamId);
        User member = findUser(memberId);
        requireTeamMember(team, member);
        SupportRequest request = findSupportRequest(requestId);
        if (!request.getHackathon().equals(hackathon) || !request.getTeam().equals(team)) {
            throw new ValidationException("Support request does not belong to the specified hackathon and team");
        }
        request.cancel(member);
        return SupportRequestDto.from(supportRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public List<SupportRequestDto> listSupportRequestsForMentor(Long hackathonId, Long mentorId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Mentor mentor = findMentor(mentorId);
        if (!hackathon.hasAssignedStaff(mentor)) {
            throw new ValidationException("Mentor is not assigned to this hackathon");
        }
        return supportRequestRepository.findByHackathonIdOrderByRequestedAtAsc(hackathonId).stream()
                .map(SupportRequestDto::from)
                .toList();
    }

    public CalendarBookingDto proposeCallForSupportRequestDto(
            Long hackathonId,
            Long requestId,
            Long mentorId,
            LocalDateTime start,
            LocalDateTime end
    ) {
        Hackathon hackathon = findHackathon(hackathonId);
        Mentor mentor = findMentor(mentorId);
        SupportRequest request = findSupportRequest(requestId);
        CalendarBooking booking = hackathon.proposeCall(request, mentor, calendarService, start, end);
        supportRequestRepository.save(request);
        hackathonRepository.save(hackathon);
        return CalendarBookingDto.from(booking);
    }

    @Transactional(readOnly = true)
    public List<SubmissionDto> listSubmissionsForStaff(Long hackathonId, Long staffId) {
        Hackathon hackathon = findHackathon(hackathonId);
        User user = findUser(staffId);
        if (!(user instanceof com.hackhub.domain.actor.StaffMember staffMember)
                || !hackathon.hasAssignedStaff(staffMember)) {
            throw new ValidationException("Staff member is not assigned to this hackathon");
        }
        return hackathon.getSubmissions().stream().map(SubmissionDto::from).toList();
    }

    @Transactional(readOnly = true)
    public SubmissionDto getSubmissionForStaff(Long hackathonId, Long submissionId, Long staffId) {
        return listSubmissionsForStaff(hackathonId, staffId).stream()
                .filter(submission -> submission.id().equals(submissionId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Submission not found: " + submissionId));
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

    public PrizePaymentResult payPrize(Long hackathonId, Long organizerId, Long teamId) {
        Hackathon hackathon = findHackathon(hackathonId);
        Organizer organizer = findOrganizer(organizerId);
        if (!hackathon.hasAssignedStaff(organizer)) {
            throw new ValidationException("Organizer is not assigned to this hackathon");
        }
        return payPrizeForWinner(hackathon, teamId);
    }

    public PrizePaymentDto payPrizeDto(Long hackathonId, Long organizerId, Long teamId) {
        PrizePaymentResult result = payPrize(hackathonId, organizerId, teamId);
        return new PrizePaymentDto(result.winningTeamId(), result.paymentReference());
    }

    private PrizePaymentResult payPrizeForWinner(Hackathon hackathon, Long expectedWinningTeamId) {
        Submission winner = hackathon.getWinningSubmission();
        if (winner == null) {
            throw new ValidationException("Cannot pay prize before a winner is proclaimed");
        }
        if (!winner.getTeam().getId().equals(expectedWinningTeamId)) {
            throw new ValidationException("The specified team is not the proclaimed winner");
        }
        PaymentReceipt receipt = hackathon.payPrize(paymentService);
        hackathonRepository.save(hackathon);
        return new PrizePaymentResult(winner.getTeam().getId(), receipt.getExternalReference());
    }

    public Hackathon advanceHackathon(Long hackathonId, HackathonStatus nextStatus) {
        Hackathon hackathon = findHackathon(hackathonId);
        hackathon.advanceTo(nextStatus);
        return hackathonRepository.save(hackathon);
    }

    public HackathonDto advanceHackathonDto(Long hackathonId, HackathonStatus nextStatus) {
        return HackathonDto.from(advanceHackathon(hackathonId, nextStatus));
    }

    public HackathonDto advanceHackathonDto(Long hackathonId, Long organizerId, HackathonStatus nextStatus) {
        Hackathon hackathon = findHackathon(hackathonId);
        Organizer organizer = findOrganizer(organizerId);
        if (!hackathon.hasAssignedStaff(organizer)) {
            throw new ValidationException("Organizer is not assigned to this hackathon");
        }
        hackathon.advanceTo(nextStatus);
        return HackathonDto.from(hackathonRepository.save(hackathon));
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

        if (invitationRepository.existsByTeamIdAndInvitedUserIdAndStatus(
                teamId, invitedUserId, InvitationStatus.PENDING
        )) {
            throw new ValidationException("A pending invitation already exists for this user and team");
        }

        Invitation invitation = invitedBy.inviteToTeam(invitedUser, team);
        return InvitationDto.from(invitationRepository.save(invitation));
    }

    public InvitationDto inviteUserToTeamByEmail(Long invitedById, String invitedUserEmail, Long teamId) {
        User invitedUser = userRepository.findByEmailIgnoreCase(
                        invitedUserEmail == null ? "" : invitedUserEmail.trim()
                )
                .orElseThrow(() -> new EntityNotFoundException("User not found for the supplied email"));
        return inviteUserToTeam(invitedById, invitedUser.getId(), teamId);
    }

    @Transactional(readOnly = true)
    public List<InvitationDto> listInvitationsForUser(Long userId) {
        findUser(userId);
        return invitationRepository.findByInvitedUserIdOrderByCreatedAtDesc(userId).stream()
                .map(InvitationDto::from)
                .toList();
    }

    public TeamDto acceptTeamInvitation(UUID invitationId, Long acceptingUserId) {
        Invitation invitation = findInvitation(invitationId);
        User acceptingUser = findUser(acceptingUserId);

        acceptingUser.acceptInvitation(invitation);
        invitationRepository.save(invitation);
        return TeamDto.from(teamRepository.save(invitation.getTeam()));
    }

    public InvitationDto declineTeamInvitation(UUID invitationId, Long decliningUserId) {
        Invitation invitation = findInvitation(invitationId);
        User decliningUser = findUser(decliningUserId);
        invitation.decline(decliningUser);
        return InvitationDto.from(invitationRepository.save(invitation));
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

    private SupportRequest findSupportRequest(Long id) {
        return supportRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Support request not found: " + id));
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

    public record PrizePaymentResult(Long winningTeamId, String paymentReference) {
    }
}
