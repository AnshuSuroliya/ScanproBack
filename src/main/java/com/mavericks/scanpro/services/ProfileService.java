package com.mavericks.scanpro.services;

import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.services.interfaces.ProfileServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mavericks.scanpro.entities.User;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService implements ProfileServiceInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    @Autowired
    private UserRepo profileRepository;

    public void saveUser(User user) {
        User existingProfile = profileRepository.findByEmail(user.getEmail());
        if (existingProfile!=null) {
            existingProfile.setFullname(user.getFullname());
            profileRepository.save(existingProfile);
            LOGGER.info("Updated user profile: {}", existingProfile);
        } else {
            profileRepository.save(user);
            LOGGER.info("Saved new user profile: {}", user);
        }
    }

    public User getUserProfile(Long id) {
        Optional<User> userProfileOptional = profileRepository.findById(id);
        if (userProfileOptional.isPresent()) {
            User userProfile = userProfileOptional.get();
            LOGGER.info("Retrieved user profile: {}", userProfile);
            return userProfile;
        } else {
            LOGGER.warn("User profile not found for id: {}", id);
            return null;
        }
    }

    public void deleteUserProfile(Long id) {
        Optional<User> userProfileOptional = profileRepository.findById(id);
        if (userProfileOptional.isPresent()) {
            profileRepository.deleteById(id);
            LOGGER.info("Deleted user profile with id: {}", id);
        } else {
            LOGGER.warn("User profile not found for id: {}", id);
            throw new RuntimeException("User profile not found"); // Or return a ResponseEntity with a 404 status code
        }
    }

    public List<User> getAllUserProfiles() {
        List<User> userProfiles = profileRepository.findAll();
        LOGGER.info("Retrieved all user profiles: {}", userProfiles);
        return userProfiles;
    }
}
