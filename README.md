# Avro Keyboard

Free Android system IME. Flutter settings + Kotlin keyboard.

**Step 4/6.** Production-style InputConnection handling on top of three modes:

- **বাংলা** — direct Bengali
- **English** — QWERTY
- **অভ্র** — Banglish (`ami` → আমি)

Mode is remembered locally. Typed text is never stored.

```bash
flutter pub get
flutter analyze
flutter test
flutter build apk --debug
```
