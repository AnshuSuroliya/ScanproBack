package com.mavericks.scanpro.repositories;

import com.mavericks.scanpro.entities.Github_files;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;

public interface GithubFileRepo extends JpaRepository<Github_files,Long> {

    Github_files findByPath(String path);


    @Query(value = "SELECT * from scanpro.github_file WHERE name=?1 LIMIT=?2 OFFSET=?3",nativeQuery = true)
    ArrayList<Github_files> findAllByNameWithPageLimit(String name, Integer Limit,Long Offset);
}
