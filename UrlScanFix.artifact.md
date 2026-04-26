# Implementation Plan: Repair urlscan.io Integration

## 🎯 Objective
Identify and resolve the issue preventing the app from sending scan data to **urlscan.io**.

## 📋 Identified Potential Issues
1.  **Merge Conflict Residue**: Recent GitHub sync may have removed or corrupted the `UrlScanService` initialization.
2.  **API Key Visibility**: `BuildConfig.URLSCAN_API_KEY` might be empty if the `local.properties` file or `build.gradle.kts` configuration was changed.
3.  **Tiered Logic Break**: The fallback logic between urlscan.io and Gemini might be skipping the external scan.

## 🛠️ technical Roadmap

### Phase 1: Service Verification
- [ ] **`PhishDetector.kt`**: Check if `UrlScanServiceImpl` is being called correctly.
- [ ] **`UrlScanService.kt`**: Verify the submission endpoint and header configuration.

### Phase 2: Configuration Check
- [ ] **`build.gradle.kts`**: Ensure `buildConfigField` is still correctly reading the API key.
- [ ] **`local.properties`**: (User check) Verify the key exists locally.

### Phase 3: Logic Restoration
- [ ] Re-align the `checkUrlFlow` to ensure `urlScanService.submitUrl` is triggered for every new scan.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **USC-01** | **Analysis** | Reviewing service and logic for breaks | 🔄 IN PROGRESS |
| **USC-02** | **Logic** | Restoring urlscan.io submission trigger | ⏳ PENDING |
| **USC-03** | **Config** | Verifying API key injection via BuildConfig | ⏳ PENDING |

---
*Created: April 25, 2026 - 6:45 AM*
