package com.hackhub.domain.actor;

import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.Submission;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("JUDGE")
public class Judge extends StaffMember {

    protected Judge() {
        // For future ORM mapping.
    }

    public Judge(String username, String email) {
        super(username, email);
    }

    public void evaluate(Hackathon hackathon, Submission submission, int score, String comment) {
        hackathon.evaluateSubmission(this, submission, score, comment);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.JUDGE;
    }
}
