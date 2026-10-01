package com.jobagent.repository;

import com.jobagent.entity.Application;
import com.jobagent.entity.ApplicationStatus;
import com.jobagent.entity.Job;
import com.jobagent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByUserAndJob(User user, Job job);
    Optional<Application> findByUserIdAndJobId(Long userId, Long jobId);
    boolean existsByUserIdAndJobId(Long userId, Long jobId);

    List<Application> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Application> findByUserIdAndStatus(Long userId, ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.user.id = :userId AND a.status IN ('APPLIED', 'APPLICATION_RECEIVED', 'UNDER_REVIEW', 'ASSESSMENT_RECEIVED', 'INTERVIEW_INVITATION', 'INTERVIEW_SCHEDULED', 'OFFER_RECEIVED') AND a.submittedAt >= :since")
    long countSubmittedSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.user.id = :userId AND a.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.user.id = :userId AND LOWER(a.job.company) LIKE LOWER(CONCAT('%', :company, '%'))")
    List<Application> findByUserIdAndCompanyLike(@Param("userId") Long userId, @Param("company") String company);
}
