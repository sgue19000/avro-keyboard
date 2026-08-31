# Step 3 — Bengali layout and Avro phonetic engine

## Modes

Cycle the mode key: **বাংলা → English → অভ্র → বাংলা**.

| Mode | Keys | Result of `ami` |
|---|---|---|
| বাংলা | Bengali letters + kars | type আ then ম then ি |
| English | QWERTY | `ami` |
| অভ্র | QWERTY through AvroEngine | `আমি` |

## Avro engine

Original rule-based port (longest match + prefix/suffix context).
Inspired by the public Avro Phonetic algorithm (Mehdi Hasan Khan).
Not a copy of GPL pyAvroPhonetic data. MIT-compatible original tables in this repo.

- Dart: `lib/avro/avro_engine.dart` (unit tested)
- Kotlin: `ime/avro/AvroEngine.kt` (IME path)

Keep the two tables in sync.

Prefix `` ` `` leaves the rest of the word as raw Latin (`\`hello` → hello).

## Composing

In অভ্র mode the IME uses `setComposingText` while Latin is buffered,
then `commitText` on space, punctuation, Enter, or mode change.

## Bengali Unicode

Direct বাংলা mode commits code points including vowel signs (া ি ু …) and hasanta (্).
The editor composes `ক` + `া` → কা. Backspace deletes one code unit.

## Tests

```bash
flutter test test/avro_engine_test.dart
```
