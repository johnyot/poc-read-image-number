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

@RestController
@RequestMapping("/api/ocr")
public class ImageOcrController {
    private static final String[] IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "bmp", "gif", "tiff"};

    @PostMapping(value = "/numbers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> extractNumbers(@RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !isImageFile(filename)) {
            return ResponseEntity.badRequest().body("รับเฉพาะไฟล์ภาพเท่านั้น");
        }
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tessdataPath);
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                return ResponseEntity.badRequest().body("ไฟล์ที่อัปโหลดไม่ใช่ภาพที่รองรับ");
            }
            String result = tesseract.doOCR(image);
            List<String> numbers = extractNumbersFromText(result);
            if (numbers.isEmpty()) {
                return ResponseEntity.ok("ไม่พบตัวเลขในภาพ");
            }
            return ResponseEntity.ok(numbers);
        } catch (TesseractException | IOException e) {
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
