# Project Knowledge: poc-read-image-number

## Overview
- Java Spring Boot project for OCR (Tesseract) to extract numbers from image files via REST API.
- Accepts image upload (jpg, png, etc.), validates extension, uses Tess4J for OCR, and returns all numbers found in the image as a plain string (space-separated).
- Configurable tessdata path via `application.properties` (default: `./config`).
- Unit/integration tests provided for controller and OCR logic.

## Key Files
- `src/main/java/com/example/imageocr/ImageOcrController.java`: Main REST controller for image upload and OCR.
- `src/main/resources/application.properties`: Contains `tessdata.path` config.
- `config/eng.traineddata`: Tesseract language data (ignored by git).
- `src/test/resources/test.jpg`: Test image for OCR unit test.
- `src/test/java/com/example/imageocr/ImageOcrControllerTest.java`: JUnit test for controller, expects number `0107542000011` in test.jpg.
- `.gitignore`: Ignores build, IDE, and config/eng.traineddata files.
- `EXAMPLES.md`: Example curl/Postman usage and expected results.

## Usage
- Run: `mvn spring-boot:run` or build jar and run with `java -jar ...`
- POST `/api/ocr/numbers` with multipart/form-data, field `file` (image)
- Returns: plain string of numbers separated by spaces (or empty string if not found), or error message if not an image

## Test
- `mvn test` runs all tests, including OCR on test.jpg
- Tessdata path must be set and eng.traineddata present in config/

## Next Steps for Copilot
- Read this file for project context before making changes
- Update this file with new endpoints, config, or conventions as the project evolves
