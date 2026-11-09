J.A.R.V.I.S. Android 2.0

This is a separate native Android app project. It does not replace the existing GitHub Pages PWA or change its files.

Current capabilities
- Native Android activity and interface
- Voice input through the Android speech-recognition service (microphone permission required)
- Android text-to-speech using the installed system voice, preferring UK English
- Open supported installed apps: WhatsApp, YouTube, Chrome, Maps, Gmail and Spotify
- Open the camera and Android Settings
- Time/date, a foreground in-app countdown timer, and web searches
- Typed commands if speech recognition is unavailable

Honest limits
- This is not an always-listening background assistant.
- Speech recognition availability and offline support depend on the speech service installed on the phone.
- Voice quality depends on the installed Android TTS voice.
- It cannot read private data from other apps, tap arbitrary app screens, or change protected settings without Android-supported user permissions.
- General knowledge questions currently open a web search; a private/free AI brain is not bundled.
- The timer runs while the app process remains alive; it is not an exact alarm when Android kills the app.

Build
Open this folder in Android Studio, or run gradle assembleDebug with JDK 17 and Android SDK platform 35 installed.
The GitHub Actions workflow builds a debug APK and uploads it as an artifact.
