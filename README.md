# SpendSense - Smart Android Finance & UPI Expense Tracker

SpendSense is a modern, high-performance Android personal finance and expense tracking application built using **Kotlin**, **Jetpack Compose (Material 3)**, **Room Database**, and **Apple-inspired Liquid Glass Design Language**.

---

## 🚀 Features

- **Apple Liquid Glass UI**: Translucent optical frosted cards, specular hairline gradient borders, and animated ambient mesh backgrounds.
- **Auto-Detected UPI Payments**: Real-time notification parsing and pending queue for incoming UPI SMS/notifications (Google Pay, PhonePe, Paytm, CRED).
- **Budget & Category Limits**: Visual circular progress rings and warnings when spending approaches budget caps.
- **Interactive Financial Visualizations**: Category donut breakdown charts, weekly/monthly spending trend bar charts.
- **Subscriptions & Recurring Bills**: Track monthly subscriptions with renewal reminders and active/paused states.
- **Split Bills**: Seamlessly split group dinners and shared expenses with custom member shares and settle toggles.
- **Financial Health & PDF Reports**: Generate structured monthly reports and export summaries.
- **PIN & Security Lock**: Built-in 4-digit PIN lock screen with biometric-ready local verification.
- **Gemini AI Financial Assistant**: Optional on-demand AI financial advisor sheet for smart expense insights.

---

## 🛠️ Prerequisites & Setup in Android Studio

1. **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer (Meerkat / Narwhal recommended).
2. **JDK**: JDK 17 or JDK 21 (configured in Android Studio under `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK`).
3. **Android SDK**: Android SDK Platform 34 or 35/36 installed via SDK Manager.

### 🏃 Opening and Running the Project

1. **Extract ZIP**: Extract `MyAndroidApp.zip` to your preferred folder.
2. **Open in Android Studio**:
   - Open Android Studio.
   - Click **File > Open...** (or **Open Project** on the welcome screen).
   - Select the extracted folder (`SpendSense` / `MyAndroidApp`).
3. **Gradle Sync**:
   - Android Studio will automatically detect the bundled **Gradle Wrapper (9.3.1)** and sync dependencies.
   - If prompted, click **Sync Project with Gradle Files**.
4. **Run on Device or Emulator**:
   - Connect an Android device (with USB Debugging enabled) or start an Android Virtual Device (AVD with API 24+).
   - Select `app` in the run configurations dropdown.
   - Click the green **Run (▶)** button or press `Shift + F10`.

---

## 🔑 Optional Configuration (.env)

If you would like to test the Gemini AI smart insights assistant:
1. Open the `.env` file in the project root.
2. Add your Gemini API key:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
3. Rebuild the project (`Build > Rebuild Project`).

---

## 🏗️ Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3, Custom Liquid Glass shaders & modifiers
- **Architecture**: MVVM with Kotlin Coroutines & StateFlow
- **Database**: Room (SQLite) with TypeConverters and Flow reactive queries
- **Image Loading**: Coil Compose
- **Networking & JSON**: Retrofit2 + Moshi
- **Testing**: Robolectric & JUnit4
