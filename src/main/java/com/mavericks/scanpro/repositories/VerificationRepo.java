package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.VerificationTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepo extends JpaRepository<VerificationTokens,Long> {
    VerificationTokens findByVerificationToken(String token);
}
