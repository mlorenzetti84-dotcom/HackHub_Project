package com.hackhub.domain.service;

import com.hackhub.domain.actor.Mentor;
import com.hackhub.domain.actor.Team;

import java.time.LocalDateTime;

public interface CalendarService {

    CalendarBooking bookMentoringCall(Team team, Mentor mentor, LocalDateTime start, LocalDateTime end);
}