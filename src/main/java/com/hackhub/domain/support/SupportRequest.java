package com.hackhub.domain.support;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.exception.ValidationException;
import com.hackhub.domain.hackathon.Hackathon;
import com.hackhub.domain.service.CalendarBooking;
import com.hackhub.domain.service.CalendarService;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "support_request")
public class SupportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Hackathon hackathon;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private User requestedBy;

    @ManyToOne
    private Mentor assignedMentor;

    @Column(length = 1200, nullable = false)
    private String description;
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    private SupportRequestStatus status;

    private String calendarReference;
    private LocalDateTime callStart;
    private LocalDateTime callEnd;

    protected SupportRequest() {
        // Required by JPA.
    }

    public SupportRequest(Hackathon hackathon, Team team, User requestedBy, String description) {
        this.hackathon = Objects.requireNonNull(hackathon, "hackathon cannot be null");
        this.team = Objects.requireNonNull(team, "team cannot be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy cannot be null");
        if (!team.hasMember(requestedBy)) {
            throw new ValidationException("Only a team member can request support");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("Support request description cannot be blank");
        }
        this.description = description.trim();
        this.requestedAt = LocalDateTime.now();
        this.status = SupportRequestStatus.OPEN;
    }

    public CalendarBooking proposeCall(
            Mentor mentor,
            CalendarService calendarService,
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (status != SupportRequestStatus.OPEN) {
            throw new ValidationException("Support request is not open");
        }
        assignedMentor = Objects.requireNonNull(mentor, "mentor cannot be null");
        CalendarBooking calendarBooking = mentor.proposeCall(
                team,
                Objects.requireNonNull(calendarService, "calendarService cannot be null"),
                start,
                end
        );
        calendarReference = calendarBooking.getExternalReference();
        callStart = calendarBooking.getStart();
        callEnd = calendarBooking.getEnd();
        status = SupportRequestStatus.CALL_PROPOSED;
        return calendarBooking;
    }

    public void cancel(User member) {
        if (!team.hasMember(Objects.requireNonNull(member, "member cannot be null"))) {
            throw new ValidationException("Only a team member can cancel the support request");
        }
        if (status != SupportRequestStatus.OPEN) {
            throw new ValidationException("Only open support requests can be cancelled");
        }
        status = SupportRequestStatus.CANCELLED;
    }

    public Long getId() { return id; }
    public Hackathon getHackathon() { return hackathon; }
    public Team getTeam() { return team; }
    public User getRequestedBy() { return requestedBy; }
    public Mentor getAssignedMentor() { return assignedMentor; }
    public String getDescription() { return description; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public SupportRequestStatus getStatus() { return status; }
    public String getCalendarReference() { return calendarReference; }
    public LocalDateTime getCallStart() { return callStart; }
    public LocalDateTime getCallEnd() { return callEnd; }
}
