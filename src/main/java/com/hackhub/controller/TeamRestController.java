package com.hackhub.controller;

import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.dto.SupportRequestDto;
import com.hackhub.service.HackathonApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teams")
public class TeamRestController {

    private final HackathonApplicationService hackathonApplicationService;

    public TeamRestController(HackathonApplicationService hackathonApplicationService) {
        this.hackathonApplicationService = hackathonApplicationService;
    }

    @PostMapping("/{teamId}/hackathons/{hackathonId}/registration")
    public HackathonDto registerToHackathon(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.registerTeamToHackathonDto(
                hackathonId,
                teamId,
                userId
        );
    }

    @PostMapping("/{teamId}/hackathons/{hackathonId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitProject(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody TeamSubmissionRequest request
    ) {
        return hackathonApplicationService.submitProjectDto(
                hackathonId,
                teamId,
                userId,
                request.content()
        );
    }

    @PutMapping("/{teamId}/hackathons/{hackathonId}/submissions")
    public SubmissionDto updateSubmission(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody TeamSubmissionRequest request
    ) {
        return hackathonApplicationService.updateSubmissionDto(
                hackathonId,
                teamId,
                userId,
                request.content()
        );
    }

    @PostMapping("/{teamId}/hackathons/{hackathonId}/support-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public SupportRequestDto requestSupport(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody SupportRequestBody request
    ) {
        return hackathonApplicationService.createSupportRequestDto(
                hackathonId, teamId, userId, request.description()
        );
    }

    @PostMapping("/{teamId}/hackathons/{hackathonId}/support-requests/{requestId}/cancellation")
    public SupportRequestDto cancelSupportRequest(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @PathVariable Long requestId,
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.cancelSupportRequestDto(hackathonId, teamId, requestId, userId);
    }

    public record TeamSubmissionRequest(@NotBlank @Size(max = 1200) String content) {
    }

    public record SupportRequestBody(@NotBlank @Size(max = 1200) String description) {
    }
}
