package com.hackhub.dto;

import com.hackhub.domain.actor.StaffMember;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HackathonDto(
        Long id,
        String name,
        String location,
        String rules,
        LocalDate registrationStart,
        LocalDate registrationEnd,
        LocalDate hackathonStart,
        LocalDate hackathonEnd,
        LocalDate evaluationEnd,
        BigDecimal prizeAmount,
        int maxTeamSize,
        HackathonStatus status,
        List<Long> registeredTeamIds,
        List<Long> staffMemberIds,
        List<Long> submissionIds,
        Long winningSubmissionId,
        Long winningTeamId
) {
    public static HackathonDto from(Hackathon hackathon) {
        Submission winningSubmission = hackathon.getWinningSubmission();

        return new HackathonDto(
                hackathon.getId(),
                hackathon.getName(),
                hackathon.getLocation(),
                hackathon.getRules(),
                hackathon.getRegistrationStart(),
                hackathon.getRegistrationEnd(),
                hackathon.getHackathonStart(),
                hackathon.getHackathonEnd(),
                hackathon.getEvaluationEnd(),
                hackathon.getPrizeAmount(),
                hackathon.getMaxTeamSize(),
                hackathon.getStatus(),
                hackathon.getRegisteredTeams().stream()
                        .map(Team::getId)
                        .toList(),
                hackathon.getStaffMembers().stream()
                        .map(StaffMember::getId)
                        .toList(),
                hackathon.getSubmissions().stream()
                        .map(Submission::getId)
                        .toList(),
                winningSubmission == null ? null : winningSubmission.getId(),
                winningSubmission == null ? null : winningSubmission.getTeam().getId()
        );
    }
}
