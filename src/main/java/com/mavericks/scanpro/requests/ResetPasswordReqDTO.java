package com.mavericks.scanpro.requests;

import lombok.Getter;
import lombok.Setter;

import java.security.PrivateKey;

@Getter@Setter
public class ResetPasswordReqDTO {
    private String token;
    private String newPassword;
}
