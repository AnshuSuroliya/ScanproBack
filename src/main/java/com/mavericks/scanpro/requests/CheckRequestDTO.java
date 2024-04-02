package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckRequestDTO {
    private String authToken;
    private String username;
}
