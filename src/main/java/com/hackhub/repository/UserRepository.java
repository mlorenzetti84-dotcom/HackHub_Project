package com.hackhub.repository;

import com.hackhub.domain.actor.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
