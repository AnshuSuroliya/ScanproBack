package com.mavericks.scanpro.response;

import com.mavericks.scanpro.entities.Repository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter@Setter@NoArgsConstructor@AllArgsConstructor
public class RepoHandleResDTO {
    private String message;
    private Set<Repository> repos;
}
