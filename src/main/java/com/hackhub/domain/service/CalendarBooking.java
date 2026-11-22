package com.hackhub.domain.service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class CalendarBooking {

    private UUID id;
    private String externalReference;
    private LocalDateTime start;
    private LocalDateTime end;

    protected CalendarBooking() {
        // For future ORM mapping.
    }

    public CalendarBooking(String externalReference, LocalDateTime start, LocalDateTime end) {
        this.id = UUID.randomUUID();
        this.externalReference = Objects.requireNonNull(externalReference, "externalReference cannot be null");
        this.start = Objects.requireNonNull(start, "start cannot be null");
        this.end = Objects.requireNonNull(end, "end cannot be null");

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Booking end must be after start");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}