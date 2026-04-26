# Implementation Plan: Fullscreen Image View

## 🎯 Objective
Enable users to click on site screenshots in the **Alert Screen** or **Scan Details** and view them in a dedicated fullscreen view for better visibility.

## 📋 Functional Requirements
1.  **Clickable Previews**: Make the existing screenshot cards clickable.
2.  **Dedicated Fullscreen View**: Create a new screen that displays the image as large as possible.
3.  **Zoom Support**: Leverage existing `ZoomableImage` logic for pinch-to-zoom in the large view.
4.  **Natural Navigation**: Allow users to easily return to the previous screen using a back button or system gesture.

## 🛠️ Technical Roadmap

### Phase 1: Navigation State
- [ ] **`PhishShieldApp.kt`**:
    *   Add navigation state for a "Fullscreen Image" view.
    *   Implement the route and screen transition.

### Phase 2: UI Implementation
- [ ] **`AlertScreen.kt`**: Make the "Site Preview" box clickable to trigger navigation.
- [ ] **`ScanDetailScreen.kt`**: Add the screenshot preview (if available) and make it clickable.
- [ ] **`FullscreenImageScreen.kt` (New)**: Build a simple, immersive screen for the large image.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **IMG-01** | **State** | Adding fullscreen navigation to PhishShieldApp | ✅ COMPLETED |
| **IMG-02** | **Trigger** | Making previews clickable in Alert/Detail screens | ✅ COMPLETED |
| **IMG-03** | **UI** | Creating the immersive `FullscreenImageScreen` | ✅ COMPLETED |

---
*Created: April 25, 2026 - 6:15 AM*
