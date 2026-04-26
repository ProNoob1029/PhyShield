# Implementation Plan: Dynamic Blocked Counter

## 🎯 Objective
Ensure the **"Blocked Today"** counter on the Home screen accurately reflects the total number of entries in the **User Blocklist** (Log Screen) at all times.

## 📋 Requirements
- [ ] **Data Linking**: Instead of counting recent scan results, the counter must query the `blocked_domains` table.
- [ ] **Real-Time Sync**: When a site is unblocked from the Log, the Home screen counter must update immediately.

## 🛠️ Technical Roadmap

### Phase 1: Logic Update
- [ ] **`HomeRefreshManager.kt`**:
    *   Update the `homeDataFlow` to query the `BlocklistDao` for the total count of blocked domains.
    *   Merge the scan history flow with the blocklist count flow to ensure the UI updates whenever either changes.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **CNT-01** | **Logic** | Linking counter to Blocklist Database | ✅ COMPLETED |
| **CNT-02** | **Sync** | Testing real-time unblock/decrement | ✅ COMPLETED |

---
*Created: April 25, 2026 - 7:15 AM*
