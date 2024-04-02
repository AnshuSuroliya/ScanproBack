package com.mavericks.scanpro.controllers;

import com.mavericks.scanpro.controllers.ProfileController;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.requests.UserUpdateRequest;
import com.mavericks.scanpro.services.ProfileService;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        profileService = mock(ProfileService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        profileController = new ProfileController(profileService, passwordEncoder);
    }

    @Test
    void testGetUserProfile_UserFound() {
        // Setup
        long id = 1L;
        User user = new User();
        user.setId(id);
        user.setFullname("John Doe");
        when(profileService.getUserProfile(id)).thenReturn(user);

        // Execution
        ResponseEntity<User> responseEntity = profileController.getUserProfile(id);

        // Verification
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(user, responseEntity.getBody());
        verify(profileService, times(1)).getUserProfile(id);
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        // Setup
        long id = 1L;
        when(profileService.getUserProfile(id)).thenReturn(null);

        // Execution
        ResponseEntity<User> responseEntity = profileController.getUserProfile(id);

        // Verification
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        verify(profileService, times(1)).getUserProfile(id);
    }

    @Test
    void testUpdateUserProfile_Success() {
        // Setup
        long id = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setConfirmPassword("password");

        User existingProfile = new User();
        existingProfile.setId(id);
        existingProfile.setFullname("John Doe");
        existingProfile.setEmail("john@example.com");

        when(profileService.getUserProfile(id)).thenReturn(existingProfile);
        when(passwordEncoder.matches(request.getConfirmPassword(), existingProfile.getPassword())).thenReturn(true);

        // Execution
        ResponseEntity<String> responseEntity = profileController.updateUserProfile(id, request);

        // Verification
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User profile updated successfully!", responseEntity.getBody());
        verify(profileService, times(1)).getUserProfile(id);
        verify(profileService, times(1)).saveUser(existingProfile);
    }

    @Test
    void testUpdateUserProfile_PasswordsDoNotMatch() {
        // Setup
        long id = 1L;
        UserUpdateRequest request = new UserUpdateRequest();
        request.setConfirmPassword("password");

        User existingProfile = new User();
        existingProfile.setId(id);
        existingProfile.setFullname("John Doe");
        existingProfile.setEmail("john@example.com");

        when(profileService.getUserProfile(id)).thenReturn(existingProfile);
        when(passwordEncoder.matches(request.getConfirmPassword(), existingProfile.getPassword())).thenReturn(false);

        // Execution
        ResponseEntity<String> responseEntity = profileController.updateUserProfile(id, request);

        // Verification
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("Passwords do not match", responseEntity.getBody());
        verify(profileService, times(1)).getUserProfile(id);
        verify(profileService, never()).saveUser(existingProfile);
    }
    @Test
    public void testUpdateUserRole_Success() {
        // Mocking
        Long id = 1L;
        User existingUser = new User();
        existingUser.setRole("USER");
        when(profileService.getUserProfile(id)).thenReturn(existingUser);

        User updatedUser = new User();
        updatedUser.setRole("ADMIN");

        // Execution
        ResponseEntity<String> responseEntity =profileController.updateUserRole(id, updatedUser);

        // Assertion
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User role updated successfully!", responseEntity.getBody());
        assertEquals("ADMIN", existingUser.getRole()); // Verify that the role is updated in the user object
        verify(profileService, times(1)).saveUser(existingUser); // Verify that saveUser is called with the updated user
    }

    @Test
    public void testUpdateUserRole_UserNotFound() {
        // Mocking
        Long id = 1L;
        when(profileService.getUserProfile(id)).thenReturn(null);

        User updatedUser = new User();
        updatedUser.setRole("ADMIN");

        // Execution
        ResponseEntity<String> responseEntity = profileController.updateUserRole(id, updatedUser);

        // Assertion
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
        verify(profileService, never()).saveUser(any()); // Verify that saveUser is never called
    }
    @Test
    public void testGetAllUserProfiles_Success() {
        // Mocking
        List<User> mockUserProfiles = new ArrayList<>();
        mockUserProfiles.add(new User());
        mockUserProfiles.add(new User());
        when(profileService.getAllUserProfiles()).thenReturn(mockUserProfiles);

        // Execution
        ResponseEntity<List<User>> responseEntity = profileController.getAllUserProfiles();

        // Assertion
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockUserProfiles, responseEntity.getBody());
    }

    @Test
    public void testGetAllUserProfiles_EmptyList() {
        // Mocking
        List<User> mockUserProfiles = new ArrayList<>();
        when(profileService.getAllUserProfiles()).thenReturn(mockUserProfiles);

        // Execution
        ResponseEntity<List<User>> responseEntity = profileController.getAllUserProfiles();

        // Assertion
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockUserProfiles, responseEntity.getBody());
    }
    @Test
    public void testDeleteUserProfile_Success() {
        // Mocking
        Long id = 1L;
        doNothing().when(profileService).deleteUserProfile(id);

        // Execution
        ResponseEntity<String> responseEntity = profileController.deleteUserProfile(id);

        // Assertion
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User profile deleted successfully!", responseEntity.getBody());
        verify(profileService).deleteUserProfile(id); // Verify that deleteUserProfile method is called with the correct id
    }

    @Test
    public void testUpdateUserProfile_WithNonExistingProfile_ReturnsNotFound() {
        // Mock ProfileService
        ProfileService profileService = mock(ProfileService.class);

        // Mock PasswordEncoder
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        // Mock Profile Update Request
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFullname("Updated Fullname");
        request.setEmail("updated@example.com");
        request.setConfirmPassword("password");

        // Mock existing profile as null
        when(profileService.getUserProfile(1L)).thenReturn(null);

        // Create an instance of ProfileController
        ProfileController profileController = new ProfileController(profileService, passwordEncoder);

        // Call the updateUserProfile method and capture the response
        ResponseEntity<String> responseEntity = profileController.updateUserProfile(1L, request);

        // Verify that the response entity returns not found status
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(null, responseEntity.getBody());
    }
}
