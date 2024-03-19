package com.mavericks.scanpro.controllers;

import com.mavericks.scanpro.entities.Github_files;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.*;
import com.mavericks.scanpro.response.FetchFileResDTO;
import com.mavericks.scanpro.response.FileExplorerResDTO;
import com.mavericks.scanpro.response.SaveFileResDTO;
import com.mavericks.scanpro.security.jwt.JwtUtils;
import com.mavericks.scanpro.services.GithubFileServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;

@RestController
@RequestMapping("/repository")
public class DocumentController {
    Logger log = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private GithubFileServiceImpl git_Service;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/fetch/content")
    @ResponseBody
    private ResponseEntity<?> fetchExplorerDoc(@RequestBody FetchFileReqDTO req){
        System.out.println(req);

        FileExplorerResDTO response =git_Service.FileExplorerGithub(req.getReponame(),req.getPath(),req.getOwner());
        if(response==null){
            log.error("No Such file directory exixst!");
            return new ResponseEntity<>("Not Found!", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/fetch/file")
    @ResponseBody
    private ResponseEntity<?> fetchOriginalDoc(@RequestBody FetchFileReqDTO req){
        System.out.println(req.getPath());
        FetchFileResDTO response =git_Service.FetchFromGithub(req.getPath(), req.getOwner());
        if(response==null){
            log.error("file not found!");
            return new ResponseEntity<>("Not Found!", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/upload/folder",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    private ResponseEntity<SaveFileResDTO> MakeFoldergDoc(@ModelAttribute UploadFileReqDTO req,@RequestHeader("Authorization") String headers ) throws IOException {
        String base64String ="";

        SaveFileResDTO response = git_Service.UploadOriginalDoc(req.getName(), req.getPath(),req.getRepoName(),req.getOwner(), base64String);
        if(response==null){
            log.error("Unable to upload file");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/upload/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    private ResponseEntity<SaveFileResDTO> uploadOrgDoc(@ModelAttribute UploadFileReqDTO req,@RequestHeader("Authorization") String headers ) throws IOException {
        byte[] bytes = req.getFile().getBytes();
        String base64String = Base64.getEncoder().encodeToString(bytes);
        SaveFileResDTO response = git_Service.UploadOriginalDoc(req.getName(), req.getPath(),req.getRepoName(),req.getOwner(), base64String);
        if(response==null){
            log.error("Unable to upload file");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/update/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    private ResponseEntity<?> updateOrgDoc(@ModelAttribute UpdateFileReqDTO req, @RequestHeader("Authorization") String headers ) throws IOException {
        Long id=0L;
        try{

            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                id = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken)).getId();
            }
        }catch (Exception e){
            log.error("Unable to get id from JWT token!",e);
            return new ResponseEntity<>("Not Found!", HttpStatus.NOT_FOUND);
        }
        byte[] bytes = req.getFile().getBytes();
        String base64String = Base64.getEncoder().encodeToString(bytes);

        SaveFileResDTO response = git_Service.UpdateOriginalDoc(req.getName(), req.getPath(),req.getSha(), base64String,req.getRepoName(),req.getOwner());
        if(response==null){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @PostMapping("/delete/file")
    @ResponseBody
    private String DeleteFile(@RequestBody FetchFileReqDTO req, @RequestHeader("Authorization") String headers ){
        Long id=0L;
        try{

            if (headers != null && headers.startsWith("Bearer ")) {
                String jwtToken = headers.substring(7);
                id = userRepo.findByEmail(jwtUtils.getUserNameFromJwtToken(jwtToken)).getId();
            }
        }catch (Exception e){
            log.error("Unable to get id from JWT token!",e);
            //return new ResponseEntity<>("No Access!",HttpStatus.FORBIDDEN);
        }

        ResponseEntity<String> res = git_Service.DeleteFile(req.getPath(),req.getOwner());
        log.info("final reponse : {}",res.getBody());
        return  res.getBody();
    }


    @PostMapping("/isFileScanned")
    @ResponseBody
    private Boolean isFilescanned( @RequestBody FetchFileReqDTO req){
        System.out.println(req.getPath());
        return git_Service.isFileScanned(req.getPath(), req.getOwner());
    }
}
