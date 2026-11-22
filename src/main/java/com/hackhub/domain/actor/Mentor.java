package com.hackhub.domain.actor;

import com.hackhub.domain.hackathon.ViolationReport;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("MENTOR")
public class Mentor extends StaffMember {

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

    public ViolationReport reportViolation(com.hackhub.domain.hackathon.Hackathon hackathon, Team team, String description) {
        return hackathon.reportViolation(this, team, description);
    }

    @Override
    public StaffRole getRole() {
        return StaffRole.MENTOR;
    }
}
