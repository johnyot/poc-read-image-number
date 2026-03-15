package com.example.imageocr;

import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.protobuf.ByteString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for receipt analysis using Google Document AI (Receipt Parser).
 * Endpoint: POST /api/receipt/analyze
 */
@RestController
@RequestMapping("/api/receipt")
public class ReceiptDonutController {

    private static final Logger logger = LogManager.getLogger(ReceiptDonutController.class);
     private static final String[] SUPPORTED_EXTENSIONS = {"jpg", "jpeg", "png", "bmp", "tiff", "pdf"};

     @Value("${google.documentai.project-id:}")
     private String projectId;

     @Value("${google.documentai.location:us}")
     private String location;

     @Value("${google.documentai.processor-id:}")
     private String processorId;

    /**
     * Accepts an image/pdf receipt, sends it to Google Document AI Receipt Parser,
     * and returns the extracted entities.
     */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeReceipt(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        long fileSize = file.getSize();

        logger.info("=== [DocumentAI] /api/receipt/analyze called ===");
        logger.info("[Request] filename={}, size={} bytes, contentType={}", filename, fileSize, file.getContentType());

        // Validate file
        if (filename == null || filename.isBlank()) {
            logger.warn("[Request] Rejected: filename is null or empty");
            return ResponseEntity.badRequest().body("ไม่พบชื่อไฟล์");
        }
        if (!isSupportedImage(filename)) {
            logger.warn("[Request] Rejected: {} is not a supported image type", filename);
            return ResponseEntity.badRequest().body("รับเฉพาะไฟล์ภาพเท่านั้น (jpg, jpeg, png, bmp, tiff)");
        }
        if (fileSize == 0) {
            logger.warn("[Request] Rejected: file is empty");
            return ResponseEntity.badRequest().body("ไฟล์ว่างเปล่า");
        }

        if (projectId.isBlank() || processorId.isBlank() || location.isBlank()) {
            logger.error("[Config] Missing required config: google.documentai.project-id/location/processor-id");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Google Document AI ยังไม่ถูกตั้งค่า (project-id/location/processor-id)");
        }

        try {
            byte[] imageBytes = file.getBytes();
            logger.debug("[Request] Read {} bytes from file", imageBytes.length);

            String mimeType = resolveMimeType(filename, file.getContentType());
            logger.debug("[Request] Using mimeType={}", mimeType);

            String endpoint = String.format("%s-documentai.googleapis.com:443", location);
            String processorName = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            logger.info("[Request] Google endpoint={}", endpoint);
            logger.info("[Request] processorName={}", processorName);

            DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .build();

            try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
                RawDocument rawDocument = RawDocument.newBuilder()
                    .setContent(ByteString.copyFrom(imageBytes))
                    .setMimeType(mimeType)
                    .build();

                ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName)
                    .setRawDocument(rawDocument)
                    .build();

                logger.info("[Request] Calling Document AI processDocument");
                ProcessResponse response = client.processDocument(request);
                Document document = response.getDocument();
                Map<String, Object> payload = buildResponsePayload(document);

                logger.info("[Response] entitiesCount={}, textLength={}",
                    document.getEntitiesCount(), document.getText() == null ? 0 : document.getText().length());
                logger.debug("[Response] payload={}", payload);
                logger.info("=== [DocumentAI] Done ===");

                return ResponseEntity.ok(payload);
            }
        } catch (IOException e) {
            logger.error("[Request] Failed to read file {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("ไม่สามารถอ่านไฟล์ได้: " + e.getMessage());
        } catch (com.google.api.gax.rpc.UnauthenticatedException e) {
            logger.error("[Response] Authentication failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Authentication กับ Google Cloud ไม่สำเร็จ (ตรวจสอบ GOOGLE_APPLICATION_CREDENTIALS)");
        } catch (com.google.api.gax.rpc.NotFoundException e) {
            logger.error("[Response] Processor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("ไม่พบ Google Document AI Processor ตามค่าที่ตั้งไว้");
        } catch (com.google.api.gax.rpc.PermissionDeniedException e) {
            logger.error("[Response] Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Service Account ไม่มีสิทธิ์เรียกใช้งาน Document AI Processor");
        } catch (com.google.api.gax.rpc.ResourceExhaustedException e) {
            logger.error("[Response] Quota exceeded: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("เกินโควต้า Document AI");
        } catch (Exception e) {
            logger.error("[Request] Unexpected error for file {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Google Document AI error: " + e.getMessage());
        }
    }

    private boolean isSupportedImage(String filename) {
        String lower = filename.toLowerCase();
        for (String ext : SUPPORTED_EXTENSIONS) {
            if (lower.endsWith("." + ext)) return true;
        }
        return false;
    }

    private String resolveMimeType(String filename, String contentType) {
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".bmp")) return "image/bmp";
        if (lower.endsWith(".tiff")) return "image/tiff";
        return "image/jpeg";
    }

    private Map<String, Object> buildResponsePayload(Document document) {
        Map<String, Object> payload = new LinkedHashMap<>();
        List<Map<String, Object>> entities = new ArrayList<>();

        for (Document.Entity entity : document.getEntitiesList()) {
            Map<String, Object> entityMap = new LinkedHashMap<>();
            entityMap.put("type", entity.getType());
            entityMap.put("value", entity.getMentionText());
            entityMap.put("confidence", entity.getConfidence());

            if (entity.getPropertiesCount() > 0) {
                List<Map<String, Object>> properties = new ArrayList<>();
                for (Document.Entity property : entity.getPropertiesList()) {
                    Map<String, Object> propertyMap = new LinkedHashMap<>();
                    propertyMap.put("type", property.getType());
                    propertyMap.put("value", property.getMentionText());
                    propertyMap.put("confidence", property.getConfidence());
                    properties.add(propertyMap);
                }
                entityMap.put("properties", properties);
            }

            entities.add(entityMap);
        }

        payload.put("entities", entities);
        payload.put("text", document.getText());
        payload.put("entityCount", document.getEntitiesCount());
        return payload;
    }
}
