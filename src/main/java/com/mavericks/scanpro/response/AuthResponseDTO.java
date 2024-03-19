package com.mavericks.scanpro.response;

import lombok.Getter;
import lombok.Setter;

@Setter@Getter
public class AuthResponseDTO {
    private Boolean success;
    private String message;
    private String token;
}
