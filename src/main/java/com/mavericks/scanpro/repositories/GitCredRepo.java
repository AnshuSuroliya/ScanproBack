package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.GitCreds;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GitCredRepo extends JpaRepository<GitCreds,Long> {
}
