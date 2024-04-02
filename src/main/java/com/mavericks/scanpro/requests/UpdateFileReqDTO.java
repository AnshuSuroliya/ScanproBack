package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter@Setter
public class UpdateFileReqDTO {
    private String name;
    private MultipartFile file;
    private String path;
    private String repoName;
    private Long owner;
    private String sha;
}
