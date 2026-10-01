package com.jobagent.repository;

import com.jobagent.entity.User;
import com.jobagent.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    Optional<UserSettings> findByUser(User user);
    Optional<UserSettings> findByUserId(Long userId);
}
