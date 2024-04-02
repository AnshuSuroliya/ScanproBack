package com.mavericks.scanpro.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.requests.UserUpdateRequest;
import com.mavericks.scanpro.services.ProfileService;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@RestController
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private ProfileService profileService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public ProfileController(ProfileService profileService, PasswordEncoder passwordEncoder) {
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/getUserProfile/{id}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long id) {
        LOGGER.info("Fetching user profile with id: {}", id);
        User userProfile = profileService.getUserProfile(id);
        if (userProfile != null) {
            LOGGER.info("User profile found: {}", userProfile);
            return ResponseEntity.ok(userProfile);
        } else {
            LOGGER.warn("User profile not found for id: {}", id);
            return ResponseEntity.notFound().build(); // Return 404 if user profile not found
        }
    }

    @DeleteMapping("/deleteUserProfile/{id}")
    public ResponseEntity<String> deleteUserProfile(@PathVariable Long id) {
        LOGGER.info("Deleting user profile with id: {}", id);
        profileService.deleteUserProfile(id);
        LOGGER.info("User profile deleted successfully for id: {}", id);
        return ResponseEntity.ok("User profile deleted successfully!");
    }

    @GetMapping("/getUserProfiles")
    public ResponseEntity<List<User>> getAllUserProfiles() {
        LOGGER.info("Fetching all user profiles");
        List<User> userProfiles = profileService.getAllUserProfiles();
        LOGGER.info("Fetched {} user profiles", userProfiles.size());
        return ResponseEntity.ok(userProfiles);
    }

    @PutMapping("/updateUserProfile/{id}")
    public ResponseEntity<String> updateUserProfile(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        LOGGER.info("Updating user profile with id: {}", id);
        User existingProfile = profileService.getUserProfile(id);
        if (existingProfile != null) {
            // Check if passwords match
            if (!passwordEncoder.matches(request.getConfirmPassword(), existingProfile.getPassword())) {
                LOGGER.warn("Passwords do not match for updating user profile with id: {}", id);
                return ResponseEntity.badRequest().body("Passwords do not match");
            }

            // Update other profile details
            existingProfile.setFullname(request.getFullname());
            existingProfile.setEmail(request.getEmail());

            // Save the updated profile
            profileService.saveUser(existingProfile);

            LOGGER.info("User profile updated successfully for id: {}", id);
            return ResponseEntity.ok("User profile updated successfully!");
        } else {
            LOGGER.warn("User profile not found for updating with id: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/updateUserRole/{id}")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestBody User user) {
        LOGGER.info("Updating user role with id: {}", id);
        User existingProfile = profileService.getUserProfile(id);
        if (existingProfile != null) {
            // Update the user's role
            existingProfile.setRole(user.getRole()); // Assuming you have a getter and setter for role in User class
            profileService.saveUser(existingProfile); // Save the updated user to the database

            LOGGER.info("User role updated successfully for id: {}", id);
            return ResponseEntity.ok("User role updated successfully!");
        } else {
            LOGGER.warn("User profile not found for updating role with id: {}", id);
            return ResponseEntity.notFound().build(); // Return 404 if user not found
        }
    }
}
