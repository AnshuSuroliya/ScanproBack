package com.mavericks.scanpro.security;

import com.mavericks.scanpro.entities.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class userDetailsTest {
    @InjectMocks
    UserDetails userDetailsTemplate;


    @Test
    void build() {
        User mockUser =new User();
        mockUser.setEmail("test@gmail.com");
        mockUser.setPassword("Password@123");
        mockUser.setId(Long.parseLong("123"));
        mockUser.setRole("ADMIN");

        UserDetails details = UserDetails.build(mockUser);

        assertEquals(mockUser.getEmail(),details.getUsername());
        assertEquals(mockUser.getId(),details.getId());
        assertEquals(mockUser.getPassword(),details.getPassword());
    }



    @Test
    void getUsername() {
        User mockUser =new User();
        mockUser.setEmail("test@gmail.com");
        mockUser.setPassword("Password@123");
        mockUser.setId(Long.parseLong("123"));
        mockUser.setRole("ADMIN");

        UserDetails details =UserDetails.build(mockUser);

        assertEquals("test@gmail.com",details.getUsername());
    }

    @Test
    void isAccountNonExpired() {
        UserDetails details =new UserDetails();
        assertTrue(details.isAccountNonExpired());
    }

    @Test
    void isAccountNonLocked() {
        UserDetails details =new UserDetails();
        assertTrue(details.isAccountNonLocked());
    }

    @Test
    void isCredentialsNonExpired() {
        UserDetails details =new UserDetails();
        assertTrue(details.isCredentialsNonExpired());
    }

    @Test
    void isEnabled() {
        UserDetails details =new UserDetails();
        assertTrue(details.isEnabled());
    }


}