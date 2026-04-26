# PhishShield Settings Implementation

## 🎯 Objective
The goal of the Settings feature is to empower users with full control over their security posture. This implementation will provide granular tuning for AI detection, automation for threat response, and robust tools for managing personal browsing data and privacy.

### User Autonomy Goals:
- **Customized Protection**: Allow users to balance between strict security (Aggressive) and minimal interference (Cautious).
- **Automation**: Reduce friction by enabling automatic blocking of verified high-risk threats.
- **Privacy Control**: Provide "one-click" solutions for wiping local scan history and resetting manual blocklists.

## 📋 Functional Requirements
- [ ] **AI Sensitivity Levels**: 3-state toggle (Cautious, Balanced, Aggressive).
- [ ] **Auto-Block Feature**: Toggle to skip the "Alert Screen" for 100% verified phishing links.
- [ ] **Live Dashboard Toggle**: Option to disable background refresh to save battery/data.
- [ ] **Database Management**: Dedicated buttons to clear `scan_history` and `blocked_domains` tables.
- [ ] **Confirmation Dialogs**: Safety prompts for all destructive "Clear Data" actions.

## 🛠️ Technical Roadmap

### Phase 1: Persistence Layer (Jetpack DataStore)
- [ ] Define `SettingsManager` class.
- [ ] Configure `PreferencesDataStore` for asynchronous preference handling.
- [ ] Implement keys for: `SENSITIVITY_LEVEL`, `AUTO_BLOCK_ENABLED`, `DASHBOARD_LIVE_REFRESH`.

### Phase 2: Core Logic Integration
- [ ] **PhishDetector Integration**: Update the Gemini prompt generation to respect the `SENSITIVITY_LEVEL`.
- [ ] **Blocklist/History Integration**: Add `deleteAll()` methods to `ScanHistoryDao` and `BlocklistDao`.

### Phase 3: Modern UI Development (Jetpack Compose)
- [ ] Implement `SettingsScreen` container with vertical scrolling.
- [ ] Design custom `SettingRow` component for standard toggles.
- [ ] Build a "Danger Zone" card for data management actions.
- [ ] Implement Material 3 `AlertDialog` for clearing data confirmation.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **SET-01** | **DataStore** | Initializing Preferences DataStore and keys | ✅ COMPLETED |
| **SET-02** | **Manager** | Building `SettingsManager` Flow-based API | ✅ COMPLETED |
| **SET-03** | **UI Layout** | Creating the Material 3 list-style settings UI | ✅ COMPLETED |
| **SET-04** | **AI Tuning** | Connecting sensitivity slider to Gemini Prompts | ✅ COMPLETED |
| **SET-05** | **Data Wiping** | Implementing database clear functions + Dialogs | ✅ COMPLETED |

---
*Last Updated: April 25, 2026 - 3:45 AM*
