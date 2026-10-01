package com.jobagent.repository;

import com.jobagent.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByJobKey(String jobKey);
    boolean existsByJobKey(String jobKey);

    @Query("SELECT j FROM Job j WHERE j.company = :company AND j.title = :title AND j.location = :location")
    Optional<Job> findByCompanyAndTitleAndLocation(
        @Param("company") String company,
        @Param("title") String title,
        @Param("location") String location
    );

    List<Job> findByIsActiveTrueOrderByPostedDateDesc();

    @Query("SELECT j FROM Job j WHERE j.isActive = true AND j.country = 'India' AND j.targetGraduationYear = :gradYear AND j.maxExperienceYears <= :maxExp")
    List<Job> findEligibleJobsForFresher(
        @Param("gradYear") Integer gradYear,
        @Param("maxExp") Double maxExp
    );
}
