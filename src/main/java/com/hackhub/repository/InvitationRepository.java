package com.hackhub.repository;

import com.hackhub.domain.actor.Invitation;
import com.hackhub.domain.actor.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    boolean existsByTeamIdAndInvitedUserIdAndStatus(Long teamId, Long invitedUserId, InvitationStatus status);
    List<Invitation> findByInvitedUserIdOrderByCreatedAtDesc(Long invitedUserId);
}
