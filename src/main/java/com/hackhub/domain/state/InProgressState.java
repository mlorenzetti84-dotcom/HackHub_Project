package com.hackhub.domain.state;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;

import java.time.LocalDateTime;

public class InProgressState extends AbstractHackathonState {

    @Override
    public HackathonStatus getStatus() {
        return HackathonStatus.IN_CORSO;
    }

    @Override
    public void addMentor(Hackathon hackathon, Mentor mentor) {
        hackathon.addMentorInternal(mentor);
    }

    @Override
    public Submission submitProject(Hackathon hackathon, Team team, String projectName, String repositoryUrl) {
        return hackathon.submitProjectInternal(team, projectName, repositoryUrl);
    }

    @Override
    public Submission updateSubmission(Hackathon hackathon, Team team, String projectName, String repositoryUrl) {
        return hackathon.updateSubmissionInternal(team, projectName, repositoryUrl);
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
        return hackathon.requestSupportInternal(team, mentor, calendarService, start, end);
    }

    @Override
    public void transitionTo(Hackathon hackathon, HackathonStatus nextStatus) {
        if (nextStatus == HackathonStatus.IN_VALUTAZIONE) {
            hackathon.changeStateInternal(new InEvaluationState());
            return;
        }
        super.transitionTo(hackathon, nextStatus);
    }
}