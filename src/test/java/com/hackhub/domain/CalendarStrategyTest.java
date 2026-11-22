package com.hackhub.domain;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;
import com.hackhub.domain.actor.User;
import com.hackhub.domain.service.BusinessHoursCalendarService;
import com.hackhub.domain.service.CalendarService;
import com.hackhub.domain.service.InMemoryCalendarService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.junit.jupiter.api.Assertions.*;

class CalendarStrategyTest {
    private final User owner = new User("owner", "owner@example.com");
    private final Team team = new Team("Team", owner, 3);
    private final Mentor mentor = new Mentor("mentor", "mentor@example.com");

    private static LocalDate nextSaturday() {
        return LocalDate.now().plusDays(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    private static LocalDate nextMonday() {
        return LocalDate.now().plusDays(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    @Test
    void strategiesHaveDifferentPoliciesForWeekendBookings() {
        LocalDateTime start = nextSaturday().atTime(10, 0);
        LocalDateTime end = start.plusHours(1);
        CalendarService unrestricted = new InMemoryCalendarService();
        CalendarService businessHours = new BusinessHoursCalendarService();

        assertNotNull(unrestricted.bookMentoringCall(team, mentor, start, end));
        assertThrows(IllegalArgumentException.class,
                () -> businessHours.bookMentoringCall(team, mentor, start, end));
    }

    @Test
    void businessHoursAcceptsWeekdayAppointmentsAndRejectsOverlap() {
        LocalDateTime start = nextMonday().atTime(10, 0);
        CalendarService calendar = new BusinessHoursCalendarService();

        assertNotNull(calendar.bookMentoringCall(team, mentor, start, start.plusHours(1)));
        assertThrows(IllegalArgumentException.class,
                () -> calendar.bookMentoringCall(team, mentor, start.plusMinutes(30), start.plusHours(2)));
    }

    @Test
    void businessHoursRejectsOutsideWorkingHours() {
        LocalDateTime start = nextMonday().atTime(18, 0);
        CalendarService calendar = new BusinessHoursCalendarService();
        assertThrows(IllegalArgumentException.class,
                () -> calendar.bookMentoringCall(team, mentor, start, start.plusMinutes(30)));
    }
}
