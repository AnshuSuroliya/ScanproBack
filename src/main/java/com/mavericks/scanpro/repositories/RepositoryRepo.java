package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryRepo extends JpaRepository<Repository,Long> {
    Repository findByName(String name);

    Repository findByNameAndOwner(String name, Long id);

}
