package com.hackhub.domain.service;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Alternative booking strategy: weekdays only, between 09:00 and 18:00.
 * Slot overlap and basic date validation are delegated to the same
 * in-memory booking engine used by the default strategy.
 */
@Component
@ConditionalOnProperty(name = "hackhub.calendar.strategy", havingValue = "business-hours")
public class BusinessHoursCalendarService implements CalendarService {

    private static final LocalTime OPEN = LocalTime.of(9, 0);
    private static final LocalTime CLOSE = LocalTime.of(18, 0);
    private final InMemoryCalendarService bookings = new InMemoryCalendarService();

    @Override
    public CalendarBooking bookMentoringCall(
            Team team, Mentor mentor, LocalDateTime start, LocalDateTime end
    ) {
        Objects.requireNonNull(start, "start cannot be null");
        Objects.requireNonNull(end, "end cannot be null");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Call end must be after its start");
        }
        if (!start.toLocalDate().equals(end.toLocalDate())
                || isWeekend(start.getDayOfWeek())
                || start.toLocalTime().isBefore(OPEN)
                || end.toLocalTime().isAfter(CLOSE)) {
            throw new IllegalArgumentException(
                    "Mentoring calls must be within 09:00-18:00 on the same weekday"
            );
        }
        return bookings.bookMentoringCall(team, mentor, start, end);
    }

    private static boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
