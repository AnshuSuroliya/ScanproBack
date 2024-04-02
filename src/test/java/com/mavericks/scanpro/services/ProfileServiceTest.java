package com.mavericks.scanpro.services;

import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProfileServiceTest {

    @Mock
    private UserRepo profileRepository;

    @InjectMocks
    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUser_NewUser_Success() {
        User newUser = new User();
        newUser.setEmail("test@example.com");
        newUser.setFullname("Test User");

        when(profileRepository.findByEmail(anyString())).thenReturn(null);
        when(profileRepository.save(any(User.class))).thenReturn(newUser);

        profileService.saveUser(newUser);

        verify(profileRepository, times(1)).save(newUser);
    }

    @Test
    void saveUser_ExistingUser_Success() {
        // Arrange
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setFullname("Old User");

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("test@example.com");
        updatedUser.setFullname("Updated User");

        when(profileRepository.findByEmail(anyString())).thenReturn(existingUser);
        when(profileRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        profileService.saveUser(updatedUser);

        // Assert
        // Verify that the existing user's profile has been updated with the new information
        assertEquals("Updated User", existingUser.getFullname());
        verify(profileRepository, times(1)).save(existingUser);
    }


    @Test
    void getUserProfile_UserExists_Success() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@example.com");
        existingUser.setFullname("Test User");

        when(profileRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));

        User userProfile = profileService.getUserProfile(1L);

        assertEquals(existingUser, userProfile);
    }


    @Test
    void deleteUserProfile_UserDoesNotExist_ExceptionThrown() {
        when(profileRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> profileService.deleteUserProfile(1L));
    }

    @Test
    void getAllUserProfiles_ReturnsListOfUsers() {
        List<User> userList = new ArrayList<>();
        userList.add(new User());
        userList.add(new User());
        userList.add(new User());

        when(profileRepository.findAll()).thenReturn(userList);

        List<User> allUserProfiles = profileService.getAllUserProfiles();

        assertEquals(userList.size(), allUserProfiles.size());
    }
}


