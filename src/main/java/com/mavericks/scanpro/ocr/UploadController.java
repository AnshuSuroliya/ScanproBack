package com.mavericks.scanpro.ocr;
 
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
 
@RestController
@RequestMapping("/api")
public class UploadController {
	 
    private final InvoiceProcessingService invoiceProcessingService;
 
    public UploadController(InvoiceProcessingService invoiceProcessingService) {
        this.invoiceProcessingService = invoiceProcessingService;
    }
 
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes(); 
            return invoiceProcessingService.extractTextFromDocument(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred while uploading the file: " + e.getMessage();
        }
    }
}

