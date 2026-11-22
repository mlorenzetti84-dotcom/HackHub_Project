package com.hackhub.dto;

import com.hackhub.domain.support.SupportRequest;
import com.hackhub.domain.support.SupportRequestStatus;

import java.time.LocalDateTime;

public record SupportRequestDto(
        Long id,
        Long hackathonId,
        Long teamId,
        Long requestedById,
        Long assignedMentorId,
        String description,
        LocalDateTime requestedAt,
        SupportRequestStatus status,
        String calendarReference
) {
    public static SupportRequestDto from(SupportRequest request) {
        return new SupportRequestDto(
                request.getId(),
                request.getHackathon().getId(),
                request.getTeam().getId(),
                request.getRequestedBy().getId(),
                request.getAssignedMentor() == null ? null : request.getAssignedMentor().getId(),
                request.getDescription(),
                request.getRequestedAt(),
                request.getStatus(),
                request.getCalendarReference()
        );
    }
}
