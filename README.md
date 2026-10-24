# FocusLock – Android app (Kotlin + Jetpack Compose)

A study-focus app that locks distracting apps for a set time. Once a session starts, it cannot be cancelled until the timer ends. Includes login, focus modes, a black theme and an AI study coach.

## Run it
1. Install **Android Studio** and open this folder (File > Open). Let Gradle sync.
2. Get a free Gemini API key from aistudio.google.com.
3. Open `local.properties` (Studio creates it) and add one line:
   `GEMINI_API_KEY=your_key_here`
   (see `local.properties.example`; never commit this file)
4. Plug in your phone (USB debugging on) or use an emulator, then press Run.
5. In the app, tap **Open Accessibility settings** > FocusLock Blocker > turn ON.
6. Pick a mode, set minutes, tap **Start locked session**.

## How the lock works
- A session end time is saved on the device. There is no cancel button.
- An Accessibility Service watches which app comes to the foreground. If it is blocked in the chosen mode, the user is sent to the Home screen and shown a black "Stay focused" screen.
- Settings is blocked during a session so the service cannot be switched off.
- Calls, keyboards, launcher and SystemUI are always allowed.

## Modes
Study, Reading (block lists), Exam Prep, Deep Work (block everything except calculator/notes/phone), Custom (you tick the apps).

## Known limits
- Restarting the phone or using safe mode can still bypass the lock.
- The API key is built into the APK. Fine for testing; for a public release, call the AI from your own server.
- Login is stored on the phone only. Cloud accounts could be added with Firebase Auth.
- Google Play restricts Accessibility and QUERY_ALL_PACKAGES use.
