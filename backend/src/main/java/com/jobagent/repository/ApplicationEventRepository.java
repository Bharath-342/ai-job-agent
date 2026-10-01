package com.jobagent.repository;

import com.jobagent.entity.ApplicationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationEventRepository extends JpaRepository<ApplicationEvent, Long> {
    List<ApplicationEvent> findByApplicationIdOrderByEventTimeAsc(Long applicationId);
    List<ApplicationEvent> findTop50ByOrderByEventTimeDesc();

    @Query("SELECT e FROM ApplicationEvent e JOIN FETCH e.application a JOIN FETCH a.user u JOIN FETCH a.job j WHERE u.id = :userId ORDER BY e.eventTime DESC")
    List<ApplicationEvent> findRecentEventsByUserId(@Param("userId") Long userId);
}
