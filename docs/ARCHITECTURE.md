# Avro Keyboard architecture

```
Flutter application (lib/)
  settings / enable IME
        |
        v
Kotlin MainActivity (MethodChannel only)
        |
        v
Kotlin AvroKeyboardService : InputMethodService
  KeyboardPanel (drawn keys, layout state)
        |
        v
InputConnection — any Android app
```

Language state lives on `KeyboardPanel` (`KeyboardLanguage`, `KeyboardPage`).
Step 3 should wrap `KeyAction.Commit` when language is BANGLA; do not move
commit into Flutter.
