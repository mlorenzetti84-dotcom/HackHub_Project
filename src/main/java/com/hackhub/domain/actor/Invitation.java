package com.hackhub.domain.actor;

import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invitation")
public class Invitation {

    @Id
    private UUID id;
    @ManyToOne
    private User invitedBy;
    @ManyToOne
    private User invitedUser;
    @ManyToOne
    private Team team;
    @Enumerated(EnumType.STRING)
    private InvitationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    protected Invitation() {
        // For future ORM mapping.
    }

    Invitation(User invitedBy, User invitedUser, Team team) {
        this.id = UUID.randomUUID();
        this.invitedBy = Objects.requireNonNull(invitedBy, "invitedBy cannot be null");
        this.invitedUser = Objects.requireNonNull(invitedUser, "invitedUser cannot be null");
        this.team = Objects.requireNonNull(team, "team cannot be null");
        this.status = InvitationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void accept(User acceptingUser) {
        ensureAnswerCanBeSubmittedBy(acceptingUser);
        team.addMember(invitedUser);
        this.status = InvitationStatus.ACCEPTED;
        this.answeredAt = LocalDateTime.now();
    }

    public void decline(User decliningUser) {
        ensureAnswerCanBeSubmittedBy(decliningUser);
        this.status = InvitationStatus.DECLINED;
        this.answeredAt = LocalDateTime.now();
    }

    private void ensureAnswerCanBeSubmittedBy(User user) {
        if (!Objects.equals(invitedUser, user)) {
            throw new ValidationException("Only the invited user can answer this invitation");
        }
        if (status != InvitationStatus.PENDING) {
            throw new ValidationException("Invitation has already been answered");
        }
    }

    public UUID getId() {
        return id;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public User getInvitedUser() {
        return invitedUser;
    }

    public Team getTeam() {
        return team;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
}
