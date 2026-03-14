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

@SpringBootTest
@AutoConfigureMockMvc
public class ImageOcrControllerNumberTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testInvalidImageFile() throws Exception {
        // อัปโหลดไฟล์ที่ไม่ใช่ภาพจริง จะต้องได้ 400
        MockMultipartFile file = new MockMultipartFile("file", "test.png", MediaType.IMAGE_PNG_VALUE, new byte[]{1,2,3});
        mockMvc.perform(multipart("/api/ocr/numbers").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ไฟล์ที่อัปโหลดไม่ใช่ภาพที่รองรับ"));
    }
}
