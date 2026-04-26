# PhishShield - AI-Powered Phishing Protection

PhishShield is a modern Android application designed to protect users from phishing attacks by analyzing URLs from SMS, QR codes, and manual input. It leverages Google's Gemini AI for deep analysis and maintains an extensive local database for rapid, offline detection.

## 🚀 Key Features

- **Multi-Source Scanning**: Analyze links via QR code scanner, SMS text analysis, or direct URL input.
- **AI-Powered Analysis**: Integrates with Google Gemini (via Firebase Vertex AI) to detect sophisticated phishing patterns and typosquatting.
- **Robust Local Detection**: An extensive local database of over 50+ malicious keywords and patterns for instant offline protection.
- **Threat Dashboard**: Real-time monitoring of protection status and recent blocked threats.
- **Modular Architecture**: Clean, scalable codebase built with Jetpack Compose.

## 🏗️ Architecture & Project Structure

The project follows a modular design to ensure maintainability and ease of development.

### Core Modules (UI Package)

- **`PhishShieldApp` (`Screen.kt`)**: The main application container that manages navigation state and the high-level alert system.
- **`PhishDetector.kt`**: The brain of the app. Contains the `PhishDetector` logic which coordinates AI analysis and local heuristics.
- **`HomeScreen.kt`**: Displays the security dashboard, protection stats, and recent threat activity.
- **`ScannerScreen.kt`**: Implements the CameraX-based QR code scanner and manual text analysis tools.
- **`AlertScreen.kt`**: A critical UI component that intercepts navigation to malicious sites and provides detailed risk reports.
- **`LogScreen.kt` & `SettingsScreen.kt`**: Placeholder modules for historical activity logs and app configuration.
- **`Components.kt`**: Reusable Jetpack Compose components used throughout the app (Headers, Bottom Nav, Stat Cards).

## 🛡️ Detection Mechanism

### 1. AI Analysis (Vertex AI / Gemini)
The app sends suspected URLs to the `gemini-2.5-flash-lite` model with a specialized prompt to identify:
- Typosquatting (e.g., `paypa1.com`)
- Malicious intent in subdomains
- Known phishing landing page patterns

### 2. Local Heuristics
If AI analysis is unavailable or as a first line of defense, the app checks for:
- **Keywords**: Extensive lists covering Financial (Chase, PayPal), Streaming (Netflix, Amazon), Crypto (Binance, MetaMask), and Government services.
- **URL Length**: Detection of unusually long URLs often used to mask malicious domains.
- **Shorteners**: Flags the use of services like `bit.ly` or `tinyurl.com` which are common in phishing.
- **Suspicious TLDs**: Scans for TLDs frequently associated with malicious activity.

## 🛠️ Technical Stack

- **UI**: Jetpack Compose
- **Language**: Kotlin
- **AI**: Firebase Vertex AI (Gemini 2.0 Flash Lite)
- **Scanning**: ML Kit Barcode Scanning
- **Camera**: CameraX
- **Concurrency**: Kotlin Coroutines & Flow

---
*Developed for Polihack 2026*
