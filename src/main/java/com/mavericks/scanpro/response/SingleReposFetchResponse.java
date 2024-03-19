package com.mavericks.scanpro.response;

import com.mavericks.scanpro.entities.Repository;
import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SingleReposFetchResponse {
    Repository repo;
    String owner_email="";
    String meassage="";
}
