package com.mavericks.scanpro.security.jwt;


import com.mavericks.scanpro.security.UserDetailServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailServiceImpl userDetailsService;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Test
    void testDoFilterInternalValidToken() throws ServletException, IOException {
        // Mocking a valid JWT token
        String validToken = "validToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn("testuser");

        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).getUserNameFromJwtToken(validToken);
        verify(jwtUtils).validateJwtToken(validToken);
        verify(userDetailsService).loadUserByUsername("testuser");
    }

    @Test
    void testDoFilterInternalInvalidToken() throws ServletException, IOException {
        // Mocking an invalid JWT token
        String invalidToken = "invalidToken";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateJwtToken(invalidToken);
        verify(jwtUtils, never()).getUserNameFromJwtToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void testDoFilterInternalNoToken() throws ServletException, IOException {
        // No token provided in the request
        when(request.getHeader("Authorization")).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils, never()).validateJwtToken(any());
        verify(jwtUtils, never()).getUserNameFromJwtToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void testDoFilterInternalException() throws ServletException, IOException {
        // Mocking an exception during token processing
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtils.validateJwtToken("validToken")).thenThrow(new RuntimeException("Test exception"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtUtils).validateJwtToken("validToken");
        verify(jwtUtils, never()).getUserNameFromJwtToken(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void testParseJwt() {
        // Valid token in the Authorization header
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        String result = authTokenFilter.parseJwt(request);
        assertEquals("validToken", result);

        // No token in the Authorization header
        when(request.getHeader("Authorization")).thenReturn(null);
        result = authTokenFilter.parseJwt(request);
        assertNull(result);

        // Invalid token format in the Authorization header
        when(request.getHeader("Authorization")).thenReturn("InvalidToken");
        result = authTokenFilter.parseJwt(request);
        assertNull(result);
    }
}
