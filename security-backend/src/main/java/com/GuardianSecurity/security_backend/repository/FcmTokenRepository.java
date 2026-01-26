package com.GuardianSecurity.security_backend.repository;
import com.GuardianSecurity.security_backend.model.UserFcmToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
    Optional<UserFcmToken> findByToken(String token);

    List<UserFcmToken> findByUserId(Long userId);

    void deleteByToken(String token);
}
