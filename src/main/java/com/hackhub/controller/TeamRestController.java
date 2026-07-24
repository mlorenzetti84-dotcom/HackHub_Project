package com.hackhub.controller;

import com.hackhub.dto.HackathonDto;
import com.hackhub.dto.SubmissionDto;
import com.hackhub.service.HackathonApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
            @RequestBody TeamMemberRequest request
    ) {
        return hackathonApplicationService.registerTeamToHackathonDto(
                hackathonId,
                teamId,
                request.memberId()
        );
    }

    @PostMapping("/{teamId}/hackathons/{hackathonId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionDto submitProject(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestBody TeamSubmissionRequest request
    ) {
        return hackathonApplicationService.submitProjectDto(
                hackathonId,
                teamId,
                request.memberId(),
                request.content()
        );
    }

    @PutMapping("/{teamId}/hackathons/{hackathonId}/submissions")
    public SubmissionDto updateSubmission(
            @PathVariable Long teamId,
            @PathVariable Long hackathonId,
            @RequestBody TeamSubmissionRequest request
    ) {
        return hackathonApplicationService.updateSubmissionDto(
                hackathonId,
                teamId,
                request.memberId(),
                request.content()
        );
    }

    public record TeamMemberRequest(Long memberId) {
    }

    public record TeamSubmissionRequest(Long memberId, String content) {
    }
}
