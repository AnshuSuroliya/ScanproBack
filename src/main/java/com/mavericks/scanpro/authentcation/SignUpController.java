package com.mavericks.scanpro.authentcation;

import com.mavericks.scanpro.entities.GitCreds;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.GitCredRepo;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.SignUpRequest;
import com.mavericks.scanpro.response.AuthResponseDTO;
import com.mavericks.scanpro.services.EmailServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SignUpController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SignUpController.class);

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    GitCredRepo gitCredRepo;

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
        AuthResponseDTO res = new AuthResponseDTO();

        // Validate feilds
        if (signUpRequest.getFullname() == null || signUpRequest.getFullname().isEmpty()) {
            res.setSuccess(false);
            res.setMessage("Fullname is required!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        if (!isValidEmail(signUpRequest.getEmail())) {
            res.setSuccess(false);
            res.setMessage("Invalid email format!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        // Check if password meets the complexity requirements
        if (!isValidPassword(signUpRequest.getPassword())) {
            res.setSuccess(false);
            res.setMessage("Password must contain at least 8 characters with minimum 3 numbers and a special character!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        // Check if email is already registered
        if (userRepository.findByEmail(signUpRequest.getEmail()) != null) {
            res.setSuccess(false);
            res.setMessage("Email already registered!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }



        // Create a new user and save to the database
        User user = new User();
        user.setFullname(signUpRequest.getFullname());
        user.setEmail(signUpRequest.getEmail());
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        GitCreds gitCreds=new GitCreds();
        gitCreds.setAuthToken(signUpRequest.getAuthToken());
        gitCreds.setUsername(signUpRequest.getGithubUsername());
        gitCreds = gitCredRepo.save(gitCreds);

        user.setGitCreds(gitCreds);
        user.setRole("USER");
        userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(signUpRequest.getEmail());
        } catch (Exception e) {
            LOGGER.error("Email sender failed:", e);

            // If sending email fails, rollback user creation and return error response
            res.setSuccess(false);
            res.setMessage("Unable to send verification email!");
            userRepository.delete(user);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        // If everything is successful, return success response
        res.setSuccess(true);
        res.setMessage("User registered successfully!");
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    private boolean isValidEmail(String email) {
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }


    private boolean isValidPassword(String password) {
        //  At least 8 characters with minimum 3 numbers and a special character
        String regex = "^(?=.*[0-9]{3,})(?=.*[!@#$%^&*])(?=.*[a-zA-Z]).{8,}$";
        return password.matches(regex);
    }
}



