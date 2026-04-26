# Implementation Plan: Individual Activity Deletion

## 🎯 Objective
Enable users to delete individual scan records from the **Home Screen Activity History**. This will allow for a cleaner dashboard and the ability to manually remove specific "Warnings" or "Safe" logs, which will dynamically update the dashboard statistics.

## 📋 Functional Requirements
1.  **Deletion Trigger**: Add a "Delete Record" button to the **Scan Detail** screen (where the reasons are shown).
2.  **Confirmation**: Use a Material 3 `AlertDialog` to prevent accidental deletions.
3.  **Database Sync**: Permanently remove the specific record from the `scan_history` table.
4.  **Dynamic UI**: The Home screen "Warnings" and "Safe Score" must update immediately after a deletion.

## 🛠️ Technical Roadmap

### Phase 1: Database Logic
- [ ] **`ScanHistoryDao.kt`**: Add `@Delete` method for `ScanResultEntity` or a `@Query` to delete by ID.
- [ ] **`HomeRefreshManager.kt`**: Add `deleteScanResult(id: Int)` function to trigger the DAO and refresh the UI flow.

### Phase 2: UI Implementation
- [ ] **`ScanDetailScreen.kt`**:
    *   Add a trash/delete icon to the Top Bar or a prominent button at the bottom.
    *   Implement the `AlertDialog` for confirmation.
- [ ] **`PhishShieldApp.kt`**:
    *   Pass the `id` of the scan result through the navigation to ensure the correct item is deleted.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **DEL-01** | **DB** | Adding delete method to ScanHistoryDao | ✅ COMPLETED |
| **DEL-02** | **Logic** | Implementing background deletion in Manager | ✅ COMPLETED |
| **DEL-03** | **UI** | Adding Delete button & Dialog to Detail view | ✅ COMPLETED |
| **DEL-04** | **Sync** | Verifying dynamic counter updates | ✅ COMPLETED |

---
*Created: April 26, 2026 - 2:45 AM*
