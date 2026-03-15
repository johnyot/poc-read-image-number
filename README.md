# Image OCR Spring Boot

## คำอธิบาย
โปรเจกต์นี้เป็น Spring Boot REST API และ CLI สำหรับรับไฟล์ภาพ ตรวจสอบนามสกุลไฟล์ว่าเป็นภาพเท่านั้น จากนั้นอ่านภาพและดึงตัวเลขทั้งหมดออกมาด้วย OCR (Tesseract) และแสดงผลตัวเลขที่พบในภาพ

## วิธีใช้งาน REST API

1. ติดตั้ง Java 17+ และ Maven
2. ติดตั้ง Tesseract OCR ในเครื่อง (ต้องมี tesseract ใน PATH หรือกำหนด path ใน application.properties)
3. สร้างไฟล์ jar:

    mvn clean package

4. รัน Spring Boot:

    mvn spring-boot:run
    # หรือ
    java -jar target/imageocr-0.0.1-SNAPSHOT.jar

5. เรียก API:

    POST /api/ocr/numbers (multipart/form-data, field: file)

    ตัวอย่าง curl:
    curl -X POST http://localhost:8080/api/ocr/numbers -F "file=@/path/to/image.jpg"

    ผลลัพธ์: string ตัวเลขทั้งหมดคั่นด้วยช่องว่าง เช่น
    0107542000011 05634 0200176 ...

    ถ้าไม่พบตัวเลข: จะได้ string ว่าง
    ถ้าไม่ใช่ไฟล์ภาพ: จะได้ข้อความ "รับเฉพาะไฟล์ภาพเท่านั้น"

6. เรียก API วิเคราะห์ใบเสร็จด้วย Google Document AI:

    POST /api/receipt/analyze (multipart/form-data, field: file)

    ต้องตั้งค่า environment variables ก่อนรัน:

    - GOOGLE_APPLICATION_CREDENTIALS=C:/path/to/service-account.json
    - GOOGLE_PROJECT_ID=your-google-project-id
    - GOOGLE_DOCUMENT_AI_LOCATION=us
    - GOOGLE_DOCUMENT_AI_PROCESSOR_ID=your-processor-id

    ตัวอย่าง curl:
    curl -X POST http://localhost:8080/api/receipt/analyze -F "file=@/path/to/receipt.jpg"

    ผลลัพธ์: JSON ที่มี entities เช่น supplier_name, receipt_date, total_amount, line_item

## วิธีใช้งาน CLI

1. รันโปรแกรมโดยระบุ path ของไฟล์ภาพ:

    java -jar target/imageocr-0.0.1-SNAPSHOT.jar <path/to/imagefile>

## หมายเหตุ
- รองรับเฉพาะไฟล์ภาพนามสกุล jpg, jpeg, png, bmp, gif, tiff
- ต้องติดตั้ง Tesseract OCR ก่อนใช้งานจริง
- สำหรับ API ใบเสร็จ Google Document AI รองรับ jpg, jpeg, png, bmp, tiff, pdf
