package com.hackhub.domain.state;

import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

public class CompletedState extends AbstractHackathonState {

    @Override
    public HackathonStatus getStatus() {
        return HackathonStatus.CONCLUSO;
    }

    @Override
    public void proclaimWinner(Hackathon hackathon, Organizer organizer, Submission winningSubmission) {
        hackathon.proclaimWinnerInternal(organizer, winningSubmission);
    }
}