package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.VerificationTokens;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCodesRepo extends JpaRepository<VerificationTokens,Long> {
}
