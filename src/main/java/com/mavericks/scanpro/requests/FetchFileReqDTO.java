package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FetchFileReqDTO {
    String reponame;
    String path;
    Long owner;
}
