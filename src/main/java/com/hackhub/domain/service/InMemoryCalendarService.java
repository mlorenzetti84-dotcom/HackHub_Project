package com.hackhub.domain.service;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InMemoryCalendarService implements CalendarService {

    @Override
    public CalendarBooking bookMentoringCall(Team team, Mentor mentor, LocalDateTime start, LocalDateTime end) {
        String reference = "CAL-" + UUID.randomUUID();
        return new CalendarBooking(reference, start, end);
    }
}
