# Avro Keyboard

Free Android system keyboard. Flutter settings UI + Kotlin IME.

**Step 2/6.** English QWERTY with shift, symbols, and an EN/Bangla layout
switch. Bangla is a placeholder. Avro phonetic conversion is not implemented yet.

## Enable

1. Install the debug APK.
2. Open **Avro Keyboard** → **Enable keyboard in system settings**.
3. Turn on **Avro Keyboard**, then select it.

## Build

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```
