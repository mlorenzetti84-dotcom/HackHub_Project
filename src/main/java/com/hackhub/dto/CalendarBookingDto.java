package com.hackhub.dto;

import com.hackhub.domain.service.CalendarBooking;

import java.time.LocalDateTime;
import java.util.UUID;

public record CalendarBookingDto(
        UUID id,
        String externalReference,
        LocalDateTime start,
        LocalDateTime end
) {
    public static CalendarBookingDto from(CalendarBooking booking) {
        return new CalendarBookingDto(
                booking.getId(),
                booking.getExternalReference(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}
