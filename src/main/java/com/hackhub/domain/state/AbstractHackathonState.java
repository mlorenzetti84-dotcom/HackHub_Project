package com.hackhub.domain.state;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.exception.InvalidStateTransitionException;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;

import java.time.LocalDateTime;

public abstract class AbstractHackathonState implements HackathonState {

    @Override
    public void registerTeam(Hackathon hackathon, Team team) {
        throw operationNotAllowed("register a team");
    }

    @Override
    public void addMentor(Hackathon hackathon, Mentor mentor) {
        throw operationNotAllowed("add a mentor");
    }

    @Override
    public Submission submitProject(Hackathon hackathon, Team team, String projectName, String repositoryUrl) {
        throw operationNotAllowed("submit a project");
    }

    @Override
    public Submission updateSubmission(Hackathon hackathon, Team team, String projectName, String repositoryUrl) {
        throw operationNotAllowed("update a submission");
    }

    @Override
    public CalendarBooking requestSupport(
            Hackathon hackathon,
            Team team,
            Mentor mentor,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    ) {
        throw operationNotAllowed("request support");
    }

    @Override
    public void evaluateSubmission(Hackathon hackathon, Judge judge, Submission submission, int score, String comment) {
        throw operationNotAllowed("evaluate a submission");
    }

    @Override
    public void proclaimWinner(Hackathon hackathon, Organizer organizer, Submission winningSubmission) {
        throw operationNotAllowed("proclaim a winner");
    }

    @Override
    public void transitionTo(Hackathon hackathon, HackathonStatus nextStatus) {
        throw new InvalidStateTransitionException(
                "Cannot transition hackathon from " + getStatus() + " to " + nextStatus
        );
    }

    protected InvalidStateTransitionException operationNotAllowed(String operation) {
        return new InvalidStateTransitionException(
                "Cannot " + operation + " while hackathon is " + getStatus()
        );
    }
}