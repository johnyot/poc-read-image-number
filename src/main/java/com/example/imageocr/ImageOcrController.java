package com.example.imageocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.springframework.beans.factory.annotation.Value;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/api/ocr")
public class ImageOcrController {
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "bmp", "gif", "tiff"};
    private static final Logger logger = LogManager.getLogger(ImageOcrController.class);

    @PostMapping(value = "/numbers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractNumbers(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        logger.info("Received file: {} (size: {} bytes)", filename, file.getSize());
        if (filename == null || !isImageFile(filename)) {
            logger.warn("Rejected file: {} (not an image)", filename);
            return ResponseEntity.badRequest().body("รับเฉพาะไฟล์ภาพเท่านั้น");
        }
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                logger.warn("File is not a supported image: {}", filename);
                return ResponseEntity.badRequest().body("ไฟล์ที่อัปโหลดไม่ใช่ภาพที่รองรับ");
            }
            logger.debug("Starting OCR for file: {}", filename);
            String result = tesseract.doOCR(image);
            logger.debug("OCR result: {}", result);
            List<String> numbers = extractNumbersFromText(result);
            logger.info("Numbers found: {}", numbers);
            if (numbers.isEmpty()) {
                return ResponseEntity.ok(""); // Return empty string if no numbers found
            }
            // Return numbers as a single string, space-separated
            String numbersString = String.join(" ", numbers);
            return ResponseEntity.ok(numbersString);
        } catch (TesseractException | IOException e) {
            logger.error("OCR error for file {}: {}", filename, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("เกิดข้อผิดพลาดในการอ่านภาพ: " + e.getMessage());
        }
    }

    private boolean isImageFile(String path) {
        String lower = path.toLowerCase();
        for (String ext : IMAGE_EXTENSIONS) {
            if (lower.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    }

    private List<String> extractNumbersFromText(String text) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(text);
        List<String> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(matcher.group());
        }
        return numbers;
    }

    @Value("${tessdata.path}")
    private String tessdataPath;
}
