package com.hackhub.dto;

import com.hackhub.domain.hackathon.ViolationReport;

import java.time.LocalDateTime;

public record ViolationReportDto(
        Long id,
        Long mentorId,
        Long teamId,
        String description,
        LocalDateTime reportedAt
) {
    public static ViolationReportDto from(ViolationReport report) {
        return new ViolationReportDto(
                report.getId(),
                report.getMentor().getId(),
                report.getTeam().getId(),
                report.getDescription(),
                report.getReportedAt()
        );
    }
}
