package com.hackhub.controller;

import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.dto.CalendarBookingDto;
import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.PrizePaymentDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.dto.ViolationReportDto;
import com.hackhub.service.HackathonApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public HackathonDto createHackathon(@RequestBody CreateHackathonRequest request) {
        return hackathonApplicationService.createHackathonDto(
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
                request.organizerId()
        );
    }

    @GetMapping("/{hackathonId}")
    public HackathonDto getHackathon(@PathVariable Long hackathonId) {
        return hackathonApplicationService.getHackathonDto(hackathonId);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/registration")
    public HackathonDto registerTeam(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId
    ) {
        return hackathonApplicationService.registerTeamToHackathonDto(hackathonId, teamId);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitProject(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody SubmitProjectRequest request
    ) {
        return hackathonApplicationService.submitProjectDto(hackathonId, teamId, request.content());
    }

    @PutMapping("/{hackathonId}/teams/{teamId}/submissions")
    public SubmissionDto updateSubmission(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody SubmitProjectRequest request
    ) {
        return hackathonApplicationService.updateSubmissionDto(hackathonId, teamId, request.content());
    }

    @PostMapping("/{hackathonId}/submissions/{submissionId}/evaluations")
    public SubmissionDto evaluateSubmission(
            @PathVariable Long hackathonId,
            @PathVariable Long submissionId,
            @RequestBody EvaluateSubmissionRequest request
    ) {
        return hackathonApplicationService.evaluateSubmissionDto(
                hackathonId,
                request.judgeId(),
                submissionId,
                request.score(),
                request.comment()
        );
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/disqualification")
    public HackathonDto disqualifyTeam(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody OrganizerActionRequest request
    ) {
        return hackathonApplicationService.disqualifyTeamDto(hackathonId, request.organizerId(), teamId);
    }

    @PostMapping("/{hackathonId}/state")
    public HackathonDto advanceState(
            @PathVariable Long hackathonId,
            @RequestBody AdvanceStateRequest request
    ) {
        return hackathonApplicationService.advanceHackathonDto(hackathonId, request.nextStatus());
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/winner")
    public HackathonDto proclaimWinner(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody OrganizerActionRequest request
    ) {
        return hackathonApplicationService.proclaimWinnerDto(hackathonId, request.organizerId(), teamId);
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/winner/payment")
    public PrizePaymentDto proclaimWinnerAndPayPrize(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody OrganizerActionRequest request
    ) {
        return hackathonApplicationService.proclaimWinnerAndPayPrizeDto(
                hackathonId,
                request.organizerId(),
                teamId
        );
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/mentor-calls")
    @ResponseStatus(HttpStatus.CREATED)
    public CalendarBookingDto proposeMentorCall(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody MentorCallRequest request
    ) {
        return hackathonApplicationService.proposeMentorCallDto(
                hackathonId,
                request.mentorId(),
                teamId,
                request.start(),
                request.end()
        );
    }

    @PostMapping("/{hackathonId}/teams/{teamId}/violations")
    @ResponseStatus(HttpStatus.CREATED)
    public ViolationReportDto reportViolation(
            @PathVariable Long hackathonId,
            @PathVariable Long teamId,
            @RequestBody ViolationReportRequest request
    ) {
        return hackathonApplicationService.reportViolationDto(
                hackathonId,
                request.mentorId(),
                teamId,
                request.description()
        );
    }

    public record CreateHackathonRequest(
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
    }

    public record SubmitProjectRequest(String content) {
    }

    public record EvaluateSubmissionRequest(Long judgeId, int score, String comment) {
    }

    public record AdvanceStateRequest(HackathonStatus nextStatus) {
    }

    public record OrganizerActionRequest(Long organizerId) {
    }

    public record MentorCallRequest(Long mentorId, LocalDateTime start, LocalDateTime end) {
    }

    public record ViolationReportRequest(Long mentorId, String description) {
    }

}
