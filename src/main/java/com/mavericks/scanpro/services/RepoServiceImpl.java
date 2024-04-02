package com.mavericks.scanpro.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavericks.scanpro.entities.GitCreds;
import com.mavericks.scanpro.entities.Repository;
import com.mavericks.scanpro.entities.User;
import com.mavericks.scanpro.repositories.RepositoryRepo;
import com.mavericks.scanpro.repositories.UserRepo;
import com.mavericks.scanpro.requests.RepoHandleReqDTO;
import com.mavericks.scanpro.requests.UpdateRepoAccess;
import com.mavericks.scanpro.response.RepoHandleResDTO;
import com.mavericks.scanpro.response.SingleReposFetchResponse;
import com.mavericks.scanpro.security.jwt.JwtUtils;
import com.mavericks.scanpro.services.interfaces.RepoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class RepoServiceImpl implements RepoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoServiceImpl.class);
    @Autowired
    private UserRepo userRepo;

    @Value("${Github.Base.url}")
    private String base_git_url;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RepositoryRepo repositoryRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RepoServiceImpl repoService;

    @Autowired
    private GithubFileServiceImpl githubFileService;



    public RepoHandleResDTO getallRepos(User user)
    {
        if (user == null) return null;

        Set<Repository> repos = user.getRepoaccess();
        LOGGER.info("fetched Repo : {}",repos);

        getallUserRepos(user);

        RepoHandleResDTO res = new RepoHandleResDTO();
        res.setRepos(repos);
        return res;
    }

    public RepoHandleResDTO UpdateAccess(User owner, UpdateRepoAccess req)
    {
        RepoHandleResDTO res = new RepoHandleResDTO();

        try{
            Repository repo = repositoryRepo.findByName(req.getName());

            if (!Objects.equals(repo.getOwner(), owner.getId())) {
                res.setMessage("You are not the Owner");
                LOGGER.error("Only Owner can Update Acces permissions!");
                return res;
            }

            Set<String> EmailList = req.getEmaillist();
            Set<User> accessList = new HashSet<>();

            Set<User> previousUsers = repo.getAccessUser();
            for (User prevuser:previousUsers) {
                Set<Repository> userAccesses = prevuser.getRepoaccess();
                userAccesses.remove(repo);
                userRepo.save(prevuser);
            }

            for (String email: EmailList) {
                User u = userRepo.findByEmail(email.toString());
                if (u != null) {
                    Set<Repository> userAccesses = u.getRepoaccess();
                    userAccesses.add(repo);
                    u.setRepoaccess(userAccesses);
                    userRepo.save(u);
                    accessList.add(u);
                }
            }

            Set<Repository> userAccesses  =owner.getRepoaccess();
            userAccesses.add(repo);
            owner.setRepoaccess(userAccesses);
            userRepo.save(owner);
            repo.setAccessUser(accessList);
            repositoryRepo.save(repo);

            LOGGER.info("Access Updated");
            res.setMessage("Access Updated!");


        }catch (NullPointerException e){
            LOGGER.error("NullPointer Exception occured :",e);
            res.setMessage("Access Update Failed!");
        }catch (Exception e){
            LOGGER.error("Some error occured :",e);
            res.setMessage("Access Update Failed!");
        }

        return  res;
    }

    public RepoHandleResDTO DeleteRepo(User owner,RepoHandleReqDTO req)
    {
        RepoHandleResDTO res = new RepoHandleResDTO();
        Repository repo = repositoryRepo.findByName(req.getName());

        if (!Objects.equals(repo.getOwner(), owner.getId())) {
            res.setMessage("You are not the Owner");
            LOGGER.error("Only Owner can delete A Repo!");
            return res;
        }

        if(repo!=null && Objects.equals(repo.getOwner(), owner.getId())){
            Set<User> accessors = repo.getAccessUser();
            for(User tempuser : accessors) {
                Set<Repository> re = tempuser.getRepoaccess();
                re.remove(repo);
                tempuser.setRepoaccess(re);
                userRepo.save(tempuser);
            }
            repo.setAccessUser(null);
            repositoryRepo.save(repo);
            repositoryRepo.delete(repo);
        }


        res.setMessage("Deleted!");
        return  res;
    }

    public SingleReposFetchResponse getRepo(User user, String name) {
        System.out.println(name);

        SingleReposFetchResponse res =new SingleReposFetchResponse();
        Repository repo = repositoryRepo.findByName(name);
        res.setOwner_email(userRepo.findById(repo.getOwner()).get().getEmail());
        if(!repo.getAccessUser().contains(user)){
            res.setMeassage("Not allowed!");
            LOGGER.error("Cannot open repo without access permission!");
            return res;
        }
        res.setRepo(repo);
        res.setMeassage("Repo Found!");
        return res;

    }

    public Boolean checkaccess(User user, String name,Long owner) {
        try{
        Repository repo = repositoryRepo.findByNameAndOwner(name,owner);
        return  repo.getAccessUser().contains(user);}
        catch (Exception e){
            return false;
        }
    }


    public void getallUserRepos(User user) {
        GitCreds creds =user.getGitCreds();

        RepoHandleResDTO response =new RepoHandleResDTO();
        Set<Repository> repos =new HashSet<>();

        JsonNode res=null;
        HttpStatus status;
        try{
            String path = base_git_url+"user/repos";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken());
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.GET,requestEntity, String.class);

            res = objectMapper.readTree(responseEntity.getBody());
            status = (HttpStatus) responseEntity.getStatusCode();
            LOGGER.info("response: {} ",res);

            for(JsonNode mp: res){
                System.out.println(mp);
                Repository r =repositoryRepo.findByNameAndOwner(mp.get("name").asText(),user.getId());
                if(r!=null){
                    repos.add(r);
                }else{
                    r =new Repository();

                    Set<User> accessuser = new HashSet<>();
                    accessuser.add(user);
                    Set<Repository> accesses =user.getRepoaccess();

                    r.setId(mp.get("id").asLong());
                    r.setName(mp.get("name").asText());
                    r.setOwner(user.getId());
                    r.setDescription("");
                    r.setAccessUser(accessuser);
                    repositoryRepo.save(r);
                    accesses.add(r);
                    user.setRepoaccess(accesses);
                    userRepo.save(user);

                    repos.add(r);
                }
            }

            response.setRepos(repos);

        }catch (JsonProcessingException e){
            LOGGER.error("Response parse error : ",e);
            return;
        } catch (Exception e){
            LOGGER.error("some Error Occured :",e);
            return ;
        }


        return;
    }
}
