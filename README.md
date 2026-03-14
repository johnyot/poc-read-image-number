# Image OCR Spring Boot CLI

## คำอธิบาย
โปรแกรมนี้เป็น Spring Boot CLI สำหรับรับ path ของไฟล์ภาพ ตรวจสอบนามสกุลไฟล์ว่าเป็นภาพเท่านั้น จากนั้นอ่านภาพและดึงตัวเลขทั้งหมดออกมาด้วย OCR (Tesseract) และแสดงผลตัวเลขที่พบในภาพ

## วิธีใช้งาน

1. ติดตั้ง Java 17+ และ Maven
2. ติดตั้ง Tesseract OCR ในเครื่อง (ต้องมี tesseract ใน PATH หรือกำหนด path ในโค้ด)
3. สร้างไฟล์ jar:

    mvn clean package

4. รันโปรแกรมโดยระบุ path ของไฟล์ภาพ:

    java -jar target/imageocr-0.0.1-SNAPSHOT.jar <path/to/imagefile>

## หมายเหตุ
- รองรับเฉพาะไฟล์ภาพนามสกุล jpg, jpeg, png, bmp, gif, tiff
- ต้องติดตั้ง Tesseract OCR ก่อนใช้งานจริง
