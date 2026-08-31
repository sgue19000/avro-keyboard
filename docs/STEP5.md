# Step 5 — Voice typing

## API

Android `SpeechRecognizer` + `RecognitionListener` + `RecognizerIntent`.
Chosen because it is free, ships with the device, needs no API key, and supports partial results.
Audio is handled by the user's installed recognition service (often Google). This app does not record or upload audio itself.

## Language

- বাংলা and অভ্র modes request `bn-BD`.
- English requests `en-US`.
- Voice output is natural text. Avro is not applied to speech.
- Mixed Bengali/English is whatever the device recognizer returns. There is no custom language detector.

## Compose

Partial results call `setComposingText` with the latest hypothesis only.
The final result calls `commitText` once. Partials are never concatenated.

## Permission

`RECORD_AUDIO` via `VoicePermissionActivity` launched from the IME.
Denied permission returns the mic to idle. No request loop.

## Offline

`EXTRA_PREFER_OFFLINE` is not set. Offline packs may exist on some devices; this is not verified and not claimed.

## Privacy

No audio files, no speech logs, no analytics. Typed text is not stored.
