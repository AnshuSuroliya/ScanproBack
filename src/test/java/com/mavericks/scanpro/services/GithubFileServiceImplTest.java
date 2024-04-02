package com.mavericks.scanpro.services;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.mavericks.scanpro.entities.*;
import com.mavericks.scanpro.ocr.InvoiceProcessingService;
import com.mavericks.scanpro.repositories.*;
import com.mavericks.scanpro.response.FetchFileResDTO;
import com.mavericks.scanpro.response.FileExplorerResDTO;
import com.mavericks.scanpro.response.RepoHandleResDTO;
import com.mavericks.scanpro.response.SaveFileResDTO;
import com.mavericks.scanpro.services.GithubFileServiceImpl;
import com.mavericks.scanpro.services.interfaces.GithubFileService;
import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Date;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class GithubFileServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private GithubFileRepo githubFileRepo;

    @Mock
    private ScannedFileRepo scannedFileRepo;

    @Mock
    private InvoiceProcessingService invoiceProcessingService;

    @Mock
    private AddressRepo addressRepo;

    @Mock
    private RepositoryRepo repositoryRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private GitCredRepo gitCredRepo;

    @InjectMocks
    private GithubFileServiceImpl githubFileService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @SneakyThrows
    @Test
    void testFileExplorerGithub() {
        String repoName = "testRepo";
        String path = "sample/path";
        Long owner = 1L;

        FileExplorerResDTO expectedResponse = new FileExplorerResDTO();
        ArrayList<HashMap<String, String>> files = new ArrayList<>();
        HashMap<String, String> file1 = new HashMap<>();
        file1.put("name", "TestFile1");
        file1.put("type", "File");
        file1.put("size", "1024");
        file1.put("path", "sample/path/TestFile1");
        files.add(file1);
        expectedResponse.setFiles(files);

        JsonNode MockNode = objectMapper.createObjectNode();



        GitCreds cred =new GitCreds(1L,new User(),"username","token");
        User mockUser =new User(1L,"Rohit","rohit@gmail.com",false,"password","ADMIN",null,null,cred,null);

        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>("[{\"name\":\"TestFile1\",\"type\":\"File\",\"size\":\"1024\"}]", HttpStatus.OK);
        when(restTemplate.exchange(anyString(), any(), any(), eq(String.class))).thenReturn(mockResponseEntity);
        when(userRepo.findById(any())).thenReturn(Optional.of(mockUser));

        FileExplorerResDTO actualResponse = githubFileService.FileExplorerGithub(repoName, path, owner);


        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getFiles().size(), actualResponse.getFiles().size());
        assertEquals(expectedResponse.getFiles().get(0).get("name"), actualResponse.getFiles().get(0).get("name"));
        assertEquals(expectedResponse.getFiles().get(0).get("type"), actualResponse.getFiles().get(0).get("type"));
        assertEquals(expectedResponse.getFiles().get(0).get("size"), actualResponse.getFiles().get(0).get("size"));
        assertEquals(expectedResponse.getFiles().get(0).get("path"), actualResponse.getFiles().get(0).get("path"));
    }

}
