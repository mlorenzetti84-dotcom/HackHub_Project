package com.hackhub.dto;

import com.hackhub.domain.actor.Invitation;
import com.hackhub.domain.actor.InvitationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationDto(
        UUID id,
        Long invitedById,
        Long invitedUserId,
        Long teamId,
        InvitationStatus status,
        LocalDateTime createdAt,
        LocalDateTime answeredAt
) {
    public static InvitationDto from(Invitation invitation) {
        return new InvitationDto(
                invitation.getId(),
                invitation.getInvitedBy().getId(),
                invitation.getInvitedUser().getId(),
                invitation.getTeam().getId(),
                invitation.getStatus(),
                invitation.getCreatedAt(),
                invitation.getAnsweredAt()
        );
    }
}
