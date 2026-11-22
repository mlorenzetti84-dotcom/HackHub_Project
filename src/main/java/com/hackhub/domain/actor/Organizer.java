package com.hackhub.domain.actor;

import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.Submission;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ORGANIZER")
public class Organizer extends StaffMember {

    protected Organizer() {
        // For future ORM mapping.
    }

    public Organizer(String username, String email) {
        super(username, email);
    }

    public void assignStaff(Hackathon hackathon, StaffMember staffMember) {
        hackathon.addStaffMember(this, staffMember);
    }

    public void proclaimWinner(Hackathon hackathon, Submission winningSubmission) {
        hackathon.proclaimWinner(this, winningSubmission);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.ORGANIZER;
    }
}
