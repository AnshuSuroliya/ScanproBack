package com.mavericks.scanpro.services.interfaces;
import com.mavericks.scanpro.entities.User;
import java.util.List;

public interface ProfileServiceInterface {
	void saveUser(User user);
    User getUserProfile(Long id);
    void deleteUserProfile(Long id);
    List<User> getAllUserProfiles();
}
