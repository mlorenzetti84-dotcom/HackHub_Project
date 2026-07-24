package com.hackhub.repository;

import com.hackhub.domain.actor.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
