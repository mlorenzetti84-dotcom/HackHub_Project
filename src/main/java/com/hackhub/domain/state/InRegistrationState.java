package com.hackhub.domain.state;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

public class InRegistrationState extends AbstractHackathonState {

    @Override
    public HackathonStatus getStatus() {
        return HackathonStatus.IN_ISCRIZIONE;
    }

    @Override
    public void registerTeam(Hackathon hackathon, Team team) {
        hackathon.registerTeamInternal(team);
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
    public void transitionTo(Hackathon hackathon, HackathonStatus nextStatus) {
        if (nextStatus == HackathonStatus.IN_CORSO) {
            hackathon.changeStateInternal(new InProgressState());
            return;
        }
        super.transitionTo(hackathon, nextStatus);
    }
}
