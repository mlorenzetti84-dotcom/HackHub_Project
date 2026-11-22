package com.hackhub.domain.state;

import com.hackhub.domain.hackathon.HackathonStatus;

public final class HackathonStateFactory {

    private HackathonStateFactory() {
    }

    public static HackathonState fromStatus(HackathonStatus status) {
        return switch (status) {
            case IN_ISCRIZIONE -> new InRegistrationState();
            case IN_CORSO -> new InProgressState();
            case IN_VALUTAZIONE -> new InEvaluationState();
            case CONCLUSO -> new CompletedState();
        };
    }
}