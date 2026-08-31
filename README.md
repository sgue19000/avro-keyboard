# Avro Keyboard

Free Android system IME. Flutter settings + Kotlin keyboard.

**Step 3/6.** Three modes:

- **বাংলা** — direct Bengali layout (consonants, vowels, kars, hasanta, digits)
- **English** — QWERTY
- **অভ্র** — Banglish phonetic engine (`ami` → আমি)

Prefix a word with `` ` `` in Avro mode to keep Latin as-is.

## Build

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```
