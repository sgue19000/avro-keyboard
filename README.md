# Avro Keyboard

Free Android system keyboard (`com.avrokeyboard.app`).

Modes: **বাংলা** | **English** | **অভ্র** (Banglish) plus a **mic** key for device speech-to-text.

## Enable

1. Install the APK.
2. Settings → System → Languages & input → On-screen keyboard → Avro Keyboard.
3. Choose it in a text field.
4. Cycle the mode key. Tap the mic for speech (needs `RECORD_AUDIO` and a working recognizer).

অভ্র: type `ami` for আমি. Prefix `` ` `` to keep Latin. Speech is **not** run through Avro.

## Build

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --release
flutter build appbundle --release
```

CI on `main` uploads debug APK, release APK, and AAB. Release artifacts are signed with the debug key unless you add your own keystore.

## Privacy

This app does not store typed text or recordings. Android speech services may send audio to the vendor you have installed. Voice is not guaranteed offline.

## Limits

Avro coverage is the bundled rule table, not desktop Avro. Speech quality depends on the device provider and language pack.
