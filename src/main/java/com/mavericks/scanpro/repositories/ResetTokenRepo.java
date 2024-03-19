package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.ResetTokens;
import com.mavericks.scanpro.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetTokenRepo extends JpaRepository<ResetTokens,Long> {
    ResetTokens findByResetToken(String token);

    ResetTokens findByUser(User user);
}
