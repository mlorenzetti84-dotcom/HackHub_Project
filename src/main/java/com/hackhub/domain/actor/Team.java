package com.hackhub.domain.actor;

import com.hackhub.domain.exception.ValidationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int maxSize;
    private boolean disqualified;

    @OneToMany(mappedBy = "currentTeam", cascade = CascadeType.ALL)
    private List<User> members = new ArrayList<>();

    @Transient
    private List<Invitation> invitations = new ArrayList<>();

    protected Team() {
        // For future ORM mapping.
    }

    public Team(String name, User owner, int maxSize) {
        if (maxSize < 1) {
            throw new ValidationException("Team max size must be at least 1");
        }
        this.name = User.requireText(name, "name");
        this.maxSize = maxSize;
        addMember(Objects.requireNonNull(owner, "owner cannot be null"));
    }

    public Invitation createInvitation(User invitedBy, User invitedUser) {
        Objects.requireNonNull(invitedBy, "invitedBy cannot be null");
        Objects.requireNonNull(invitedUser, "invitedUser cannot be null");

        if (!hasMember(invitedBy)) {
            throw new ValidationException("Inviter must belong to this team");
        }
        if (hasMember(invitedUser)) {
            throw new ValidationException("User already belongs to this team");
        }
        if (isFull()) {
            throw new ValidationException("Team is already full");
        }

        Invitation invitation = new Invitation(invitedBy, invitedUser, this);
        invitations.add(invitation);
        return invitation;
    }

    public void addMember(User user) {
        Objects.requireNonNull(user, "user cannot be null");
        if (hasMember(user)) {
            return;
        }
        if (isFull()) {
            throw new ValidationException("Team is already full");
        }
        if (user.isInTeam()) {
            throw new ValidationException("User already belongs to another team");
        }
        members.add(user);
        user.joinTeam(this);
    }

    public void removeMember(User user) {
        if (members.remove(user)) {
            user.leaveCurrentTeam(this);
        }
    }

    public boolean hasMember(User user) {
        return members.contains(user);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public void disqualify() {
        this.disqualified = true;
    }

    public boolean isDisqualified() {
        return disqualified;
    }

    public void ensureCanSubmit() {
        if (disqualified) {
            throw new ValidationException("Disqualified teams cannot submit or update submissions");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public List<Invitation> getInvitations() {
        return Collections.unmodifiableList(invitations);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Team team)) {
            return false;
        }
        if (id == null || team.id == null) {
            return false;
        }
        return Objects.equals(id, team.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return System.identityHashCode(this);
        }
        return Objects.hash(id);
    }
}
