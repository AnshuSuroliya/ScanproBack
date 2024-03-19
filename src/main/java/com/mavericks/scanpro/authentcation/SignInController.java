package com.mavericks.scanpro.authentcation;

import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.SignInRequest;
import com.mavericks.scanpro.response.AuthResponseDTO;
import com.mavericks.scanpro.security.UserDetails;
import com.mavericks.scanpro.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SignInController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponseDTO> signIn(@RequestBody SignInRequest signInRequest) {
        AuthResponseDTO res =new AuthResponseDTO();

        if (!isValidEmail(signInRequest.getEmail())) {
            res.setSuccess(false);
            res.setMessage("Invalid email format!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        // Check if password meets the complexity requirements
        if (!isValidPassword(signInRequest.getPassword())) {
            res.setSuccess(false);
            res.setMessage("Password must contain at least 8 characters with minimum 3 numbers and a special character!");
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        User user=userRepo.findByEmail(signInRequest.getEmail());

        if(user==null){
            res.setSuccess(false);
            res.setMessage("Email not Registred!");

            return new ResponseEntity<AuthResponseDTO>(res,HttpStatus.OK);
        }
        System.out.println(passwordEncoder.encode(signInRequest.getPassword()) +"--"+ user.getPassword());



        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInRequest.getEmail(), signInRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwtToken = jwtUtils.generateJwtToken(authentication);

        // Get UserDetails from authenticated user
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        res.setSuccess(true);
        res.setMessage("User Logged In!");
        res.setToken(jwtToken);

        return new ResponseEntity<AuthResponseDTO>(res,HttpStatus.OK);
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













