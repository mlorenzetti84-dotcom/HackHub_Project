package com.hackhub.dto;

import com.hackhub.domain.hackathon.Evaluation;
import com.hackhub.domain.hackathon.Submission;

import java.time.LocalDateTime;
import java.util.List;

public record SubmissionDto(
        Long id,
        Long hackathonId,
        Long teamId,
        String projectName,
        String repositoryUrl,
        LocalDateTime submittedAt,
        double averageScore,
        List<Long> evaluationIds
) {
    public static SubmissionDto from(Submission submission) {
        return new SubmissionDto(
                submission.getId(),
                submission.getHackathon().getId(),
                submission.getTeam().getId(),
                submission.getProjectName(),
                submission.getRepositoryUrl(),
                submission.getSubmittedAt(),
                submission.averageScore(),
                submission.getEvaluations().stream()
                        .map(Evaluation::getId)
                        .toList()
        );
    }
}
