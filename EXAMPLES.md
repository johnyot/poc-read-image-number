# ตัวอย่างการเรียกใช้งาน API

## ตัวอย่าง curl

```sh
curl -X POST http://localhost:8080/api/ocr/numbers \
  -F "file=@/path/to/your/image.png"
```

## ตัวอย่างวิเคราะห์ใบเสร็จ (Google Document AI)

### ตั้งค่า environment variables (PowerShell)

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:/path/to/service-account.json"
$env:GOOGLE_PROJECT_ID="your-google-project-id"
$env:GOOGLE_DOCUMENT_AI_LOCATION="us"
$env:GOOGLE_DOCUMENT_AI_PROCESSOR_ID="your-processor-id"
```

### เรียก API

```sh
curl -X POST http://localhost:8080/api/receipt/analyze \
  -F "file=@/path/to/receipt.jpg"
```

### ตัวอย่างผลลัพธ์

```json
{
  "entities": [
    { "type": "supplier_name", "value": "7-Eleven", "confidence": 0.98 },
    { "type": "total_amount", "value": "150.00", "confidence": 0.97 }
  ],
  "text": "...",
  "entityCount": 2
}
```

## ตัวอย่าง Postman
1. Method: POST
2. URL: `http://localhost:8080/api/ocr/numbers`
3. Tab: Body > form-data
4. เพิ่ม key ชื่อ `file` (type: File) แล้วเลือกไฟล์ภาพ
5. กด Send

### ผลลัพธ์
- ถ้าเจอตัวเลข: จะได้ string ตัวเลขทั้งหมดคั่นด้วยช่องว่าง เช่น

```
0107542000011 05634 0200176 7 2 1 014 00 28 00 1 18 00 7 1 0 18 00 1 1001 18 00 1 008 18 00 1 003 18 00 1 18 00 71 18 00 1 3 20 00 1 3 20 00 031 80 00 0 00 224 00 02 1 00 1 0 10 00 42 210 00 210 00 5 10030208 067888 0000872866 1 0453397 11 11 56 18 03 1 17 11 0 2711 7117
```
- ถ้าไม่เจอ: จะได้ string ว่าง (empty string)
- ถ้าไม่ใช่ไฟล์ภาพ: จะได้ข้อความ `รับเฉพาะไฟล์ภาพเท่านั้น`
