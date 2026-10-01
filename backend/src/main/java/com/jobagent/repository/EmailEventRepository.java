package com.jobagent.repository;

import com.jobagent.entity.EmailEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailEventRepository extends JpaRepository<EmailEvent, Long> {
    Optional<EmailEvent> findByMessageId(String messageId);
    boolean existsByMessageId(String messageId);
    List<EmailEvent> findByUserIdOrderByReceivedAtDesc(Long userId);
    List<EmailEvent> findByApplicationIdOrderByReceivedAtDesc(Long applicationId);
}
