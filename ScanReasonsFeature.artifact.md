# Implementation Plan: Detailed Scan Reasons in History

## 🎯 Objective
Enable the persistent storage and display of detailed "Reasons" for every scanned URL (AI and urlscan.io insights) within the Activity History.

## 📋 Functional Requirements
1.  **Persistent Storage**: Ensure the `scan_history` database table correctly stores the list of reasons provided by the detection engines.
2.  **Detail Visibility**: Allow users to click on any item in the **Home Screen Activity** or **Activity Log** to view the full list of reasons why it was flagged or verified.
3.  **UI Enhancement**: Create a dedicated detail view (or update existing ones) to present these reasons clearly.

## 🛠️ Technical Roadmap

### Phase 1: Data Architecture
- [x] **`Entities.kt`**: Verify `ScanResultEntity` includes a `reasons` field (already planned as a comma-separated string).
- [x] **`HomeRefreshManager.kt`**: Update the `toThreat()` conversion to include the `reasons` list in the `Threat` data model.

### Phase 2: UI Integration
- [x] **`HomeScreen.kt`**: Update `ThreatItem` or the click action to pass the reasons to the main app container.
- [x] **`PhishShieldApp.kt`**:
    *   Enhance the navigation state to handle showing a "Scan Detail" screen.
    *   Implement the logic to display previous reasons when a history item is clicked.
- [x] **`ScanDetailScreen.kt` (New)**: Create a screen to show the URL, Title, Source, and the bulleted list of Reasons from the database.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **REA-01** | **DB** | Ensuring `reasons` are correctly mapped in DAOs/Entities | ✅ COMPLETED |
| **REA-02** | **Model** | Updating `Threat` model to carry reason data | ✅ COMPLETED |
| **REA-03** | **UI** | Building the `ScanDetailScreen` for history viewing | ✅ COMPLETED |
| **REA-04** | **Flow** | Connecting Log/Home clicks to the detail view | ✅ COMPLETED |

---
*Created: April 25, 2026 - 4:15 AM*
