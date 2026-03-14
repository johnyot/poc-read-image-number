package com.example.imageocr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageOcrControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testNonImageFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "dummy text".getBytes());
        mockMvc.perform(multipart("/api/ocr/numbers").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("รับเฉพาะไฟล์ภาพเท่านั้น"));
    }

    @Test
    void testImageWithNumbers() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/test.jpg")) {
            assertNotNull(is, "test.jpg not found in resources");
            MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, is
            );
            mockMvc.perform(multipart("/api/ocr/numbers").file(file))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("0107542000011")));
        }
    }
}
