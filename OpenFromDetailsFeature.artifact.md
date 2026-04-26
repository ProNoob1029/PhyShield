# Implementation Plan: Open from Details

## 🎯 Objective
Allow users to launch the **Safe Preview** (in-app WebView) directly from the **Scan Detail** screen, specifically for sites they have reviewed and found to be acceptable.

## 📋 Functional Requirements
1.  **Direct Action**: Add an "Open in Safe Preview" button to the `ScanDetailScreen`.
2.  **Contextual Visibility**: The button should be most prominent for "Safe" sites but available for others if the user chooses to proceed after reading the reasons.
3.  **Seamless Navigation**: Closing the WebView should return the user to the Scan Detail screen (or back to the Home screen depending on the flow).

## 🛠️ Technical Roadmap

### Phase 1: UI Enhancement
- [x] **`ScanDetailScreen.kt`**:
    *   Update the function signature to accept an `onOpenUrl: (String) -> Unit` lambda.
    *   Add a **"Open in Safe Preview"** button at the bottom of the screen.
    *   Use the `PhishBlue` theme for the button to indicate a safe/standard action.

### Phase 2: Navigation Integration
- [x] **`PhishShieldApp.kt`**:
    *   Update the `ScanDetailScreen` call to provide the `onOpenUrl` logic.
    *   Ensure the state correctly transitions from `showScanDetail` to `showSafeWebView`.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **OPN-01** | **UI** | Adding the "Open" button to `ScanDetailScreen` | ✅ COMPLETED |
| **OPN-02** | **Flow** | Linking the button to the WebView in `PhishShieldApp` | ✅ COMPLETED |

---
*Created: April 25, 2026 - 4:20 AM*
