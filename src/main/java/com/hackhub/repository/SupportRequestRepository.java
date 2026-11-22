package com.hackhub.repository;

import com.hackhub.domain.support.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findByHackathonIdOrderByRequestedAtAsc(Long hackathonId);
}
