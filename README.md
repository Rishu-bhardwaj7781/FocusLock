# FocusLock – Android app (Kotlin + Jetpack Compose)

## Run it
1. Install **Android Studio** and open this folder (File > Open). Let Gradle sync.
2. Open `local.properties` (Studio creates it) and add one line:
   `ANTHROPIC_API_KEY=your_key_here`
3. Plug in your phone (USB debugging on) or use an emulator, then press Run.
4. In the app, tap **Open Accessibility settings** > FocusLock Blocker > turn ON.
5. Pick a mode, set minutes, tap **Start locked session**.

## How the lock works
- A session end time is saved on the device. There is no cancel button.
- An Accessibility Service watches which app comes to the foreground. If it is blocked in the
  chosen mode, the user is sent to the Home screen and shown a black "Stay focused" screen.
- Settings is blocked during a session so the service cannot be switched off.
- Calls, keyboards, launcher and SystemUI are always allowed.

## Modes
Study, Reading (block lists), Exam Prep, Deep Work (block everything except calculator/notes/phone),
Custom (you tick the apps).

## Known limits
- Rebooting into safe mode or restarting the phone can still bypass any non-device-owner app.
- The API key is inside the APK. Fine for testing; for release call the AI from your own server.
- Login is stored on the phone only. For cloud accounts, add Firebase Auth.
- Google Play restricts Accessibility and QUERY_ALL_PACKAGES use; you must justify it in the Play Console.
