package com.jobagent.repository;

import com.jobagent.entity.CandidateProfile;
import com.jobagent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {
    Optional<CandidateProfile> findByUser(User user);
    Optional<CandidateProfile> findByUserId(Long userId);
}
