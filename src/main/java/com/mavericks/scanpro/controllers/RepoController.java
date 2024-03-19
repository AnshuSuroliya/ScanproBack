package com.mavericks.scanpro.controllers;

import com.mavericks.scanpro.entities.Repository;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.RepositoryRepo;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.RepoHandleReqDTO;
import com.mavericks.scanpro.requests.UpdateRepoAccess;
import com.mavericks.scanpro.response.RepoHandleResDTO;
import com.mavericks.scanpro.response.SingleReposFetchResponse;
import com.mavericks.scanpro.security.jwt.JwtUtils;
import com.mavericks.scanpro.services.GithubFileServiceImpl;
import com.mavericks.scanpro.services.RepoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/repo")
public class RepoController {
    Logger log = LoggerFactory.getLogger(RepoController.class);
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RepoServiceImpl repoService;

    @Autowired
    private GithubFileServiceImpl githubFileService;

    @GetMapping("/getAll")
    private RepoHandleResDTO getAllReposOfAccount(@RequestHeader("Authorization") String headers) {
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError ocured during finding all repos of user : ",e);
            return null;
        }

        return repoService.getallRepos(user);
    }

    @GetMapping("/get")
    private SingleReposFetchResponse getRepo(@RequestHeader("Authorization") String headers, @Param("name") String name) {
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError ocured during loading the requested Repo : ",e);
            return null;
        }

        return repoService.getRepo(user,name);
    }


    @PostMapping("/create")
    private RepoHandleResDTO createNewREpo(@RequestBody RepoHandleReqDTO req, @RequestHeader("Authorization") String headers) {
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError ocured during parsing Jwt : ",e);
            return null;
        }
        return githubFileService.createRepo(req,user);
    }

    @GetMapping("/checkAccess")
    @ResponseBody
    private  Boolean checkAccess(@Param("name")String name,@Param("owner")Long owner,@RequestHeader("Authorization") String headers){
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError ocured during parsing Jwt : ",e);
            return null;
        }

        return repoService.checkaccess(user,name,owner);
    }

    @PutMapping("/updateAccess")
    private RepoHandleResDTO removeAccess(@RequestBody UpdateRepoAccess req, @RequestHeader("Authorization") String headers) {
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError occured during parsing Jwt : ",e);
            return null;
        }

       return repoService.UpdateAccess(user,req);

    }

    @PutMapping("/delete")
    private RepoHandleResDTO deleteRepo(@RequestBody RepoHandleReqDTO req, @RequestHeader("Authorization") String headers){
        User user = null;
        try {
            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                user = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken));
            }
        } catch (Exception e) {
            log.error("someError ocured during parsing Jwt : ",e);
            return null;
        }

        return  repoService.DeleteRepo(user,req);

    }


}