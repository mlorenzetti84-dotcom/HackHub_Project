package com.hackhub.dto;

import com.hackhub.domain.actor.StaffMember;
import com.hackhub.domain.actor.User;

public record UserDto(
        Long id,
        String username,
        String email,
        Long currentTeamId,
        String role
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCurrentTeam() == null ? null : user.getCurrentTeam().getId(),
                user instanceof StaffMember staffMember ? staffMember.getRole().name() : "PARTICIPANT"
        );
    }
}
