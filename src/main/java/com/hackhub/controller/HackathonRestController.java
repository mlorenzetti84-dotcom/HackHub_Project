package com.hackhub.controller;

import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.dto.CalendarBookingDto;
import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.PrizePaymentDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.dto.SupportRequestDto;
import com.hackhub.dto.ViolationReportDto;
import com.hackhub.service.HackathonApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/hackathons")
public class HackathonRestController {

    private final HackathonApplicationService hackathonApplicationService;

    public HackathonRestController(HackathonApplicationService hackathonApplicationService) {
        this.hackathonApplicationService = hackathonApplicationService;
    }

    @GetMapping
    public List<HackathonDto> listHackathons() {
        return hackathonApplicationService.listHackathons();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HackathonDto createHackathon(
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody CreateHackathonRequest request
    ) {
        return hackathonApplicationService.createHackathonWithStaffDto(
                request.name(),
                request.location(),
                request.rules(),
                request.registrationStart(),
                request.registrationEnd(),
                request.hackathonStart(),
                request.hackathonEnd(),
                request.evaluationEnd(),
                request.prizeAmount(),
                request.maxTeamSize(),
                userId,
                request.judgeId(),
                request.mentorIds()
        );
    }

    @GetMapping("/{hackathonId}")
    public HackathonDto getHackathon(@PathVariable Long hackathonId) {
        return hackathonApplicationService.getHackathonDto(hackathonId);
    }

    @PostMapping("/{hackathonId}/submissions/{submissionId}/evaluations")
    public SubmissionDto evaluateSubmission(
            @PathVariable Long hackathonId,
            @PathVariable Long submissionId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody EvaluateSubmissionRequest request
    ) {
        return hackathonApplicationService.evaluateSubmissionDto(
                hackathonId,
                userId,
                submissionId,
                request.score(),
                request.comment()
        );
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/disqualification")
    public HackathonDto disqualifyTeam(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.disqualifyTeamDto(hackathonId, userId, teamId);
    }

    @PostMapping("/{hackathonId}/state")
    public HackathonDto advanceState(
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody AdvanceStateRequest request
    ) {
        return hackathonApplicationService.advanceHackathonDto(
                hackathonId, userId, request.nextStatus()
        );
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/winner")
    public HackathonDto proclaimWinner(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.proclaimWinnerDto(hackathonId, userId, teamId);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/winner/payment")
    public PrizePaymentDto payPrize(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.payPrizeDto(
                hackathonId,
                userId,
                teamId
        );
    }

    @PostMapping("/{hackathonId}/mentors")
    public HackathonDto addMentor(
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody AddMentorRequest request
    ) {
        return hackathonApplicationService.assignMentorDto(
                hackathonId,
                userId,
                request.mentorId()
        );
    }

    @GetMapping("/{hackathonId}/support-requests")
    public List<SupportRequestDto> listSupportRequests(
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.listSupportRequestsForMentor(hackathonId, userId);
    }

    @PostMapping("/{hackathonId}/support-requests/{requestId}/call")
    public CalendarBookingDto proposeCallForRequest(
            @PathVariable Long hackathonId,
            @PathVariable Long requestId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody MentorCallForRequest request
    ) {
        return hackathonApplicationService.proposeCallForSupportRequestDto(
                hackathonId, requestId, userId, request.start(), request.end()
        );
    }

    @GetMapping("/{hackathonId}/submissions")
    public List<SubmissionDto> listSubmissionsForStaff(
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.listSubmissionsForStaff(hackathonId, userId);
    }

    @GetMapping("/{hackathonId}/submissions/{submissionId}")
    public SubmissionDto getSubmissionForStaff(
            @PathVariable Long hackathonId,
            @PathVariable Long submissionId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.getSubmissionForStaff(hackathonId, submissionId, userId);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/violations")
    @ResponseStatus(HttpStatus.CREATED)
    public ViolationReportDto reportViolation(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody ViolationReportRequest request
    ) {
        return hackathonApplicationService.reportViolationDto(
                hackathonId,
                userId,
                teamId,
                request.description()
        );
    }

    public record CreateHackathonRequest(
            @NotBlank String name,
            @NotBlank String location,
            @NotBlank @Size(max = 2000) String rules,
            @NotNull LocalDate registrationStart,
            @NotNull LocalDate registrationEnd,
            @NotNull LocalDate hackathonStart,
            @NotNull LocalDate hackathonEnd,
            @NotNull LocalDate evaluationEnd,
            @NotNull @DecimalMin("0.01") BigDecimal prizeAmount,
            @Positive int maxTeamSize,
            @NotNull Long judgeId,
            @NotEmpty List<@NotNull Long> mentorIds
    ) {
    }

    public record EvaluateSubmissionRequest(
            @jakarta.validation.constraints.Min(0) @jakarta.validation.constraints.Max(10) int score,
            @NotBlank @Size(max = 1200) String comment
    ) {
    }

    public record AdvanceStateRequest(@NotNull HackathonStatus nextStatus) {
    }

    public record MentorCallForRequest(@NotNull LocalDateTime start, @NotNull LocalDateTime end) {
    }

    public record AddMentorRequest(@NotNull Long mentorId) {
    }

    public record ViolationReportRequest(@NotBlank @Size(max = 1200) String description) {
    }

}
