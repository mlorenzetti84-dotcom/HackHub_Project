package com.hackhub.domain.service;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "hackhub.calendar.strategy", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryCalendarService implements CalendarService {

    private final List<BookedSlot> bookedSlots = new ArrayList<>();

    @Override
    public synchronized CalendarBooking bookMentoringCall(
            Team team,
            Mentor mentor,
            LocalDateTime start,
            LocalDateTime end
    ) {
        Objects.requireNonNull(team, "team cannot be null");
        Objects.requireNonNull(mentor, "mentor cannot be null");
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Call end must be after its start");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A mentoring call cannot be booked in the past");
        }
        boolean mentorIsBusy = bookedSlots.stream()
                .anyMatch(slot -> slot.mentor().equals(mentor)
                        && start.isBefore(slot.end())
                        && end.isAfter(slot.start()));
        if (mentorIsBusy) {
            throw new IllegalArgumentException("The mentor is already busy in the requested time slot");
        }

        String reference = "CAL-" + UUID.randomUUID();
        bookedSlots.add(new BookedSlot(mentor, start, end));
        return new CalendarBooking(reference, start, end);
    }

    private record BookedSlot(Mentor mentor, LocalDateTime start, LocalDateTime end) {
    }
}
