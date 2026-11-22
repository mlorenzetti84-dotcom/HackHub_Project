package com.hackhub.controller;

import com.hackhub.dto.InvitationDto;
import com.hackhub.dto.TeamDto;
import com.hackhub.service.HackathonApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final HackathonApplicationService hackathonApplicationService;

    public UserRestController(HackathonApplicationService hackathonApplicationService) {
        this.hackathonApplicationService = hackathonApplicationService;
    }

    @PostMapping("/me/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto createTeam(
            @RequestAttribute("authenticatedUserId") Long userId,
            @Valid @RequestBody CreateTeamRequest request
    ) {
        return hackathonApplicationService.createTeam(userId, request.name(), request.maxSize());
    }

    @PostMapping("/me/teams/{teamId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public InvitationDto inviteUser(
            @RequestAttribute("authenticatedUserId") Long userId,
            @PathVariable Long teamId,
            @Valid @RequestBody InviteUserRequest request
    ) {
        return hackathonApplicationService.inviteUserToTeamByEmail(
                userId,
                request.invitedUserEmail(),
                teamId
        );
    }

    @GetMapping("/me/invitations")
    public List<InvitationDto> listInvitations(
            @RequestAttribute("authenticatedUserId") Long userId
    ) {
        return hackathonApplicationService.listInvitationsForUser(userId);
    }

    @PostMapping("/me/invitations/{invitationId}/acceptance")
    public TeamDto acceptInvitation(
            @RequestAttribute("authenticatedUserId") Long userId,
            @PathVariable UUID invitationId
    ) {
        return hackathonApplicationService.acceptTeamInvitation(invitationId, userId);
    }

    @PostMapping("/me/invitations/{invitationId}/decline")
    public InvitationDto declineInvitation(
            @RequestAttribute("authenticatedUserId") Long userId,
            @PathVariable UUID invitationId
    ) {
        return hackathonApplicationService.declineTeamInvitation(invitationId, userId);
    }

    public record CreateTeamRequest(@NotBlank String name, @Positive int maxSize) {
    }

    public record InviteUserRequest(@NotBlank @Email String invitedUserEmail) {
    }
}
