package com.mavericks.scanpro.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    private String email;
    private String password;
    private  String fullname;
    private String githubUsername;
    private String authToken;
}
