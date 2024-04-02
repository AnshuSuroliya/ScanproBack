package com.mavericks.scanpro.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.CheckRequestDTO;
import com.mavericks.scanpro.security.jwt.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/check")
public class CheckController {
    static final Logger LOGGER = LoggerFactory.getLogger(CheckController.class);
    @Autowired
    UserRepo userRepo;

    @Autowired
    JwtUtils jwtUtils;

    @Value("${Github.Base.url}")
    private String base_git_url;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;


    @GetMapping("/email")
    public Boolean IsEmailRegistred(@Param("email")String email){
        return userRepo.existsByEmail(email);
    }

    @GetMapping("/get/user")
    public User getUserById(@Param("id")Long id){
        return userRepo.findById(id).get();
    }

    @GetMapping("/validateJWT")
    public Boolean IsJwtValidate(@RequestHeader("Authorization") String headers ){
        Long id=0L;
        try{
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                return jwtUtils.validateJwtToken(jwtToken);
            }
            return false;
        }catch (Exception e){
            LOGGER.error("Unable to get id from JWT token!",e);
            return false;
        }
    }

    @PostMapping("/validateAuth")
    public Boolean isAuthTokenValidate(@RequestBody CheckRequestDTO req) {

        try{
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + req.getAuthToken() );
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            JsonNode res = null;
            String abs_url = base_git_url+"user/repos";

            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> Restresponse = restTemplate.exchange(abs_url, HttpMethod.GET,requestEntity , String.class);

            return true;
        } catch (Exception e) {
            return  false;
        }
    }

}
