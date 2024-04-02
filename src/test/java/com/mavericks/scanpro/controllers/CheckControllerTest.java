package com.mavericks.scanpro.controllers;

import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.services.GithubFileServiceImpl;
import com.mavericks.scanpro.services.RepoServiceImpl;
import org.hibernate.annotations.Check;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckControllerTest {

    @Mock
    private GithubFileServiceImpl githubFileService;

    @Mock
    private RepoServiceImpl repoService;

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CheckController checkController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        githubFileService = mock(GithubFileServiceImpl.class);
        repoService = mock(RepoServiceImpl.class);
    }

    @Test
    void isEmailRegistred() {
        when(userRepo.existsByEmail(any())).thenReturn(true);
        assertTrue(checkController.IsEmailRegistred("rohit@gmail.com"));
    }

    @Test
    void getUserById() {
        User mockUser =new User();
        mockUser.setEmail("email@gmail.com");
        mockUser.setRole("ADMIN");
        mockUser.setFullname("Rohit");
        mockUser.setPassword("Password");

        when(userRepo.findById(any())).thenReturn(Optional.of(mockUser));
        assertEquals("email@gmail.com",checkController.getUserById(1L).getEmail());
        assertEquals("ADMIN",checkController.getUserById(1L).getRole());
        assertEquals("Password",checkController.getUserById(1L).getPassword());
        assertEquals("Rohit",checkController.getUserById(1L).getFullname());
    }

    @Test
    void isJwtValidate() {
    }

    @Test
    void isAuthTokenValidate() {
    }
}