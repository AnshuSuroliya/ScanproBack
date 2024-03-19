package com.mavericks.scanpro.authentcation;


import com.mavericks.scanpro.entities.ResetTokens;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.ResetTokenRepo;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.repositories.VerificationRepo;
import com.mavericks.scanpro.requests.EmailRequest;
import com.mavericks.scanpro.requests.ResetPasswordReqDTO;
import com.mavericks.scanpro.services.EmailServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.UUID;



@RestController
public class EmailController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private ResetTokenRepo resetTokenRepo;

    @Autowired
    private VerificationRepo verificationRepo;

    @Autowired
    private EmailServiceImpl emailService;

    @PostMapping("/api/send/PasswordReset/Email")
    public ResponseEntity<String> sendPasswordResetEmail(@RequestBody EmailRequest request) {
        return emailService.sendPasswordResetEmail(request);
    }

    @PostMapping("/api/reset/Password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordReqDTO req) {
        // Find the user by reset token
        LOGGER.info(req.getToken());
        User user = resetTokenRepo.findByResetToken(req.getToken()).getUser();
        if (user != null) {
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));

            ResetTokens token = resetTokenRepo.findByUser(user);
            resetTokenRepo.delete(token);

            return ResponseEntity.ok("Password reset successfully!");
        } else {
            // Invalid or expired token
            return ResponseEntity.badRequest().body("Invalid or expired token.");
        }
    }

    @GetMapping("/api/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        User user = verificationRepo.findByVerificationToken(token).getUser();
        if (user != null) {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            return ResponseEntity.ok("Email verified successfully!");

        } else {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
    }
}
