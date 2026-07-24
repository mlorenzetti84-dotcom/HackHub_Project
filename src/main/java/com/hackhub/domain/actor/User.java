package com.hackhub.domain.actor;

import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;

    @ManyToOne
    private Team currentTeam;

    protected User() {
        // For future ORM mapping.
    }

    public User(String username, String email) {
        this.username = requireText(username, "username");
        this.email = requireText(email, "email");
    }

    public Invitation inviteToTeam(User invitedUser, Team team) {
        if (!team.hasMember(this)) {
            throw new ValidationException("Only a team member can invite another user");
        }
        return team.createInvitation(this, invitedUser);
    }

    public void acceptInvitation(Invitation invitation) {
        Objects.requireNonNull(invitation, "invitation cannot be null").accept(this);
    }

    void joinTeam(Team team) {
        Objects.requireNonNull(team, "team cannot be null");
        if (currentTeam != null && !currentTeam.equals(team)) {
            throw new ValidationException("User already belongs to another team");
        }
        this.currentTeam = team;
    }

    void leaveCurrentTeam(Team team) {
        if (Objects.equals(this.currentTeam, team)) {
            this.currentTeam = null;
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Team getCurrentTeam() {
        return currentTeam;
    }

    public boolean isInTeam() {
        return currentTeam != null;
    }

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
        return value.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User user)) {
            return false;
        }
        if (id == null || user.id == null) {
            return false;
        }
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return Objects.hash(id);
    }
}
