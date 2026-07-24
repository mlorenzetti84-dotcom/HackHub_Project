package com.hackhub.domain.actor;

import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.hackathon.ViolationReport;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@DiscriminatorValue("MENTOR")
public class Mentor extends StaffMember {

    @ManyToOne
    private Hackathon assignedHackathon;

    protected Mentor() {
        // For future ORM mapping.
    }

    public Mentor(String username, String email) {
        super(username, email);
    }

    public CalendarBooking proposeCall(
            Team team,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return calendarService.bookMentoringCall(team, this, start, end);
    }

    public ViolationReport reportViolation(Hackathon hackathon, Team team, String description) {
        return hackathon.reportViolation(this, team, description);
    }

    public ViolationReport reportViolation(Team team, String description) {
        if (assignedHackathon == null) {
            throw new com.hackhub.domain.exception.ValidationException("Mentor is not assigned to an hackathon");
        }
        return assignedHackathon.reportViolation(this, team, description);
    }

    public void assignTo(Hackathon hackathon) {
        this.assignedHackathon = Objects.requireNonNull(hackathon, "hackathon cannot be null");
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.MENTOR;
    }
}
