# Implementation Plan: 70% Confidence Automatic Protection

## 🎯 Objective
Upgrade the **Automatic Protection** system to automatically intercept and block websites if the detection engines are **70% or more certain** that a link is malicious.

## 📋 Logic Requirements
1.  **Confidence Scoring**: Update detection engines to return a numerical confidence percentage (0-100).
2.  **Threshold Enforcement**: If "Automatic Protection" is enabled in Settings, any scan result with **Confidence >= 70%** will bypass the alert screen and be added to the blocklist immediately.
3.  **UI Feedback**: Show a notification (Toast) when a site is automatically neutralized.

## 🛠️ Technical Roadmap

### Phase 1: Detection Engine Upgrades
- [ ] **`PhishDetector.kt`**:
    *   Update `PhishResult` to include a `confidenceScore: Int`.
    *   Modify urlscan.io logic: Map the community "score" (0-100) directly to confidence.
    *   Modify Gemini AI prompt: Explicitly ask the AI to provide a "confidenceScore" between 0-100 in the JSON response.

### Phase 2: Flow Integration
- [ ] **`PhishShieldApp.kt`**:
    *   Collect the `autoBlock` preference from `SettingsManager`.
    *   In the scan result handler, if `result.isMalicious` AND `autoBlock == true` AND `result.confidenceScore >= 70`:
        *   Call `BlocklistManager.blockUrl()`.
        *   Show "Threat neutralized" Toast.
        *   Skip showing the Alert Screen.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **AUT-01** | **Model** | Adding `confidenceScore` to `PhishResult` | ✅ COMPLETED |
| **AUT-02** | **AI Tuning** | Updating Gemini prompt for scoring | ✅ COMPLETED |
| **AUT-03** | **Logic** | Implementing 70% threshold check in Main App | ✅ COMPLETED |
| **AUT-04** | **UX** | Adding automated block notifications | ✅ COMPLETED |

---
*Created: April 25, 2026 - 4:10 AM*
