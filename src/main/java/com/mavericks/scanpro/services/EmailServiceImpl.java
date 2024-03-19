package com.mavericks.scanpro.services;

import com.mavericks.scanpro.entities.ResetTokens;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.entities.VerificationTokens;
import com.mavericks.scanpro.repositories.ResetTokenRepo;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.repositories.VerificationRepo;
import com.mavericks.scanpro.requests.EmailRequest;
import com.mavericks.scanpro.services.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.sql.Date;
import java.util.UUID;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private VerificationRepo verificationRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ResetTokenRepo resetTokenRepo;

    @Value("${frontend.verification.url}")
    private String FrontendVerifyUrl;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

    public ResponseEntity<String> sendVerificationEmail(String email) {

        String verificationToken = UUID.randomUUID().toString();


        try {
            String verificationLink = FrontendVerifyUrl + verificationToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Email Verification");
            message.setText("Please click the link below to verify your email address:\n" + verificationLink);
            emailSender.send(message);
            LOGGER.info("Successfull Verification mail with code "+verificationToken);

            VerificationTokens token =new VerificationTokens();
            token.setVerificationToken(verificationToken);
            token.setUser(userRepo.findByEmail(email));

            verificationRepo.save(token);

            return ResponseEntity.status(HttpStatus.OK).body("Verification mail sent!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body("Failed to send Verification email.");
        }
    }

    public ResponseEntity<String> sendPasswordResetEmail(EmailRequest request) {
        // Find the user by email
        User user = userRepo.findByEmail(request.getEmail());
        if (user != null) {
            String code = UUID.randomUUID().toString();
            ResetTokens token =new ResetTokens();
            token.setResetToken(code);
            token.setUser(user);
            token.setCreationDate(new Date(System.currentTimeMillis()));
            token =resetTokenRepo.save(token);

            try {
                // Construct the reset password link with the token
                String resetLink = "http://localhost:3000/reset-password?token=" + code;

                // Create a SimpleMailMessage
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Password Reset");
                message.setText("Please click the following link to reset your password:\n" + resetLink);

                // Send the email
                emailSender.send(message);

                // Return success response
                return ResponseEntity.ok("Password reset email sent successfully!");
            } catch (Exception e) {
                // Log the error
                LOGGER.error("Failed to send password reset email: {}", e.getMessage());

                // Return error response
                return ResponseEntity.status(HttpStatus.OK).body("Failed to send password reset email.");
            }
        } else {
            // User not found with the provided email address
            return ResponseEntity.badRequest().body("User not found with the provided email address.");
        }
    }
}







