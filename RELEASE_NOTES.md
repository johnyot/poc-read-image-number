# Release Notes

## 2026-03-15 (390b059)

### Summary
- Migrated receipt analysis endpoint from HuggingFace Donut to Google Document AI.
- Switched sensitive configuration to environment-variable based setup.

### What Changed
- Added Google Document AI Java SDK dependency (`google-cloud-document-ai`) with Google Cloud BOM.
- Updated receipt endpoint implementation to call Document AI processor and return structured entity output.
- Replaced Donut properties with Google Document AI properties in application config.
- Added local environment template `.env.example` and ignored `.env` to prevent secret leaks.
- Updated tests and project documentation to reflect the new Google API flow.

### Notes
- Required env vars: `GOOGLE_APPLICATION_CREDENTIALS`, `GOOGLE_PROJECT_ID`, `GOOGLE_DOCUMENT_AI_LOCATION`, `GOOGLE_DOCUMENT_AI_PROCESSOR_ID`.
- Existing endpoint path remains: `POST /api/receipt/analyze`.
