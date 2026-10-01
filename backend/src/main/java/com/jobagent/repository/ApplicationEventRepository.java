package com.jobagent.repository;

import com.jobagent.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, Long> {
    List<ApplicationEvent> findByApplicationIdOrderByEventTimeAsc(Long applicationId);
    List<ApplicationEvent> findTop50ByOrderByEventTimeDesc();
}
