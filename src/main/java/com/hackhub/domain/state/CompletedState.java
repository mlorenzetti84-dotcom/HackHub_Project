package com.hackhub.domain.state;

import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.HackathonStatus;

public class CompletedState extends AbstractHackathonState {

    @Override
    public HackathonStatus getStatus() {
        return HackathonStatus.CONCLUSO;
    }

}
