package com.mavericks.scanpro.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter@Setter@NoArgsConstructor@AllArgsConstructor
public class RepoHandleReqDTO {
    private String name;
    private String description;
    private Set<String> emailList;
    private Boolean adding;
}
