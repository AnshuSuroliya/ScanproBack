package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class SignInRequest {
    private String email;
    private String password;
}
