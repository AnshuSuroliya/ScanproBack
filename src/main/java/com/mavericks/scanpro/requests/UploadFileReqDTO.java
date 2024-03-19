package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UploadFileReqDTO {
    private String name;
    private String repoName;
    private MultipartFile file;
    private Long owner;

    //path = foldername+"/"+fldername+"/"..........
    private String path;
}
