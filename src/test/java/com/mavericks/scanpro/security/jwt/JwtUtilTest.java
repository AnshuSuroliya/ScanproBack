package com.rohit.identityverify.security.jwthandlers;

import com.mavericks.scanpro.security.jwt.JwtUtils;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtUtilTest {

    @Mock
    Logger logger;
    @Mock
    private JwtUtils jwtUtil;

    @Mock
    private Jwts jwts;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void generateJwtToken() {

    }

    @Test
    void getEmailJwtToken() {
    }

    @Test
    void validateJwtTokenMalformed() {
        assertFalse(jwtUtil.validateJwtToken("invalidToken"));
    }
    @Test
    void validateJwtTokenExpired() {
        assertFalse(jwtUtil.validateJwtToken("eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InJvaGl0eWFkYTEyOEBnbWFpbC5jb20ifQ.-_VCQ_BRbFx2BaBGIZGGG1FGrGkUeedFD9YVR5FAaYk"));
    }
    @Test
    void validateJwtTokenUnsupprted() {
        assertFalse(jwtUtil.validateJwtToken("eyJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6InJvaGl0eWFkYTEyOEBnbWFpbC5jb20ifQ.-_VCQ_BRbFx2BaBGIZGGG1FGrGkUeedFD9YVR5FAa"));
    }
    @Test
    void validateJwtTokenIllegal() {
        assertFalse(jwtUtil.validateJwtToken("eyJhbGciOiJIUzI1NiJ9.dsad.-_VCQ_BRbFx2BaBGIZGGG1FGrGkUeedFD9YVR5FAaYk"));
    }
}