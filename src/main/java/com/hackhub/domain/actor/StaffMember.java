package com.hackhub.domain.actor;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public abstract class StaffMember extends User {

    protected StaffMember() {
        // For future ORM mapping.
    }

    protected StaffMember(String username, String email) {
        super(username, email);
    }

    @Transient
    public abstract StaffRole getRole();
}
