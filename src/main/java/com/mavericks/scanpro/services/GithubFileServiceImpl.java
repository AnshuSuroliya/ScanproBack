package com.mavericks.scanpro.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mavericks.scanpro.controllers.DocumentController;
import com.mavericks.scanpro.entities.*;
import com.mavericks.scanpro.ocr.InvoiceProcessingService;
import com.mavericks.scanpro.repositories.*;
import com.mavericks.scanpro.requests.RepoHandleReqDTO;
import com.mavericks.scanpro.response.FetchFileResDTO;
import com.mavericks.scanpro.response.FileExplorerResDTO;
import com.mavericks.scanpro.response.RepoHandleResDTO;
import com.mavericks.scanpro.response.SaveFileResDTO;
import com.mavericks.scanpro.services.interfaces.GithubFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Date;
import java.util.*;

@Service
public class GithubFileServiceImpl implements GithubFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    GithubFileRepo githubFileRepo;

    @Autowired
    ScannedFileRepo scannedFileRepo;

    @Autowired
    InvoiceProcessingService invoiceProcessingService;

    @Autowired
    AddressRepo addressRepo;

    @Autowired
    RepositoryRepo repositoryRepo;

    @Autowired
    UserRepo userRepo;

    @Autowired
    GitCredRepo gitCredRepo;

    @Value("${Github.auth.code}")
    private String auth_Code;

    @Value("${Github.url}")
    private String git_url;

    @Value("${Github.Base.url}")
    private String base_git_url;


    private boolean AlreadyPresent(String path){
        return githubFileRepo.findByPath(path) != null;
    }

    public FileExplorerResDTO FileExplorerGithub(String repoName,String path,Long Owner) {
        FileExplorerResDTO response = new FileExplorerResDTO();

        try {
            GitCreds creds =userRepo.findById(Owner).get().getGitCreds();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken() );
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            JsonNode res = null;
            String abs_url = base_git_url+"repos/"+creds.getUsername()+"/"+repoName+"/contents/"+path;
            LOGGER.info(abs_url);

            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> Restresponse = restTemplate.exchange(abs_url,HttpMethod.GET,requestEntity , String.class);
            LOGGER.info(Restresponse.getBody());
            res = objectMapper.readTree(Restresponse.getBody());
            LOGGER.info("Fetch response :{}", res);

            ArrayList<HashMap<String,String>> arr =new ArrayList<>();

            for(int i =0;res!=null && i<res.size();i++){
                HashMap<String,String> mp =new HashMap<>();
                mp.put("name",res.get(i).get("name").asText() );
                mp.put("type",res.get(i).get("type").asText());
                mp.put("size",res.get(i).get("size").asText());
                mp.put("path",abs_url+res.get(i).get("name").asText());
                arr.add(i,mp);
            }
            response.setFiles(arr);

        }catch (JsonParseException e){
            LOGGER.error("Error occured during parsing json from rest api :{}", e.toString());
            response.setFiles(null);
        }
        catch (Exception e) {
            LOGGER.error("Some Error Occured :{}", e.toString());
        }

        return  response;
    }

    public FetchFileResDTO FetchFromGithub(String path,Long owner) {
        FetchFileResDTO response = new FetchFileResDTO();
        GitCreds creds = userRepo.findById(owner).get().getGitCreds();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken());
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            JsonNode res = null;

            HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
            ResponseEntity<String> Restresponse = restTemplate.exchange(path,HttpMethod.GET,requestEntity , String.class);
            res = objectMapper.readTree(Restresponse.getBody());
            LOGGER.info("Fetch response :{}", res);

            Scanned_files scannedFile = githubFileRepo.findByPath(path).getScannedFile();

            response.setName(res.get("name").asText());
            response.setSha(res.get("sha").asText());
            response.setContent(res.get("content").asText());
            response.setScannedData(scannedFile);
            response.setAddress(scannedFile.getAddress());


        }catch (JsonParseException e){
            LOGGER.error("Error occured during parsing json from rest api :{}", e.toString());
        }
        catch (Exception e) {
            LOGGER.error("Some Error Occured :{}", e.toString());
        }

        return  response;
    }

    public SaveFileResDTO UploadOriginalDoc(String name,String path,String repoName,Long Owner, String base64String) {
        SaveFileResDTO response =new SaveFileResDTO();

        GitCreds creds =userRepo.findById(Owner).get().getGitCreds();

        String abs_path  = String.format("%srepos/%s/%s/contents/%s%s",base_git_url,creds.getUsername(),repoName,path,name);
        //System.out.println(abs_path);

        if(name.indexOf('.')<0){
            abs_path=abs_path+"/INIT";
            name="INIT";
        }

        if(AlreadyPresent(abs_path)){

            response.setSuccess(false);
            response.setMessage("Duplicate");
            return response;
        }

        JsonNode res = null;
        HttpStatus status;

        Boolean created=false;

        try{

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken());
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            String requestBody =String.format("{\"message\":\"random message\",\"content\":\"%s\"}",base64String);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(abs_path, HttpMethod.PUT, requestEntity, String.class);

            res = objectMapper.readTree(responseEntity.getBody());
            status = (HttpStatus) responseEntity.getStatusCode();

            LOGGER.info("response: {} ",res);

        }catch (JsonProcessingException e){
            LOGGER.error("Response parse error : ",e);

            return response;
        } catch (Exception e){
            LOGGER.error("some Error Occured :",e);
            return response;
        }

        try{
            if(status== HttpStatus.CREATED){
                Github_files Git_file = new Github_files();
                Git_file.setName(name);
                Git_file.setSha(res.get("content").get("sha").asText());
                Git_file.setPath(abs_path);
                Git_file.setLast_update(new Date(System.currentTimeMillis()));
                Git_file.setSize(res.get("content").get("size").asLong());
                githubFileRepo.save(Git_file);

                if(name.indexOf('.')>=0) {
                    //Scanning

                    byte[] bArray = Base64.getDecoder().decode(base64String);
                    Invoice invoice = invoiceProcessingService.ProcessTextFromDocument(bArray);
                    Scanned_files scanned_file = new Scanned_files();
                    scanned_file.setDate(invoice.getInvoiceDate());
                    scanned_file.setTax(invoice.getTotalTax());
                    scanned_file.setSubtotal(invoice.getSubTotal());
                    scanned_file.setTotal_amount(invoice.getInvoiceTotal());
                    scanned_file.setInvoice_id(invoice.getInvoiceId());

                    Address address = new Address();
                    address.setAddress(""+invoice.getBillingAddress());
                    address.setName(""+invoice.getCustomerName());
                    addressRepo.save(address);
                    scanned_file.setAddress(address);
                    scanned_file = scannedFileRepo.save(scanned_file);
                    Git_file.setScannedFile(scanned_file);
                }
                githubFileRepo.save(Git_file);
                response.setSuccess(true);
                response.setMessage("Saved!");
                response.setName(res.get("content").get("name").asText());
                response.setSha(res.get("content").get("sha").asText());
                response.setPath(res.get("content").get("path").asText());

                LOGGER.info("File saved!");
                created=true;
            }
            else{
                response.setSuccess(false);
                response.setMessage("Some Error Occured");
                LOGGER.error("File saving failed!");
            }
        }catch (Exception e){
            if(created){
            DeleteFile(path,Owner);
            }
            response.setSuccess(false);
            response.setMessage("Some Error Occured");
            LOGGER.error("File saving failed!",e);
        }

        return response;
    }

    //left to Update after changing the code having problem updatng the file
    public SaveFileResDTO UpdateOriginalDoc(String name, String path, String sha, String base64String,String repoName,Long Owner) {
        SaveFileResDTO response =new SaveFileResDTO();

        GitCreds creds =userRepo.findById(Owner).get().getGitCreds();

        String abs_path  = String.format("%srepos/%s/%s/contents/%s%s",base_git_url,creds.getUsername(),repoName,path,name);

        if(!AlreadyPresent(abs_path)){
            response.setSuccess(false);
            response.setMessage("Update Invalid");
            LOGGER.info("Cannot update a file which in not present!");
            return response;
        }

        JsonNode res = null;
        HttpStatus status;

        try{

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken());
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            String requestBody =String.format("{\"message\":\"random message\",\"sha\":\"%s\",  \"content\":\"%s\"}",sha,base64String);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(abs_path, HttpMethod.PUT, requestEntity, String.class);

            res = objectMapper.readTree(responseEntity.getBody());
            status = (HttpStatus) responseEntity.getStatusCode();

            LOGGER.info("response: {} ",res);

        }catch (JsonProcessingException e){
            LOGGER.error("Response parse error : ",e);
            return null;
        } catch (Exception e){
            LOGGER.error("some Error Occured :",e);
            return null;
        }

        if(status== HttpStatus.OK){

            Github_files Git_file = githubFileRepo.findByPath(abs_path);
            Scanned_files prev = Git_file.getScannedFile();
            Address prevaddress = prev.getAddress();

            Git_file.setScannedFile(null);
            prev.setAddress(null);

            addressRepo.delete(prevaddress);
            scannedFileRepo.delete(prev);

            Git_file.setName(name);
            Git_file.setSha(res.get("content").get("sha").asText());
            Git_file.setPath(abs_path);
            Git_file.setSize(res.get("content").get("size").asLong());
            Git_file.setLast_update(new Date(System.currentTimeMillis()));
            githubFileRepo.save(Git_file);

            if(name.indexOf('.')>=0) {
                byte[] bArray = Base64.getDecoder().decode(base64String);
                Invoice invoice = invoiceProcessingService.ProcessTextFromDocument(bArray);
                Scanned_files scanned_file = new Scanned_files();
                scanned_file.setDate(invoice.getInvoiceDate());
                scanned_file.setTax(invoice.getTotalTax());
                scanned_file.setSubtotal(invoice.getSubTotal());
                scanned_file.setTotal_amount(invoice.getInvoiceTotal());
                scanned_file.setInvoice_id(invoice.getInvoiceId());

                Address address = new Address();
                address.setAddress(""+invoice.getBillingAddress());
                address.setName(""+invoice.getCustomerName());
                addressRepo.save(address);
                scanned_file.setAddress(address);
                scanned_file = scannedFileRepo.save(scanned_file);
                Git_file.setScannedFile(scanned_file);
            }

            //System.out.println(res);

            response.setSuccess(true);
            response.setMessage("Updated!");
            response.setName(res.get("content").get("name").asText());
            response.setSha(res.get("content").get("sha").asText());
            response.setPath(res.get("content").get("path").asText());
        }
        else{
            LOGGER.error("File updation failed!");
            response.setSuccess(false);
            response.setMessage("Some Error Occured");
        }

        LOGGER.info("File Updatd!");
        return response;
    }

    public RepoHandleResDTO createRepo(RepoHandleReqDTO req, User owner) {
        RepoHandleResDTO res = new RepoHandleResDTO();

        try {
            if (repositoryRepo.findByNameAndOwner(req.getName(), owner.getId()) != null) {
                LOGGER.error("repo with name :" + req.getName() + " already Present!");
                res.setMessage("Name Exist!");
                return res;
            }

                String path = base_git_url + "user/repos/";

                JsonNode resp = null;
                HttpStatus status;

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "token " + owner.getGitCreds().getAuthToken());
                headers.set("Accept", "application/vnd.github.v3+json");

                String createRepoUrl = base_git_url + "user/repos";
                String requestBody = "{\"name\": \"" + req.getName() + "\", \"description\": \"" + " " + "\", \"private\": true, \"auto_init\": false}";

                HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.exchange(createRepoUrl, HttpMethod.POST, requestEntity, String.class);

                resp = objectMapper.readTree(response.getBody());
                status = (HttpStatus) response.getStatusCode();

                LOGGER.info("response: {} ",resp);

            Repository newRepo = new Repository();
            newRepo.setId(Long.parseLong(resp.get("id").asText()));
            newRepo.setName(req.getName().toLowerCase());
            newRepo.setOwner(owner.getId());
            newRepo.setDescription(req.getDescription());
            newRepo = repositoryRepo.save(newRepo);


            Set<String> EmailList = req.getEmailList();
            EmailList.add(owner.getEmail());

            Set<User> accessList = new HashSet<>();

            for (String email : EmailList) {
                User u = userRepo.findByEmail(email);
                if (u != null) {
                    Set<Repository> userAccesses = u.getRepoaccess();
                    userAccesses.add(newRepo);
                    u.setRepoaccess(userAccesses);
                    userRepo.save(u);
                    accessList.add(u);
                }
            }
            newRepo.setAccessUser(accessList);
            repositoryRepo.save(newRepo);
            res.setMessage("Repo Created!");

        }catch (JsonProcessingException e) {
            LOGGER.error("Response parse error : ", e);
            res.setMessage("Repo Creation Failed!");
        } catch (Exception e) {
            LOGGER.error("some Error Occured :", e);
            res.setMessage("Repo Creation Failed!");
        }

        return res;
}

    public ResponseEntity<String> DeleteFile(String Path,Long Owner){

        JsonNode res = null;
        HttpStatus status;

        Github_files file =githubFileRepo.findByPath(Path);
        String sha =file.getSha();

        try{
            GitCreds creds = userRepo.findById(Owner).get().getGitCreds();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/vnd.github+json");
            headers.set("Authorization", "Bearer " + creds.getAuthToken());
            headers.set("X-GitHub-Api-Version", "2022-11-28");

            String requestBody =String.format("{\"message\":\"Deliting this File\",\"sha\":\"%s\"}",sha);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> responseEntity = restTemplate.exchange(Path, HttpMethod.DELETE, requestEntity, String.class);

            res = objectMapper.readTree(responseEntity.getBody());
            status = (HttpStatus) responseEntity.getStatusCode();

            LOGGER.info("response for deleting a file: {} ",res);

        }catch (JsonProcessingException e){
            LOGGER.error("Response parse error : ",e);
            return new ResponseEntity<>("Unable to Delete!",HttpStatus.BAD_REQUEST);
        } catch (Exception e){
            LOGGER.error("some Error Occured :",e);
            return new ResponseEntity<>("Unable to Delete!",HttpStatus.BAD_REQUEST);
        }
        if(status==HttpStatus.OK){

            Scanned_files scanned_file =file.getScannedFile();
            if(scanned_file!=null){
                Address address = scanned_file.getAddress();
                scanned_file.setAddress(null);
                addressRepo.delete(address);
                scannedFileRepo.save(scanned_file);
                file.setScannedFile(null);
                githubFileRepo.save(file);
                scannedFileRepo.delete(scanned_file);
            }

            githubFileRepo.delete(file);

            LOGGER.info("Deleted!");
            return new ResponseEntity<>("Deleted",status);
        }

        LOGGER.info("Unable to delete!");
        return new ResponseEntity<>("Unable to Delete!",status);
    }

    public boolean isFileScanned(String path,Long owner){

        GitCreds creds = userRepo.findById(owner).get().getGitCreds();
        String final_path = base_git_url+"repos/"+creds.getUsername()+"/"+path;
        return  githubFileRepo.findByPath(final_path)!=null;
    }

}