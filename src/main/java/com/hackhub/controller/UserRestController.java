package com.hackhub.controller;

import com.hackhub.dto.InvitationDto;
import com.hackhub.dto.TeamDto;
import com.hackhub.service.HackathonApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final HackathonApplicationService hackathonApplicationService;

    public UserRestController(HackathonApplicationService hackathonApplicationService) {
        this.hackathonApplicationService = hackathonApplicationService;
    }

    @PostMapping("/{ownerId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto createTeam(
            @PathVariable Long ownerId,
            @RequestBody CreateTeamRequest request
    ) {
        return hackathonApplicationService.createTeam(ownerId, request.name(), request.maxSize());
    }

    @PostMapping("/{invitedById}/teams/{teamId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationDto inviteUser(
            @PathVariable Long invitedById,
            @PathVariable Long teamId,
            @RequestBody InviteUserRequest request
    ) {
        return hackathonApplicationService.inviteUserToTeam(
                invitedById,
                request.invitedUserId(),
                teamId
        );
    }

    @PostMapping("/{userId}/invitations/{invitationId}/acceptance")
    public TeamDto acceptInvitation(
            @PathVariable Long userId,
            @PathVariable UUID invitationId
    ) {
        return hackathonApplicationService.acceptTeamInvitation(invitationId, userId);
    }

    public record CreateTeamRequest(String name, int maxSize) {
    }

    public record InviteUserRequest(Long invitedUserId) {
    }
}
