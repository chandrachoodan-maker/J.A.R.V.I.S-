# J.A.R.V.I.S. Next — zero-budget rebuild

This is a clean, separate rebuild in `jarvis-next/`. It does not overwrite the original app.

## What works without an API key
- Speech output through voices installed on Android/Windows.
- Voice input where the browser supports the Web Speech Recognition API (recognition may use an online service).
- Time and date, notes stored locally, timers, basic conversational responses, opening common websites, and launching web searches.
- Offline app shell after the first successful load and service-worker installation.

## Voice
The app prefers English (UK) voices and recognisable male voice names. Browser speech APIs do not expose reliable gender metadata, so it cannot guarantee a male voice on every device. For the intended deep British delivery, install an English (United Kingdom) male TTS voice in the operating system and select it in Voice Configuration. Rate and pitch are adjustable.

## Important limits — no misleading promises
- This is a browser/PWA assistant, not a native Android or Windows system agent.
- A web page cannot freely control device settings, shut down the computer, operate arbitrary apps, or access private device data.
- Built-in replies work offline. Open-ended AI answers require a separate AI model/service; this zero-key build does not pretend otherwise.
- On low-memory Windows machines, a large local language model is not bundled.
- Microphone recognition availability varies by browser and may require internet access and microphone permission.

## Run / install
Host this folder on HTTPS (GitHub Pages can host it if enabled for the repository), then open its `index.html` URL in Chrome or Edge. On Android Chrome, use **Install app** / **Add to Home screen**. On Windows, use the browser's **Install app** option if available.

If the repository is hosted at its root with GitHub Pages, the URL path is `/J.A.R.V.I.S-/jarvis-next/`; the repository owner must enable Pages in repository settings if it is not already enabled.
