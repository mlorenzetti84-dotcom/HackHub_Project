package com.hackhub.domain.hackathon;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "violation_report")
public class ViolationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Mentor mentor;

    @ManyToOne
    private Team team;
    @Column(length = 1200)
    private String description;
    private LocalDateTime reportedAt;

    protected ViolationReport() {
        // For future ORM mapping.
    }

    public ViolationReport(Mentor mentor, Team team, String description) {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Violation description cannot be blank");
        }

        this.mentor = Objects.requireNonNull(mentor, "mentor cannot be null");
        this.team = Objects.requireNonNull(team, "team cannot be null");
        this.description = description.trim();
        this.reportedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Mentor getMentor() {
        return mentor;
    }

    public Team getTeam() {
        return team;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }
}
