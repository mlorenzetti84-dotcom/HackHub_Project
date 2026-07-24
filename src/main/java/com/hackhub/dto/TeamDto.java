package com.hackhub.dto;

import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;

import java.util.List;

public record TeamDto(
        Long id,
        String name,
        int maxSize,
        boolean disqualified,
        List<Long> memberIds
) {
    public static TeamDto from(Team team) {
        return new TeamDto(
                team.getId(),
                team.getName(),
                team.getMaxSize(),
                team.isDisqualified(),
                team.getMembers().stream()
                        .map(User::getId)
                        .toList()
        );
    }
}
