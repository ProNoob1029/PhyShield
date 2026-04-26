# Implementation Plan: Fix Reason Fragmentation & Overflow

## 🎯 Objective
1.  **Stop Fragmented Sentences**: Prevent reasons from being split into multiple boxes whenever they contain a comma.
2.  **Fix UI Overflow**: Ensure that long reason texts wrap correctly inside their boxes and don't push the bullet point out of place.
3.  **Strict Individual Boxing**: Guarantee that every single analysis point is rendered in its own dedicated UI box (Card).

## 📋 Identified Root Causes
- **Data Splitting**: Currently, reasons are joined by a simple comma (`,`) in the database. When a sentence contains a comma, it is incorrectly parsed as two separate reasons.
- **Layout Constraint**: The `Text` component inside the reason `Row` lacks a `weight(1f)`, which can cause it to overflow the screen instead of wrapping correctly.

## 🛠️ Technical Roadmap

### Phase 1: Robust Data Storage
- [ ] **`PhishDetector.kt` & `HomeRefreshManager.kt`**:
    *   Change the delimiter used to store reasons from a pipe (`|`) to a unique sequence like `;;;`. This is safer than a pipe which might appear in technical URL analysis.
- [ ] **`BlocklistManager.kt`**:
    *   Update `blockUrl` to use the same `;;;` delimiter.

### Phase 2: UI Layout & Robust Parsing
- [ ] **`AlertScreen.kt`, `ScanDetailScreen.kt`, `BlockedDetailsScreen.kt`**:
    *   Update the split logic to use `;;;`.
    *   **Migration Logic**: Add a check so that if a reason string contains `,The` or `,A ` (common start of AI sentences), it automatically re-splits into individual boxes to fix old corrupted database entries.
    *   Ensure the `ReasonItem` card is strictly called for each item in the resulting list.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **RSN-01** | **Backend** | Migrating to ultra-robust `;;;` delimiter | ✅ COMPLETED |
| **RSN-02** | **Migration** | Adding "Comma-Fix" logic for old data | ✅ COMPLETED |
| **RSN-03** | **UI Layout** | Guaranteeing 1:1 Box-to-Reason ratio | ✅ COMPLETED |

---
*Created: April 25, 2026 - 5:45 AM*
