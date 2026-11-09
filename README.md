# J.A.R.V.I.S.

A lightweight browser-based personal assistant with voice input/output, local timers and notes, and optional online/offline AI.

## Run it

Serve this folder from a secure HTTPS host (or `localhost`) and open `index.html` in a modern browser. Microphone access and speech recognition require browser support and permission. For Android, use current Chrome and install the site as an app if desired.

## Voice

JARVIS prefers English male voices and prioritises British English voices when the browser exposes a recognisable voice name. Voice gender is not provided consistently by browser APIs, so the app cannot guarantee that every device's voice will be male. Install an English male text-to-speech voice in Android or Windows settings, then reload the app and choose it under Settings → JARVIS voice. The app uses a slower rate and lower pitch for a calmer, deeper delivery.

## AI brains

- **Online:** requires an Anthropic API key. Anthropic API usage is billed separately; this repository does not include free credits. The key is stored in browser local storage and sent directly to Anthropic from the browser. Do not use this direct-browser setup for a production multi-user deployment.
- **Offline:** requires WebGPU, browser memory and a one-time model download. Low-memory devices may fail to load the model. Timers and notes work locally without either AI brain.

## Troubleshooting

- If smart questions do not work, open Settings and check Network, Claude key and Offline brain status.
- If microphone input does not start, grant microphone permission and use HTTPS in a supported browser. Speech recognition may require internet even when the offline AI model is installed.
- If voice output is unavailable, install an English male TTS voice in the device's speech settings and reload.
