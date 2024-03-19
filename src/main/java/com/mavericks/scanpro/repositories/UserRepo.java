package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepo extends JpaRepository<User,Long> {
    User findByEmail(String username);

    Boolean existsByEmail(String email);
}
