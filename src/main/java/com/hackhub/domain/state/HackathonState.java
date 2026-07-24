package com.hackhub.domain.state;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;

import java.time.LocalDateTime;

public interface HackathonState {

    HackathonStatus getStatus();

    void registerTeam(Hackathon hackathon, Team team);

    void addMentor(Hackathon hackathon, Mentor mentor);

    Submission submitProject(Hackathon hackathon, Team team, String projectName, String repositoryUrl);

    Submission updateSubmission(Hackathon hackathon, Team team, String projectName, String repositoryUrl);

    CalendarBooking requestSupport(
            Hackathon hackathon,
            Team team,
            Mentor mentor,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    );

    void evaluateSubmission(Hackathon hackathon, Judge judge, Submission submission, int score, String comment);

    void proclaimWinner(Hackathon hackathon, Organizer organizer, Submission winningSubmission);

    void transitionTo(Hackathon hackathon, HackathonStatus nextStatus);
}