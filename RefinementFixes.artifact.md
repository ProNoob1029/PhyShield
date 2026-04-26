# Implementation Plan: Logic Refinements & History Details

## 🎯 Objective
Address UI inconsistencies and refine core logic for safer browsing and more accurate logging.

## 📋 Requirements
1.  **Safety Guard**: Only show the "Open in Safe Preview" button on the Scan Details screen if the site was classified as **Safe**.
2.  **Detailed History**: Ensure that clicking on a malicious site in the **Activity Log** (Blocked Domains) shows the reasons why it was flagged, just like the Home screen history.
3.  **Accurate Blocking Labels**: Distinguish between "Manually Blocked" and "Automatically Blocked" (70%+ confidence) in the database and UI.

## 🛠️ Technical Roadmap

### Phase 1: Data Model & Storage
- [ ] **`Entities.kt`**: Add a `blockType` field to `BlockedDomain` (MANUAL vs AUTOMATIC).
- [ ] **`BlocklistManager.kt`**:
    *   Update `blockUrl` to accept the block type.
    *   Expose the `Threat` model for blocked items so they can carry reasons.

### Phase 2: Logic Refinements
- [ ] **`PhishShieldApp.kt`**:
    *   Update `handleScanResult` to pass the correct block type (AUTOMATIC) when the 70% threshold is hit.
- [ ] **`ScanDetailScreen.kt`**:
    *   Wrap the "Open in Safe Preview" button in a conditional check: `if (threat.title == "Safe Website")`.

### Phase 3: Log Screen Upgrades
- [ ] **`LogScreen.kt`**:
    *   Update the UI to display the `blockType` (e.g., "Automatically Neutralized" vs "Manually Reported").
    *   Ensure clicking a blocked item opens the `ScanDetailScreen` with full reasons.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **REF-01** | **Logic** | Restricting "Open" button to Safe sites only | ✅ COMPLETED |
| **REF-02** | **DB** | Adding `blockType` to distinguish Auto vs Manual | ✅ COMPLETED |
| **REF-03** | **Log UI** | Enhancing Log screen with details and accurate labels | ✅ COMPLETED |

---
*Created: April 25, 2026 - 4:25 AM*
