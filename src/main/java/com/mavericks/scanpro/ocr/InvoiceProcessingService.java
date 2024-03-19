package com.mavericks.scanpro.ocr;

import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.BinaryData;

import com.mavericks.scanpro.entities.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Service
public class InvoiceProcessingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceProcessingService.class);

    @Value("${azure.ocr.endpoint}")
    private String endpoint;

    @Value("${azure.ocr.subscription.key}")
    private String key;


    public String extractTextFromDocument(byte[] document) {
        try {
            DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
                    .credential(new AzureKeyCredential(key))
                    .endpoint(endpoint)
                    .buildClient();

            SyncPoller<OperationResult, AnalyzeResult> analyzeDocumentPoller = client.beginAnalyzeDocument(
                    "prebuilt-invoice",
                    BinaryData.fromBytes(document)
            );

            AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();
            StringBuilder extractedText = new StringBuilder();
            StringBuilder extractedTexttempo = new StringBuilder();
            for (AnalyzedDocument analyzedDocument : analyzeResult.getDocuments()) {
                Map<String, DocumentField> fields = analyzedDocument.getFields();
                for (Map.Entry<String, DocumentField> entry : fields.entrySet()) {
                    String fieldName = entry.getKey();
                    DocumentField documentField = entry.getValue();
                    Object fieldValue = documentField.getValue();

                        extractedTexttempo.append(fieldName).append(": ").append(fieldValue).append(" ");

                    if (fieldValue instanceof String || fieldValue instanceof LocalDate || fieldValue instanceof Double) {
                        extractedText.append(fieldName).append(": ").append(fieldValue).append("\n");
                    } else if (fieldValue instanceof AddressValue) {
                        extractedText.append(fieldName).append(": ").append( ((AddressValue) fieldValue).getCity()+" "+((AddressValue) fieldValue).getStreetAddress() +" "+ ((AddressValue) fieldValue).getPostalCode()).append("\n");
                    } else if (fieldValue instanceof CurrencyValue) {
                        extractedText.append(fieldName).append(": ").append(((CurrencyValue) fieldValue).getAmount()).append("\n");
                    } else {
                        LOGGER.warn("Field '{}' is of an unexpected type. Value: {}", fieldName, fieldValue);
                    }
                }
            }
            System.out.println(extractedText.toString());
            return extractedText.toString();

        } catch (Exception e) {
            LOGGER.error("Error occurred while extracting text from the document: {}", e.getMessage());
            return "Error occurred while extracting text from the document: " + e.getMessage();
        }
    }


    public Invoice ProcessTextFromDocument(byte[] document) {
        try {
            String extractedText = extractTextFromDocument(document);
            String[] lines = extractedText.split("\n");
            Invoice invoice = new Invoice();
            for (String line : lines) {
                String[] parts = line.split(": ");
                if (parts.length == 2) {
                    String fieldName = parts[0];
                    String fieldValue = parts[1];
                    switch (fieldName) {
                    case "InvoiceId":
                        invoice.setInvoiceId(fieldValue);
                        break;
                    case "SubTotal":
                        invoice.setSubTotal(Double.parseDouble(fieldValue));
                        break;
                    case "TotalTax":
                        invoice.setTotalTax(Double.parseDouble(fieldValue));
                        break;
                    case "CustomerName":
                        invoice.setCustomerName(fieldValue);
                        break;
                    case "InvoiceDate":
                        invoice.setInvoiceDate(LocalDate.parse(fieldValue));
                        break;
                    case "AmountDue":
                        invoice.setAmountDue(Double.parseDouble(fieldValue));
                        break;
                    case "InvoiceTotal":
                        invoice.setInvoiceTotal(Double.parseDouble(fieldValue));
                        break;
                    case "BillingAddress":
                        invoice.setBillingAddress(fieldValue);
                        break;
                    default:
                        break;
                }
            }
        }
            return invoice;
    } catch (NumberFormatException e) {
        LOGGER.error("Error occurred while parsing numeric value: {}", e.getMessage());
    } catch (
    DateTimeParseException e) {
        LOGGER.error("Error occurred while parsing date: {}", e.getMessage());
    } catch (Exception e) {
        LOGGER.error("Error occurred while extracting and storing text from the document: {}", e.getMessage());
    }
        return null ;
}

}