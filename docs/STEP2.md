# Step 2 — Keyboard UI and basic input

Native `KeyboardPanel` draws the IME. `AvroKeyboardService` still owns
`InputConnection`.

- Letters commit through `commitText`.
- Space commits a single space.
- Backspace uses `deleteSurroundingText(1, 0)`.
- Enter sends `KEYCODE_ENTER`.
- Shift is one-shot uppercase on the English letter page.
- `123` / `ABC` toggle the symbol page.
- `EN` toggles a placeholder Bangla letter page (no Avro yet).
