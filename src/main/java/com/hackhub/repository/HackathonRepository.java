package com.hackhub.repository;

import com.hackhub.domain.hackathon.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HackathonRepository extends JpaRepository<Hackathon, Long> {
}
