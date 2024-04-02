package com.mavericks.scanpro.response;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SaveFileResDTO {
    private boolean Success;
    private String message;
    private String name;
    private String sha;
    private String path;
}
