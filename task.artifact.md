# Implementation Plan: Settings Screen

This plan outlines the features and implementation steps for a fully functional **Settings** panel.

## 🎯 Objective
Provide users with control over the app's protection behavior, AI sensitivity, and data management.

## 🛠️ Proposed Features
1.  **AI Detection Sensitivity**: Choose between "Cautious" (few flags), "Balanced" (default), and "Aggressive" (high protection).
2.  **Automatic Protection**: Toggle to automatically block high-risk sites instead of asking every time.
3.  **Real-Time Dashboard**: Toggle to enable/disable the Home screen background refresh logic.
4.  **Data Management**:
    *   **Clear Scan History**: Delete all previous scan logs from the database.
    *   **Reset Blocklist**: Clear all manually blocked domains.
5.  **About Section**: Display version info and "Powered by Gemini AI".

## 🚀 Implementation Steps
1.  **[ ] Create `SettingsManager.kt`**: A new manager using **DataStore** to persist user preferences (Sensitivity, Auto-Block, etc.).
2.  **[ ] Design `SettingsScreen.kt` UI**:
    *   Use a modern Material 3 list layout.
    *   Implement "Switch" components for toggles.
    *   Implement a "Segmented Button" or "Slider" for Sensitivity.
    *   Add a "Danger Zone" section for clearing data with confirmation dialogs.
3.  **[ ] Integrate with Detection Logic**:
    *   Update `PhishDetector.kt` to adjust the Gemini prompt based on the user's "Sensitivity" setting.
4.  **[ ] Implement Data Wiping**:
    *   Add `clearAllScans()` to `ScanHistoryDao`.
    *   Add `clearBlocklist()` to `BlocklistDao`.
    *   Connect these to the buttons in `SettingsScreen`.

## 📂 Affected Files
- `app/src/main/java/com/example/polihackplm2/functionality/SettingsManager.kt` (New)
- `app/src/main/java/com/example/polihackplm2/ui/SettingsScreen.kt`
- `app/src/main/java/com/example/polihackplm2/db/Daos.kt`
- `app/src/main/java/com/example/polihackplm2/functionality/PhishDetector.kt`
- `app/src/main/java/com/example/polihackplm2/functionality/HomeRefreshManager.kt`
- `app/src/main/java/com/example/polihackplm2/functionality/BlocklistManager.kt`
