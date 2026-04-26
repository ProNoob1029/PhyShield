# Implementation Plan: True Edge-to-Edge UI

## 🎯 Objective
Eliminate the white bar/line at the top of the screen (status bar area) by allowing the app UI to draw behind the clock and battery icons.

## 📋 Requirements
1.  **Immersive Headers**: The Blue/Primary color of the headers must extend to the very top edge of the physical screen.
2.  **Safe Content**: Text and icons must not be covered by the camera cutout or system icons.
3.  **System Transparency**: The status bar and navigation bar should be transparent or match the app's theme.

## 🛠️ Technical Roadmap

### Phase 1: Enable System Support
- [ ] **`MainActivity.kt`**: Add `enableEdgeToEdge()` call before `setContent`.

### Phase 2: Layout Refinement
- [ ] **`PhishShieldApp.kt`**:
    *   Remove `Modifier.safeDrawingPadding()` from the root `Scaffold`. This is currently creating the "white line" by pushing the entire app down.
    *   Ensure the `bottomBar` still handles `navigationBarsPadding` correctly.

### Phase 3: Header Optimization
- [ ] **`Components.kt`**:
    *   Verify `PhishShieldHeader` uses `statusBarsPadding()` on its *internal* content (Row), while the outer `Surface` fills the width to the top.

## 📊 Detailed Task Progress

| Task ID | Component | Description | Status |
| :--- | :--- | :--- | :--- |
| **EDG-01** | **System** | Enabling `enableEdgeToEdge` in Activity | ✅ COMPLETED |
| **EDG-02** | **Scaffold** | Removing global safe padding in PhishShieldApp | ✅ COMPLETED |
| **EDG-03** | **Header** | Fine-tuning status bar padding for headers | ✅ COMPLETED |

---
*Created: April 25, 2026 - 7:00 AM*
