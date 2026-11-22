package com.hackhub.repository;

import com.hackhub.domain.hackathon.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
}
