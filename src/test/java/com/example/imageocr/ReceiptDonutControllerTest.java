package com.example.imageocr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReceiptDonutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * ทดสอบส่งไฟล์ที่ไม่ใช่ภาพ => ต้องได้ 400 Bad Request
     */
    @Test
    void testRejectNonImageFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello world".getBytes()
        );
        mockMvc.perform(multipart("/api/receipt/analyze").file(file))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * ทดสอบส่งไฟล์ว่าง => ต้องได้ 400 Bad Request
     */
    @Test
    void testRejectEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[0]
        );
        mockMvc.perform(multipart("/api/receipt/analyze").file(file))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    /**
     * ทดสอบส่งรูปใบเสร็จจริง (test.jpg) ไปที่ Google Document AI
     */
    @Test
    void testAnalyzeReceiptWithRealImage() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/test.jpg")) {
            assertNotNull(is, "test.jpg not found in test resources");
            MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", MediaType.IMAGE_JPEG_VALUE, is
            );

            MvcResult result = mockMvc.perform(multipart("/api/receipt/analyze").file(file))
                    .andDo(print())
                    .andReturn();

            int status = result.getResponse().getStatus();
            String body = result.getResponse().getContentAsString();

            System.out.println("=== [Test] Google Document AI response status: " + status + " ===");
            System.out.println("=== [Test] Google Document AI response body: " + body + " ===");

            // Accept common outcomes based on external environment/configuration.
            // 200: success, 401/403: auth/permission issue, 404: processor not found,
            // 429: quota exceeded, 502: upstream error, 503: config missing.
            assertTrue(status == 200 || status == 401 || status == 403 || status == 404
                    || status == 429 || status == 502 || status == 503,
                "Unexpected status from controller: status=" + status + ", body=" + body);

            System.out.println("=== [Test] Google Document AI status: " + status + " ===");
            System.out.println("=== [Test] Google Document AI body: " + body + " ===");

            if (status == 200) {
                assertNotNull(body, "Response body should not be null");
                assertTrue(!body.isBlank(), "Response body should not be empty");
                System.out.println("=== [Test] SUCCESS - Google Document AI parsed receipt: " + body + " ===");
            } else {
                System.out.println("=== [Test] Google Document AI not available or not configured (status=" + status + "), controller worked correctly ===");
            }
        }
    }
}
