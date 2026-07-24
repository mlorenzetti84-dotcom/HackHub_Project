package com.hackhub.domain.state;

import com.hackhub.domain.actor.Judge;
import com.hackhub.domain.actor.Organizer;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.hackathon.Submission;

public class InEvaluationState extends AbstractHackathonState {

    @Override
    public HackathonStatus getStatus() {
        return HackathonStatus.IN_VALUTAZIONE;
    }

    @Override
    public void evaluateSubmission(Hackathon hackathon, Judge judge, Submission submission, int score, String comment) {
        hackathon.evaluateSubmissionInternal(judge, submission, score, comment);
    }

    @Override
    public void proclaimWinner(Hackathon hackathon, Organizer organizer, Submission winningSubmission) {
        hackathon.proclaimWinnerInternal(organizer, winningSubmission);
        hackathon.changeStateInternal(new CompletedState());
    }

    @Override
    public void transitionTo(Hackathon hackathon, HackathonStatus nextStatus) {
        if (nextStatus == HackathonStatus.CONCLUSO) {
            hackathon.changeStateInternal(new CompletedState());
            return;
        }
        super.transitionTo(hackathon, nextStatus);
    }
}